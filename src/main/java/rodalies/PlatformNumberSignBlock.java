package rodalies;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import rodalies.client.ClientHooks;

/**
 * Cartel de numero de via ({@link RailSignType#PLATFORM_NUMBER}). Es una {@link RailSignBlock} normal
 * (3 celdas) con un estado extra {@code doubled}: clic derecho con otro cartel de numero de via en la
 * mano lo convierte en la version DOBLE, que cuelga dos paneles simetricos (uno a cada lado del poste).
 * Cada panel lleva su propio numero: el panel delantero usa el texto principal (slot 0) y el trasero el
 * texto 2 (slot 1). Al editar, se elige el panel segun el lado por el que se clica. Solo hay simple y
 * doble (como la nieve). No es un bloque nuevo ni tiene receta: se parte de un cartel simple colocado.
 */
public class PlatformNumberSignBlock extends RailSignBlock {

    public static final BooleanProperty DOUBLED = BooleanProperty.create("doubled");

    // Colision de la version DOBLE en la celda TOP (mirando al norte, se rota segun el facing):
    // poste + los dos paneles colgantes (a +Z y a -Z). Los paneles ocupan y22..31 (top-local 6..15).
    private static final VoxelShape DOUBLE_TOP = Shapes.or(
            Block.box(7, 0, 7, 9, 16, 9),    // poste
            Block.box(7, 6, 9, 9, 15, 16),   // panel +Z
            Block.box(7, 6, 0, 9, 15, 7));   // panel -Z

    public PlatformNumberSignBlock(Properties properties) {
        super(properties, RailSignType.PLATFORM_NUMBER);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, Part.MIDDLE)
                .setValue(DOUBLED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder); // FACING, PART
        builder.add(DOUBLED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        if (state.getValue(DOUBLED) && state.getValue(PART) == Part.TOP) {
            return rotateToFacing(DOUBLE_TOP, state.getValue(FACING));
        }
        return super.getShape(state, level, pos, ctx);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        // Duplicar: clic derecho con otro cartel de numero de via en mano, sin agacharse, sobre uno
        // simple -> pasa a doble (dos paneles). Consume el item de la mano.
        if (!state.getValue(DOUBLED) && !player.isSecondaryUseActive()
                && held.is(ModBlocks.PLATFORM_NUMBER_SIGNAL_ITEM.get())) {
            if (!level.isClientSide) {
                setDoubledOnAllCells(level, pos, state);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                level.playSound(null, pos, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1f, 1f);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // Editar: el texto lo guarda el BlockEntity de MIDDLE. En la version doble se elige el panel
        // (slot 0 = panel delantero +Z, slot 1 = panel trasero -Z) segun el lado por el que se clica.
        BlockPos middle = middleOf(pos, state);
        BlockEntity be = level.getBlockEntity(middle);
        if (!(be instanceof RailSignBlockEntity sign)) {
            return InteractionResult.PASS;
        }
        int slot = state.getValue(DOUBLED) ? slotForHit(state.getValue(FACING), middle, hit) : 0;
        if (level.isClientSide) {
            int fslot = slot;
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.openRailSignScreen(sign, fslot));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    /**
     * Slot del panel segun el lado del clic: pasa el punto de impacto al marco del modelo (sin rotar)
     * y mira su Z. Panel delantero (+Z) = slot 0, panel trasero (-Z) = slot 1.
     */
    private static int slotForHit(Direction facing, BlockPos middle, BlockHitResult hit) {
        double dx = hit.getLocation().x - (middle.getX() + 0.5);
        double dz = hit.getLocation().z - (middle.getZ() + 0.5);
        double modelZ = switch (facing) {
            case EAST -> -dx;
            case SOUTH -> -dz;
            case WEST -> dx;
            default -> dz; // NORTH
        };
        return modelZ >= 0 ? 0 : 1;
    }

    /** Marca doubled=true en las 3 celdas (conserva el BlockEntity y su texto). */
    private void setDoubledOnAllCells(Level level, BlockPos pos, BlockState state) {
        BlockPos middle = middleOf(pos, state);
        for (BlockPos p : new BlockPos[]{middle.below(), middle, middle.above()}) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() == this && s.hasProperty(DOUBLED)) {
                level.setBlock(p, s.setValue(DOUBLED, true), 3);
            }
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // La version doble se hizo con 2 items: al romperla se dropea el segundo (el primero va por loot).
        if (!level.isClientSide && !player.getAbilities().instabuild && state.getValue(DOUBLED)) {
            popResource(level, pos, new ItemStack(ModBlocks.PLATFORM_NUMBER_SIGNAL_ITEM.get()));
        }
        super.playerWillDestroy(level, pos, state, player);
    }
}
