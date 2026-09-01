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
 * Cartel grande de estacion. Es un multiblock que comparte orientacion (al estilo del
 * {@link Parabrisas447Block}) y que admite TRES modos de montaje segun donde lo coloques:
 *
 * <ul>
 *   <li><b>FLOOR</b> (clic sobre el suelo): la version "clasica" con poste de soporte. Multiblock
 *       en forma de T (5 celdas): un poste de 2 celdas + una fila de panel de 3 de ancho arriba.
 *       El cartel ocupa un espacio de 3x3.</li>
 *   <li><b>WALL</b> (clic sobre una pared, mirandola de frente): el mismo panel de 3 de ancho pero
 *       SIN poste, pegado a la pared (1x3).</li>
 *   <li><b>AIR</b> (clic "de lado", p.ej. mirando al norte pero colocandolo en la cara oeste de un
 *       bloque): el panel de 3 de ancho SIN poste y CENTRADO en la celda (1x3).</li>
 * </ul>
 *
 * En todos los modos, la celda central (MID) es la unica que dibuja el modelo 3D y tiene el
 * BlockEntity con el texto; las demas son solo colision (modelo vacio). Al colocar se rellenan
 * todas las celdas; al romper cualquiera se rompen todas y se dropea 1 item.
 */
public class StationSignalBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
    public static final EnumProperty<Mount> MOUNT = EnumProperty.create("mount", Mount.class);

    // --- Colisiones (definidas mirando al NORTE; se rotan con rotateToFacing segun el facing) ---
    private static final VoxelShape POLE = Block.box(7, 0, 7, 9, 16, 9);
    // FLOOR: losa del panel (banda + cartel) en la fila superior, algo profunda para apuntarla bien.
    private static final VoxelShape FLOOR_PANEL_N = Block.box(0, 5, 6, 16, 16, 10);
    // WALL: panel fino pegado a la cara trasera (+Z cuando mira al norte -> contra la pared de detras).
    private static final VoxelShape WALL_PANEL_N = Block.box(0, 4, 14, 16, 13, 16);
    // AIR: panel fino centrado en la celda (cada una de las 3 celdas del multiblock lleva su trozo).
    private static final VoxelShape AIR_PANEL_N = Block.box(0, 4, 7, 16, 13, 9);

    // true = el panel lleva el logo de Rodalies a un lado -> el renderer desplaza el texto para no
    // taparlo. false = panel sin logo -> el renderer centra el texto en todo el panel.
    private final boolean hasLogo;

    public StationSignalBlock(Properties properties, boolean hasLogo) {
        super(properties);
        this.hasLogo = hasLogo;
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, Part.MID)
                .setValue(MOUNT, Mount.FLOOR));
    }

    /** ¿El panel lleva el logo de Rodalies? Lo usa el renderer para decidir si centra el texto. */
    public boolean hasLogo() {
        return hasLogo;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, MOUNT);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        Direction facing = state.getValue(FACING);
        return switch (state.getValue(MOUNT)) {
            case FLOOR -> switch (state.getValue(PART)) {
                case BASE, MID -> POLE;
                default -> RailSignBlock.rotateToFacing(FLOOR_PANEL_N, facing);
            };
            case WALL -> RailSignBlock.rotateToFacing(WALL_PANEL_N, facing);
            case AIR -> RailSignBlock.rotateToFacing(AIR_PANEL_N, facing);
        };
    }

    // Eje de anchura (izquierda<->derecha), perpendicular al facing. +w = lado derecho (este si mira al norte).
    private static Direction widthDir(Direction facing) {
        return facing.getClockWise();
    }

    /** Posicion de la celda MID (la que renderiza + guarda el texto) a partir de cualquier parte. */
    private static BlockPos midOf(BlockPos pos, BlockState state) {
        Direction w = widthDir(state.getValue(FACING));
        if (state.getValue(MOUNT) == Mount.FLOOR) {
            return switch (state.getValue(PART)) {
                case BASE -> pos.above();
                case TOP -> pos.below();
                case TOP_LEFT -> pos.below().relative(w);         // TOP_LEFT = MID.above().relative(-w)
                case TOP_RIGHT -> pos.below().relative(w.getOpposite());
                default -> pos;                                    // MID
            };
        }
        // WALL: los flancos estan a la MISMA altura que el MID, a los lados. AIR es de 1 celda (PART=MID).
        return switch (state.getValue(PART)) {
            case TOP_LEFT -> pos.relative(w);                     // TOP_LEFT = MID.relative(-w)
            case TOP_RIGHT -> pos.relative(w.getOpposite());      // TOP_RIGHT = MID.relative(+w)
            default -> pos;                                        // MID
        };
    }

    /** Todas las posiciones del multiblock a partir de la celda MID (depende del modo de montaje). */
    private static BlockPos[] cellsFromMid(BlockPos mid, BlockState state) {
        Direction w = widthDir(state.getValue(FACING));
        if (state.getValue(MOUNT) == Mount.FLOOR) {
            return new BlockPos[] {
                    mid.below(),                            // BASE
                    mid,                                    // MID
                    mid.above(),                            // TOP
                    mid.above().relative(w.getOpposite()),  // TOP_LEFT
                    mid.above().relative(w)                 // TOP_RIGHT
            };
        }
        // WALL / AIR: fila de panel de 3 de ancho (mid + 2 flancos).
        return new BlockPos[] {
                mid,                                        // MID
                mid.relative(w.getOpposite()),              // TOP_LEFT
                mid.relative(w)                             // TOP_RIGHT
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        Direction clicked = context.getClickedFace();
        Direction look = context.getHorizontalDirection();

        // --- Decidir modo de montaje + orientacion segun la cara clicada y hacia donde mira el jugador ---
        Mount mount;
        Direction facing;
        if (clicked == Direction.UP) {
            mount = Mount.FLOOR;
            facing = look.getOpposite();                    // mira hacia el jugador
        } else if (clicked.getAxis().isHorizontal()) {
            if (clicked.getAxis() == look.getAxis()) {
                // Se mira la pared de frente -> pegado a ella, mirando hacia afuera.
                mount = Mount.WALL;
                facing = clicked;
            } else {
                // Se clica una cara lateral (perpendicular a la mirada) -> panel centrado "de lado".
                mount = Mount.AIR;
                facing = look.getOpposite();
            }
        } else { // cara inferior: sin sentido con poste -> panel centrado
            mount = Mount.AIR;
            facing = look.getOpposite();
        }

        BlockState state = defaultBlockState().setValue(FACING, facing).setValue(MOUNT, mount);
        BlockPos clickedPos = context.getClickedPos();
        Direction w = widthDir(facing);

        if (mount == Mount.FLOOR) {
            // La celda clicada es la BASE del poste; MID va una encima -> el poste se apoya y no se hunde.
            state = state.setValue(PART, Part.BASE);
            BlockPos mid = clickedPos.above();
            return allReplaceable(level, context, cellsFromMid(mid, state), clickedPos) ? state : null;
        }

        if (mount == Mount.AIR && clicked.getAxis().isHorizontal()) {
            // AIR "de lado": el panel debe quedar PEGADO al bloque clicado, no centrado sobre el hueco.
            // La celda clicada es el FLANCO adyacente al bloque; MID va una celda mas alla (hacia 'clicked',
            // que apunta HACIA AFUERA del bloque). Asi el borde del panel queda a ras de la cara clicada.
            Part clickedPart = clicked == w ? Part.TOP_LEFT : Part.TOP_RIGHT;
            state = state.setValue(PART, clickedPart);
            BlockPos mid = midOf(clickedPos, state); // = clickedPos.relative(clicked)
            return allReplaceable(level, context, cellsFromMid(mid, state), clickedPos) ? state : null;
        }

        // WALL (o AIR desde cara inferior): la celda clicada es directamente el MID (centro del panel).
        state = state.setValue(PART, Part.MID);
        return allReplaceable(level, context, cellsFromMid(clickedPos, state), clickedPos) ? state : null;
    }

    /** ¿Estan libres (reemplazables) todas las celdas del multiblock salvo la ya clicada? */
    private static boolean allReplaceable(Level level, BlockPlaceContext context, BlockPos[] cells, BlockPos clickedPos) {
        for (BlockPos p : cells) {
            if (p.equals(clickedPos)) continue;
            if (!level.getBlockState(p).canBeReplaced(context)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) {
            return;
        }
        Direction w = widthDir(state.getValue(FACING));
        if (state.getValue(MOUNT) == Mount.FLOOR) {
            // 'pos' es la BASE; coloca las otras 4 celdas de la T.
            BlockPos mid = pos.above();
            BlockPos top = mid.above();
            level.setBlock(mid, state.setValue(PART, Part.MID), 3);
            level.setBlock(top, state.setValue(PART, Part.TOP), 3);
            level.setBlock(top.relative(w.getOpposite()), state.setValue(PART, Part.TOP_LEFT), 3);
            level.setBlock(top.relative(w), state.setValue(PART, Part.TOP_RIGHT), 3);
        } else {
            // WALL / AIR: fila de panel de 3 celdas. Segun donde este 'pos':
            //  - MID (WALL, o AIR desde abajo): coloca los dos flancos a los lados.
            //  - un FLANCO (AIR "de lado"): 'pos' ya esta puesto por el item; calcula MID y coloca MID + el
            //    otro flanco (asi el panel queda desplazado, pegado al bloque clicado).
            BlockPos mid = midOf(pos, state);
            Part part = state.getValue(PART);
            if (part != Part.MID) {
                level.setBlock(mid, state.setValue(PART, Part.MID), 3);
            }
            if (part != Part.TOP_LEFT) {
                level.setBlock(mid.relative(w.getOpposite()), state.setValue(PART, Part.TOP_LEFT), 3);
            }
            if (part != Part.TOP_RIGHT) {
                level.setBlock(mid.relative(w), state.setValue(PART, Part.TOP_RIGHT), 3);
            }
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // Rompe las otras celdas sin drop (flag 35 = suppress drops); la celda que rompe el jugador
        // dropea 1 item via su loot table (dropSelf) -> exactamente 1 item.
        if (!level.isClientSide) {
            BlockPos mid = midOf(pos, state);
            for (BlockPos p : cellsFromMid(mid, state)) {
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

    /** Modo de montaje: suelo (con poste), pared (pegado) o "de lado" (centrado). */
    public enum Mount implements StringRepresentable {
        FLOOR("floor"),
        WALL("wall"),
        AIR("air");

        private final String name;

        Mount(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
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
