package rodalies;

import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Item de una señal ferroviaria con poste que, igual que el cartel de estacion, cambia a modo PARED
 * si se hace clic en la cara de un bloque mirandola de frente: entonces coloca su {@link RailSignWallBlock}
 * (la señal sin poste pegada a ese bloque). En cualquier otro caso coloca la señal con poste normal.
 * El bloque de pared queda asociado a este item: al romperlo o con pick-block devuelve este item.
 */
public class RailSignItem extends BlockItem {

    private final Block wallBlock;

    public RailSignItem(Block block, Block wallBlock, Properties properties) {
        super(block, properties);
        this.wallBlock = wallBlock;
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        if (face.getAxis().isHorizontal() && face.getAxis() == context.getHorizontalDirection().getAxis()) {
            BlockState wall = wallBlock.getStateForPlacement(context);
            return wall != null && canPlace(context, wall) ? wall : null;
        }
        return super.getPlacementState(context);
    }

    @Override
    public void registerBlocks(Map<Block, Item> blockToItemMap, Item item) {
        super.registerBlocks(blockToItemMap, item);
        blockToItemMap.put(wallBlock, item);
    }

    @Override
    public void removeFromBlockToItemMap(Map<Block, Item> blockToItemMap, Item item) {
        super.removeFromBlockToItemMap(blockToItemMap, item);
        blockToItemMap.remove(wallBlock);
    }
}
