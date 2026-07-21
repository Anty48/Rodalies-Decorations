package rodalies;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import javax.annotation.Nullable;

/**
 * Parabrisas del tren 447: un multiblock horizontal de 3 celdas (izquierda / centro / derecha)
 * que comparten una orientacion. La celda CENTRAL dibuja el modelo 3D completo (48px de ancho,
 * sobresale a los bloques vecinos); las celdas LEFT/RIGHT son solo colision (modelo vacio).
 *
 * Al colocar se rellenan las 3 celdas; al romper cualquiera se rompen las 3 y se dropea 1 item.
 */
public class Parabrisas447Block extends HorizontalDirectionalBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    public Parabrisas447Block(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, Part.CENTER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // Deja pasar la luz como el cristal normal (no proyecta sombra ni atenua el cielo).
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    // Eje de anchura (izquierda<->derecha), perpendicular al facing. +w = lado derecho.
    private static Direction widthDir(Direction facing) {
        return facing.getClockWise();
    }

    // Posicion de la celda central a partir de cualquier parte.
    private static BlockPos centerOf(BlockPos pos, BlockState state) {
        Direction w = widthDir(state.getValue(FACING));
        return switch (state.getValue(PART)) {
            case LEFT -> pos.relative(w);              // LEFT esta en -w -> centro en +w
            case RIGHT -> pos.relative(w.getOpposite());
            default -> pos;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        Direction facing = context.getHorizontalDirection().getOpposite(); // mira hacia el jugador
        BlockPos center = context.getClickedPos();
        Direction w = widthDir(facing);
        BlockPos left = center.relative(w.getOpposite());
        BlockPos right = center.relative(w);

        if (level.getBlockState(left).canBeReplaced(context)
                && level.getBlockState(right).canBeReplaced(context)) {
            return defaultBlockState().setValue(FACING, facing).setValue(PART, Part.CENTER);
        }
        return null; // no hay hueco para las 3 celdas -> no se coloca
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            Direction w = widthDir(state.getValue(FACING));
            level.setBlock(pos.relative(w.getOpposite()), state.setValue(PART, Part.LEFT), 3);
            level.setBlock(pos.relative(w), state.setValue(PART, Part.RIGHT), 3);
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // Rompe las otras dos celdas sin drop (flag 35 = suppress drops); la celda que rompe
        // el jugador dropea 1 item via su loot table (dropSelf) -> exactamente 1 item.
        if (!level.isClientSide) {
            BlockPos center = centerOf(pos, state);
            Direction w = widthDir(state.getValue(FACING));
            BlockPos[] all = { center, center.relative(w.getOpposite()), center.relative(w) };
            for (BlockPos p : all) {
                if (p.equals(pos)) continue;
                if (level.getBlockState(p).getBlock() == this) {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), 35);
                }
            }
        }
        super.playerWillDestroy(level, pos, state, player);
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
        LEFT("left"),
        CENTER("center"),
        RIGHT("right");

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
