package rodalies;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import rodalies.client.ClientHooks;

import javax.annotation.Nullable;

/**
 * Cartel grande de estacion. Es un multiblock en forma de T (5 celdas) que comparten orientacion,
 * al estilo del {@link Parabrisas447Block}:
 *
 *   [TL][TC][TR]   fila superior: el panel con el nombre (3 celdas de ancho)
 *       [MID]      celda del poste que dibuja el modelo 3D completo + guarda el texto (BlockEntity)
 *       [BASE]     base del poste (se apoya donde iria un bloque normal -> no queda hundido)
 *
 * La celda MID es la unica que dibuja el modelo y tiene el BlockEntity; el resto son solo colision
 * (modelo vacio). Al colocar se rellenan las 5 celdas; al romper cualquiera se rompen todas y se
 * dropea 1 item. El clic derecho en cualquier celda abre el editor de texto (se reenvia a MID).
 */
public class StationSignalBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    // Colisiones (facing=north). El poste es simetrico -> invariante a la rotacion.
    private static final VoxelShape POLE = Block.box(7, 0, 7, 9, 16, 9);
    // Losa del panel (banda + cartel), local y 5..16, un poco mas profunda (z 6..10) para que sea
    // facil de apuntar/romper. Segun el facing va en el eje X o Z.
    private static final VoxelShape SLAB_NS = Block.box(0, 5, 6, 16, 16, 10);
    private static final VoxelShape SLAB_EW = Block.box(6, 5, 0, 10, 16, 16);

    // true = el panel lleva el logo de Rodalies a un lado -> el renderer desplaza el texto para no
    // taparlo. false = panel sin logo -> el renderer centra el texto en todo el panel.
    private final boolean hasLogo;

    public StationSignalBlock(Properties properties, boolean hasLogo) {
        super(properties);
        this.hasLogo = hasLogo;
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, Part.MID));
    }

    /** ¿El panel lleva el logo de Rodalies? Lo usa el renderer para decidir si centra el texto. */
    public boolean hasLogo() {
        return hasLogo;
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
        return switch (state.getValue(PART)) {
            case BASE, MID -> POLE;
            default -> isNorthSouth(state.getValue(FACING)) ? SLAB_NS : SLAB_EW;
        };
    }

    private static boolean isNorthSouth(Direction facing) {
        return facing == Direction.NORTH || facing == Direction.SOUTH;
    }

    // Eje de anchura (izquierda<->derecha), perpendicular al facing. +w = lado derecho.
    private static Direction widthDir(Direction facing) {
        return facing.getClockWise();
    }

    /** Posicion de la celda MID a partir de cualquier parte. */
    private static BlockPos midOf(BlockPos pos, BlockState state) {
        Direction w = widthDir(state.getValue(FACING));
        return switch (state.getValue(PART)) {
            case BASE -> pos.above();
            case TOP -> pos.below();
            case TOP_LEFT -> pos.below().relative(w);         // TOP_LEFT = MID.above().relative(-w)
            case TOP_RIGHT -> pos.below().relative(w.getOpposite());
            default -> pos;                                    // MID
        };
    }

    /** Las 5 posiciones del multiblock a partir de la celda MID. */
    private static BlockPos[] cellsFromMid(BlockPos mid, Direction facing) {
        Direction w = widthDir(facing);
        return new BlockPos[] {
                mid.below(),                        // BASE
                mid,                                // MID
                mid.above(),                        // TOP
                mid.above().relative(w.getOpposite()), // TOP_LEFT
                mid.above().relative(w)             // TOP_RIGHT
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        Direction facing = context.getHorizontalDirection().getOpposite(); // mira hacia el jugador
        Direction w = widthDir(facing);
        // La celda clicada es la BASE del poste; MID va una encima -> el poste se apoya y no se hunde.
        BlockPos base = context.getClickedPos();
        BlockPos mid = base.above();
        BlockPos top = mid.above();
        BlockPos topLeft = top.relative(w.getOpposite());
        BlockPos topRight = top.relative(w);

        if (level.getBlockState(mid).canBeReplaced(context)
                && level.getBlockState(top).canBeReplaced(context)
                && level.getBlockState(topLeft).canBeReplaced(context)
                && level.getBlockState(topRight).canBeReplaced(context)) {
            return defaultBlockState().setValue(FACING, facing).setValue(PART, Part.BASE);
        }
        return null; // no hay hueco para las 5 celdas -> no se coloca
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            // 'pos' es la BASE; coloca las otras 4 celdas.
            Direction facing = state.getValue(FACING);
            Direction w = widthDir(facing);
            BlockPos mid = pos.above();
            BlockPos top = mid.above();
            level.setBlock(mid, state.setValue(PART, Part.MID), 3);
            level.setBlock(top, state.setValue(PART, Part.TOP), 3);
            level.setBlock(top.relative(w.getOpposite()), state.setValue(PART, Part.TOP_LEFT), 3);
            level.setBlock(top.relative(w), state.setValue(PART, Part.TOP_RIGHT), 3);
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // Rompe las otras celdas sin drop (flag 35 = suppress drops); la celda que rompe el jugador
        // dropea 1 item via su loot table (dropSelf) -> exactamente 1 item.
        if (!level.isClientSide) {
            BlockPos mid = midOf(pos, state);
            for (BlockPos p : cellsFromMid(mid, state.getValue(FACING))) {
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
        // El texto lo guarda el BlockEntity de MID; reenvia la interaccion alli desde cualquier celda.
        BlockPos mid = midOf(pos, state);
        BlockEntity be = level.getBlockEntity(mid);
        if (!(be instanceof StationSignalBlockEntity sign)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.openStationSignalScreen(sign));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Solo la celda central tiene BlockEntity (guarda el texto y dibuja el renderer).
        return state.getValue(PART) == Part.MID ? new StationSignalBlockEntity(pos, state) : null;
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
        BASE("base"),
        MID("mid"),
        TOP("top"),
        TOP_LEFT("top_left"),
        TOP_RIGHT("top_right");

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
