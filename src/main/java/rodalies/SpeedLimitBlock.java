package rodalies;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import rodalies.client.ClientHooks;

/**
 * Señal de velocidad permanente. Es una {@link RailSignBlock} normal (3 celdas) con un estado extra
 * {@code stack} que apila mas señales por debajo (como se apila la nieve):
 *   - {@code double}: dos señales de velocidad.
 *   - {@code triple}: tres señales de velocidad.
 *   - {@code ltv_end}: velocidad arriba + fin de LTV abajo (suelen ir juntas).
 * Se apila con clic derecho poniendo la señal correspondiente en la mano. Solo existen esas
 * combinaciones (no se mezcla velocidad+velocidad+finLTV). Combinar con fin de LTV funciona en
 * cualquier orden (ver {@link RailSignBlock#use}). No es un bloque nuevo ni tiene receta.
 */
public class SpeedLimitBlock extends RailSignBlock {

    /** Qué hay apilado debajo. */
    public enum Stack implements StringRepresentable {
        NONE("none"),        // señal simple
        DOUBLE("double"),    // dos velocidades
        TRIPLE("triple"),    // tres velocidades
        LTV_END("ltv_end");  // velocidad + fin de LTV

        private final String name;

        Stack(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final EnumProperty<Stack> STACK = EnumProperty.create("stack", Stack.class);

    // Colision de las señales apiladas, definida mirando al norte (se rota segun el facing).
    // DOBLE: el rombo inferior va de y=-6..9 (parte en MIDDLE y parte en BOTTOM).
    private static final VoxelShape SPEED_MID = Block.box(0, 0, 5, 16, 9, 8);   // parte en MIDDLE (0..9)
    private static final VoxelShape SPEED_BOT = Block.box(0, 10, 5, 16, 16, 8); // parte en BOTTOM (-6..0)
    // TRIPLE: señal central (y=0..15, celda MIDDLE) e inferior (y=-16..-1, celda BOTTOM).
    private static final VoxelShape TRIPLE_SIGN = Block.box(0, 0, 5, 16, 15, 8);
    // FIN DE LTV: cuadrado inferior de y=2..13 (entero dentro de la celda MIDDLE).
    private static final VoxelShape LTVEND_MID = Block.box(1, 2, 5, 15, 13, 8);

    public SpeedLimitBlock(Properties properties) {
        super(properties, RailSignType.SPEED_LIMIT, false);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, Part.MIDDLE)
                .setValue(STACK, Stack.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder); // FACING, PART
        builder.add(STACK);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        VoxelShape base = super.getShape(state, level, pos, ctx);
        Stack stack = state.getValue(STACK);
        if (stack == Stack.NONE) {
            return base;
        }
        Direction facing = state.getValue(FACING);
        Part part = state.getValue(PART);
        return switch (stack) {
            case DOUBLE -> switch (part) {
                case MIDDLE -> Shapes.or(base, rotateToFacing(SPEED_MID, facing));
                case BOTTOM -> Shapes.or(base, rotateToFacing(SPEED_BOT, facing));
                default -> base;
            };
            case TRIPLE -> switch (part) {
                case MIDDLE, BOTTOM -> Shapes.or(base, rotateToFacing(TRIPLE_SIGN, facing));
                default -> base;
            };
            case LTV_END -> part == Part.MIDDLE ? Shapes.or(base, rotateToFacing(LTVEND_MID, facing)) : base;
            default -> base;
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        Stack stack = state.getValue(STACK);

        // Apilar otra señal: clic derecho con la señal correspondiente en mano y sin agacharse.
        if (!player.isSecondaryUseActive()) {
            if (held.is(ModBlocks.SPEED_LIMIT_ITEM.get())) {
                // Añadir velocidad: none -> double -> triple (max).
                if (stack == Stack.NONE) {
                    return apply(level, pos, state, Stack.DOUBLE, player, held);
                }
                if (stack == Stack.DOUBLE) {
                    return apply(level, pos, state, Stack.TRIPLE, player, held);
                }
            } else if (held.is(ModBlocks.LTV_END_ITEM.get()) && stack == Stack.NONE) {
                // Añadir fin de LTV: solo sobre una velocidad simple.
                return apply(level, pos, state, Stack.LTV_END, player, held);
            }
        }

        // Editar: elegir la señal segun la altura del clic (solo las de velocidad llevan texto).
        BlockPos middle = middleOf(pos, state);
        BlockEntity be = level.getBlockEntity(middle);
        if (!(be instanceof RailSignBlockEntity sign)) {
            return InteractionResult.PASS;
        }
        int slot = slotForHit(stack, hit.getLocation().y - middle.getY());
        if (level.isClientSide) {
            int fslot = slot;
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.openRailSignScreen(sign, fslot));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    /** Slot de texto (0=arriba, 1=centro, 2=abajo) segun el estado y la altura local del clic. */
    private static int slotForHit(Stack stack, double localY) {
        return switch (stack) {
            case DOUBLE -> localY > 0.9 ? 0 : 1;                 // arriba / abajo
            case TRIPLE -> localY > 1.0 ? 0 : (localY > 0.0 ? 1 : 2); // arriba / centro / abajo
            default -> 0;                                        // simple o fin de LTV: solo la de arriba
        };
    }

    private InteractionResult apply(Level level, BlockPos pos, BlockState state, Stack stack,
                                    Player player, ItemStack held) {
        if (!level.isClientSide) {
            setStackOnAllCells(level, pos, state, stack);
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            level.playSound(null, pos, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1f, 1f);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Marca el estado stack en las 3 celdas (conserva el BlockEntity y sus textos). */
    private void setStackOnAllCells(Level level, BlockPos pos, BlockState state, Stack stack) {
        BlockPos middle = middleOf(pos, state);
        for (BlockPos p : new BlockPos[]{middle.below(), middle, middle.above()}) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() == this && s.hasProperty(STACK)) {
                level.setBlock(p, s.setValue(STACK, stack), 3);
            }
        }
    }

    /**
     * Combina partiendo de un FIN DE LTV ya colocado al que se le añade una señal de velocidad:
     * reemplaza las 3 celdas por señales de velocidad en estado {@code stack=ltv_end} (velocidad
     * arriba, fin de LTV abajo). Da igual el orden de colocacion: el resultado es el mismo modelo.
     */
    static void combineOntoLtvEnd(Level level, BlockPos pos, BlockState ltvEndState) {
        SpeedLimitBlock block = (SpeedLimitBlock) ModBlocks.SPEED_LIMIT.get();
        Direction facing = ltvEndState.getValue(FACING);
        BlockPos middle = middleOf(pos, ltvEndState);
        BlockState base = block.defaultBlockState().setValue(FACING, facing).setValue(STACK, Stack.LTV_END);
        level.setBlock(middle.below(), base.setValue(PART, Part.BOTTOM), 3);
        level.setBlock(middle.above(), base.setValue(PART, Part.TOP), 3);
        level.setBlock(middle, base.setValue(PART, Part.MIDDLE), 3); // ultimo -> crea el BlockEntity
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // Al romper una apilada se dropean tambien las señales extra (la primera va por loot).
        if (!level.isClientSide && !player.getAbilities().instabuild) {
            switch (state.getValue(STACK)) {
                case DOUBLE -> popResource(level, pos, new ItemStack(ModBlocks.SPEED_LIMIT_ITEM.get()));
                case TRIPLE -> {
                    popResource(level, pos, new ItemStack(ModBlocks.SPEED_LIMIT_ITEM.get()));
                    popResource(level, pos, new ItemStack(ModBlocks.SPEED_LIMIT_ITEM.get()));
                }
                case LTV_END -> popResource(level, pos, new ItemStack(ModBlocks.LTV_END_ITEM.get()));
                default -> { /* NONE: nada extra */ }
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }
}
