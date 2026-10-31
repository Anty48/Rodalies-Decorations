package rodalies;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import rodalies.client.ClientHooks;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Version de PARED de una señal ferroviaria: la misma señal sin poste, en 1 sola celda, con la
 * trasera pegada al bloque que tiene detras (el bloque "soporte", en la direccion opuesta a FACING).
 *
 * Ajuste automatico al soporte: si el soporte no es un bloque entero (un poste fino de Pantographs &
 * Wires, una valla, un muro...), la señal se mete dentro de la celda del soporte hasta tocar su cara,
 * sin dejar hueco. La distancia se lee de la VoxelShape del soporte (sin depender de ningun mod) y se
 * guarda en {@link #OFFSET} en medios pixeles. Se recalcula si el soporte cambia de forma.
 * El desplazamiento visual del modelo lo aplica {@code OffsetSignModel} (cliente) y el del texto
 * {@code RailSignRenderer}; la colision se desplaza aqui.
 *
 * Port a 1.21: {@code codec()} obligatorio, {@code use} -> {@code useWithoutItem} y el hook de cliente se
 * llama directamente bajo {@code isClientSide} (sin DistExecutor).
 */
public class RailSignWallBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    /** Hueco hasta la cara del soporte, en MEDIOS pixeles (0 = soporte de cara completa; max 15.5px). */
    public static final IntegerProperty OFFSET = IntegerProperty.create("offset", 0, 31);

    private final RailSignType type;
    private final Map<BlockState, VoxelShape> shapes = new HashMap<>();
    // Zona de la cara del soporte que tapa la señal (por facing): solo cuenta la forma del soporte
    // que queda detras de la señal, no salientes que esten a otra altura.
    private final Map<Direction, VoxelShape> footprints = new EnumMap<>(Direction.class);

    /**
     * @param shapeNorth colision de la señal mirando al norte y pegada a la pared (pared en z=16),
     *                   sin desplazamiento.
     */
    public RailSignWallBlock(Properties properties, RailSignType type, VoxelShape shapeNorth) {
        super(properties);
        this.type = type;
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OFFSET, 0));

        AABB b = shapeNorth.bounds();
        VoxelShape footNorth = Shapes.box(Math.max(0, b.minX), Math.max(0, b.minY), 0,
                Math.min(1, b.maxX), Math.min(1, b.maxY), 1);
        for (Direction d : Direction.Plane.HORIZONTAL) {
            footprints.put(d, RailSignBlock.rotateToFacing(footNorth, d));
        }
        for (BlockState s : stateDefinition.getPossibleStates()) {
            VoxelShape moved = shapeNorth.move(0, 0, s.getValue(OFFSET) / 32.0);
            shapes.put(s, RailSignBlock.rotateToFacing(moved, s.getValue(FACING)));
        }
    }

    // codec() es abstracto en HorizontalDirectionalBlock (1.21). Se registran por codigo y nunca se
    // serializan por datapack: un codec aproximado basta (igual que RailSignBlock).
    public static final MapCodec<RailSignWallBlock> CODEC =
            simpleCodec(p -> new RailSignWallBlock(p, RailSignType.NORMAL, Shapes.block()));

    @Override
    protected MapCodec<? extends RailSignWallBlock> codec() {
        return CODEC;
    }

    public RailSignType getSignType() {
        return type;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OFFSET);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return shapes.get(state);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Clic en un lateral: la señal mira hacia fuera de esa cara. Clic arriba/abajo: hacia el jugador.
        Direction face = context.getClickedFace();
        Direction facing = face.getAxis().isHorizontal() ? face : context.getHorizontalDirection().getOpposite();
        int offset = computeOffset(context.getLevel(), context.getClickedPos(), facing, 0);
        return defaultBlockState().setValue(FACING, facing).setValue(OFFSET, offset);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        Direction facing = state.getValue(FACING);
        if (dir == facing.getOpposite()) {
            // El soporte ha cambiado (p.ej. un poste de PNW que se conecta): reajustar el hueco.
            return state.setValue(OFFSET, computeOffset(level, pos, facing, state.getValue(OFFSET)));
        }
        return state;
    }

    /**
     * Hueco (en medios pixeles) entre el borde de la celda de la señal y la cara mas cercana del
     * soporte. Si el soporte no tiene forma (aire, plantas...) devuelve {@code fallback}.
     */
    private int computeOffset(BlockGetter level, BlockPos pos, Direction facing, int fallback) {
        Direction toSupport = facing.getOpposite();
        BlockPos supportPos = pos.relative(toSupport);
        VoxelShape support = level.getBlockState(supportPos).getShape(level, supportPos);
        if (support.isEmpty()) {
            return fallback;
        }
        VoxelShape behind = Shapes.join(support, footprints.get(facing), BooleanOp.AND);
        if (!behind.isEmpty()) {
            support = behind;
        }
        Direction.Axis axis = toSupport.getAxis();
        double gap = toSupport.getAxisDirection() == Direction.AxisDirection.POSITIVE
                ? support.min(axis)
                : 1.0 - support.max(axis);
        return Mth.clamp((int) Math.round(gap * 32.0), 0, 31);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (!type.editable()) {
            return InteractionResult.PASS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof RailSignBlockEntity sign)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            ClientHooks.openRailSignScreen(sign, 0);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return type.editable() ? new RailSignBlockEntity(pos, state) : null;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
