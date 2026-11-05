package rodalies;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import rodalies.client.ClientHooks;

import java.util.HashMap;
import java.util.Map;

/**
 * Caja de conexiones (sin cables): poste fino con una caja encima. Clic derecho abre un menu con 4
 * flechas (North/South/East/West) y un boton de centro para desplazar la caja dentro de la celda.
 *
 * El desplazamiento se guarda en el estado ({@link #OFFSET_X}/{@link #OFFSET_Z}, en pixeles; 3 = centro,
 * 0 = 3 px hacia el oeste/norte, 6 = 3 px hacia el este/sur). El modelo se traslada en el cliente con
 * {@code OffsetSignModel} (ver {@code ClientSetup#onModifyBakingResult}), asi que no hace falta un JSON
 * por posicion; la forma de colision se traslada igual.
 *
 * "Set default" guarda el desplazamiento en los datos persistentes del jugador (sobreviven a la muerte) y
 * las cajas que coloque despues salen ya con esa posicion ({@link #getStateForPlacement}).
 */
public class JunctionBoxBlock extends Block {

    public static final int CENTER = 3;
    public static final int MAX = 6;
    public static final IntegerProperty OFFSET_X = IntegerProperty.create("offset_x", 0, MAX);
    public static final IntegerProperty OFFSET_Z = IntegerProperty.create("offset_z", 0, MAX);

    /** Acciones del menu (se mandan al servidor en {@code JunctionBoxPacket}). */
    public static final int ACTION_CENTER = 0;
    public static final int ACTION_STEP = 1;   // 1 px hacia la direccion
    public static final int ACTION_EDGE = 2;   // hasta el tope (3 px) en esa direccion
    public static final int ACTION_SET_DEFAULT = 3; // guardar la posicion actual para las siguientes cajas

    private static final String DEFAULT_KEY = "rodalies_junction_box_default";

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(6.5, 0, 6.5, 9.5, 7, 9.5),
            Block.box(3, 7, 3, 13, 13, 13),
            Block.box(2, 9, 2, 14, 11, 14));

    public static final MapCodec<JunctionBoxBlock> CODEC = simpleCodec(JunctionBoxBlock::new);

    private final Map<BlockState, VoxelShape> shapes = new HashMap<>();

    public JunctionBoxBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(OFFSET_X, CENTER).setValue(OFFSET_Z, CENTER));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OFFSET_X, OFFSET_Z);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        Player player = context.getPlayer();
        if (player == null) {
            return state;
        }
        CompoundTag tag = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(DEFAULT_KEY);
        return withOffset(state, clamp(tag.getInt("x")), clamp(tag.getInt("z")));
    }

    /** Guarda el desplazamiento de {@code state} como posicion por defecto de las cajas que coloque el jugador. */
    public static void saveDefault(Player player, BlockState state) {
        CompoundTag root = player.getPersistentData();
        CompoundTag persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", state.getValue(OFFSET_X) - CENTER);
        tag.putInt("z", state.getValue(OFFSET_Z) - CENTER);
        persisted.put(DEFAULT_KEY, tag);
        root.put(Player.PERSISTED_NBT_TAG, persisted);
    }

    private static int clamp(int offset) {
        return Math.max(-CENTER, Math.min(MAX - CENTER, offset));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.computeIfAbsent(state, s -> SHAPE.move(
                (s.getValue(OFFSET_X) - CENTER) / 16.0, 0, (s.getValue(OFFSET_Z) - CENTER) / 16.0));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (level.isClientSide) {
            ClientHooks.openJunctionBoxScreen(pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Estado resultante de aplicar una accion del menu (centro, 1 px o hasta el tope hacia {@code dir}). */
    public static BlockState applyAction(BlockState state, int action, Direction dir) {
        if (action == ACTION_CENTER) {
            return state.setValue(OFFSET_X, CENTER).setValue(OFFSET_Z, CENTER);
        }
        if (dir.getAxis() == Direction.Axis.Y) {
            return state;
        }
        IntegerProperty prop = dir.getAxis() == Direction.Axis.X ? OFFSET_X : OFFSET_Z;
        int step = dir.getAxisDirection().getStep();
        int value = action == ACTION_EDGE ? (step > 0 ? MAX : 0) : state.getValue(prop) + step;
        return state.setValue(prop, Math.max(0, Math.min(MAX, value)));
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        int x = state.getValue(OFFSET_X) - CENTER;
        int z = state.getValue(OFFSET_Z) - CENTER;
        // Rotacion en sentido horario vista desde arriba: (x, z) -> (-z, x).
        return switch (rot) {
            case CLOCKWISE_90 -> withOffset(state, -z, x);
            case CLOCKWISE_180 -> withOffset(state, -x, -z);
            case COUNTERCLOCKWISE_90 -> withOffset(state, z, -x);
            default -> state;
        };
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        int x = state.getValue(OFFSET_X) - CENTER;
        int z = state.getValue(OFFSET_Z) - CENTER;
        return switch (mirror) {
            case LEFT_RIGHT -> withOffset(state, x, -z);
            case FRONT_BACK -> withOffset(state, -x, z);
            default -> state;
        };
    }

    private static BlockState withOffset(BlockState state, int x, int z) {
        return state.setValue(OFFSET_X, x + CENTER).setValue(OFFSET_Z, z + CENTER);
    }
}
