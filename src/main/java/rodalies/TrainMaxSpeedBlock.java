package rodalies;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import rodalies.client.ClientHooks;

import javax.annotation.Nullable;

/**
 * Cubo de cuarzo con la senal de velocidad maxima (rombo) en la cara frontal, con texto editable
 * (<=4) que dibuja {@code TrainMaxSpeedRenderer} centrado en el rombo. Se orienta con {@code FACING}.
 *
 * Port a 1.21: {@code use()} se divide; para abrir la pantalla al hacer clic derecho usamos
 * {@code useWithoutItem}. {@code DistExecutor} desaparece en NeoForge: llamamos al hook de cliente
 * directamente bajo {@code level.isClientSide} (la clase cliente solo se carga en ese lado).
 */
public class TrainMaxSpeedBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public static final MapCodec<TrainMaxSpeedBlock> CODEC = simpleCodec(TrainMaxSpeedBlock::new);

    public TrainMaxSpeedBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends TrainMaxSpeedBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TrainMaxSpeedBlockEntity sign)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            ClientHooks.openTrainMaxSpeedScreen(sign);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TrainMaxSpeedBlockEntity(pos, state);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }
}
