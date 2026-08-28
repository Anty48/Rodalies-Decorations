package rodalies;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import rodalies.client.ClientHooks;

import javax.annotation.Nullable;

/**
 * Señal ferroviaria: un poste con una señal encima. Es un multiblock VERTICAL de 3 celdas (abajo,
 * medio, arriba) que comparten orientacion, al estilo del {@link StationSignalBlock}. Se coloca
 * desde la celda de mas ABAJO (la que clica el jugador), de forma que la base del poste queda al ras
 * del suelo y nada atraviesa el suelo.
 *
 *   [TOP]     dibuja la señal (sobresale hacia arriba); colision = caja de la señal
 *   [MIDDLE]  dibuja el modelo 3D completo + guarda el texto (BlockEntity); colision = poste
 *   [BOTTOM]  base del poste (la celda que clica el jugador); colision = poste
 *
 * La celda MIDDLE es la unica que dibuja el modelo y lleva el BlockEntity; el resto son solo
 * colision (modelo vacio). Segun su {@link RailSignType} lleva texto editable o no. noOcclusion
 * porque casi toda cada celda es aire.
 */
public class RailSignBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    // El poste ocupa el centro de la celda a toda altura (simetrico -> invariante a la rotacion).
    private static final VoxelShape POLE = Block.box(7, 0, 7, 9, 16, 9);

    // Colision de la señal en la celda TOP, en orientacion norte (se rota segun el facing).
    private static final VoxelShape TOP_DIAMOND = Block.box(0, 1, 5, 16, 14, 8);   // rombos (velocidad/LVT)
    private static final VoxelShape TOP_NORMAL = Block.box(1, 2, 5, 15, 16, 8);    // cuadrado (cartel)
    private static final VoxelShape TOP_LVT_END = Block.box(1, 2, 5, 15, 15, 8);   // cuadrado (fin LTV)
    // Poste + panel colgante de via. El panel se recorta 6px por abajo (empezaba demasiado bajo).
    private static final VoxelShape TOP_PLATFORM = Shapes.or(
            Block.box(7, 0, 7, 9, 16, 9),    // poste, toda la altura de la celda
            Block.box(7, 6, 9, 9, 16, 16));  // panel colgante (recortado 6px por abajo)

    private final RailSignType type;
    // Variante "corta": el poste es una celda mas bajo (sin la celda BOTTOM). El modelo NO se baja,
    // asi que se coloca con la celda clicada como MIDDLE y el texto conserva su posicion.
    private final boolean shortPole;

    public RailSignBlock(Properties properties, RailSignType type) {
        this(properties, type, false);
    }

    public RailSignBlock(Properties properties, RailSignType type, boolean shortPole) {
        super(properties);
        this.type = type;
        this.shortPole = shortPole;
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, Part.MIDDLE));
    }

    public boolean isShort() {
        return shortPole;
    }

    public RailSignType getSignType() {
        return type;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        if (state.getValue(PART) == Part.TOP) {
            return rotateToFacing(topShapeNorth(), state.getValue(FACING));
        }
        return POLE;
    }

    private VoxelShape topShapeNorth() {
        return switch (type) {
            case SPEED_LIMIT, LTV -> TOP_DIAMOND;
            case NORMAL -> TOP_NORMAL;
            case LTV_END -> TOP_LVT_END;
            case PLATFORM_NUMBER -> TOP_PLATFORM;
        };
    }

    /** Rota una VoxelShape definida mirando al norte para que coincida con el facing dado. */
    static VoxelShape rotateToFacing(VoxelShape north, Direction facing) {
        int times = (facing.get2DDataValue() - Direction.NORTH.get2DDataValue() + 4) % 4;
        VoxelShape shape = north;
        for (int i = 0; i < times; i++) {
            VoxelShape src = shape;
            VoxelShape[] acc = {Shapes.empty()};
            src.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    acc[0] = Shapes.or(acc[0], Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            shape = acc[0];
        }
        return shape;
    }

    /** Posicion de la celda MIDDLE a partir de cualquier parte. */
    static BlockPos middleOf(BlockPos pos, BlockState state) {
        return switch (state.getValue(PART)) {
            case BOTTOM -> pos.above();
            case TOP -> pos.below();
            default -> pos; // MIDDLE
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        Direction facing = context.getHorizontalDirection().getOpposite(); // mira hacia el jugador
        if (shortPole) {
            // Variante corta (2 celdas): la celda clicada YA es MIDDLE (dibuja el modelo); solo hace
            // falta hueco para el TOP encima.
            BlockPos middle = context.getClickedPos();
            if (level.getBlockState(middle.above()).canBeReplaced(context)) {
                return defaultBlockState().setValue(FACING, facing).setValue(PART, Part.MIDDLE);
            }
            return null;
        }
        // Variante normal (3 celdas): la celda clicada es la de mas ABAJO; el modelo se dibuja una
        // celda por encima (MIDDLE).
        BlockPos bottom = context.getClickedPos();
        BlockPos middle = bottom.above();
        BlockPos top = middle.above();
        if (level.getBlockState(middle).canBeReplaced(context)
                && level.getBlockState(top).canBeReplaced(context)) {
            return defaultBlockState().setValue(FACING, facing).setValue(PART, Part.BOTTOM);
        }
        return null; // no hay hueco para las 3 celdas -> no se coloca
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            if (shortPole) {
                // 'pos' ya es MIDDLE; solo coloca TOP encima.
                level.setBlock(pos.above(), state.setValue(PART, Part.TOP), 3);
            } else {
                // 'pos' es la celda BOTTOM; coloca MIDDLE y TOP encima.
                BlockPos middle = pos.above();
                level.setBlock(middle, state.setValue(PART, Part.MIDDLE), 3);
                level.setBlock(middle.above(), state.setValue(PART, Part.TOP), 3);
            }
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // Rompe las otras celdas sin drop (flag 35); la que rompe el jugador dropea 1 item via loot.
        if (!level.isClientSide) {
            BlockPos middle = middleOf(pos, state);
            // La variante corta no tiene celda BOTTOM.
            BlockPos[] cells = shortPole
                    ? new BlockPos[]{middle, middle.above()}
                    : new BlockPos[]{middle.below(), middle, middle.above()};
            for (BlockPos p : cells) {
                if (p.equals(pos)) continue;
                if (level.getBlockState(p).getBlock() == this) {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), 35);
                }
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        // Combinar: un FIN DE LTV colocado al que se le añade una señal de velocidad en mano se
        // convierte en la señal doble (velocidad arriba, fin de LTV abajo). Da igual el orden: es
        // simetrico con doblar una velocidad añadiendole un fin de LTV. Solo la version normal.
        if (this == ModBlocks.LTV_END.get() && !shortPole
                && player.getItemInHand(hand).is(ModBlocks.SPEED_LIMIT_ITEM.get())
                && !player.isSecondaryUseActive()) {
            if (!level.isClientSide) {
                SpeedLimitBlock.combineOntoLtvEnd(level, pos, state);
                if (!player.getAbilities().instabuild) {
                    player.getItemInHand(hand).shrink(1);
                }
                level.playSound(null, pos, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1f, 1f);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!type.editable()) {
            return InteractionResult.PASS;
        }
        // El texto lo guarda el BlockEntity de MIDDLE; reenvia la interaccion alli desde cualquier celda.
        BlockPos middle = middleOf(pos, state);
        BlockEntity be = level.getBlockEntity(middle);
        if (!(be instanceof RailSignBlockEntity sign)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.openRailSignScreen(sign, 0));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Solo la celda MIDDLE de las señales editables tiene BlockEntity (guarda el texto).
        return (type.editable() && state.getValue(PART) == Part.MIDDLE)
                ? new RailSignBlockEntity(pos, state) : null;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    public enum Part implements StringRepresentable {
        BOTTOM("bottom"),
        MIDDLE("middle"),
        TOP("top");

        private final String name;

        Part(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
