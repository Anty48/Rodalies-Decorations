package rodalies;

import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import rodalies.client.HectometerSignRenderer;
import rodalies.client.OffsetSignModel;
import rodalies.client.RailSignRenderer;
import rodalies.client.SlidingTrainDoorRenderer;
import rodalies.client.StationSignalRenderer;
import rodalies.client.TrainMaxSpeedRenderer;

import java.util.Map;

/**
 * Configuracion solo-cliente (port a NeoForge 1.21.1). Registra los renderers de BlockEntity.
 *
 * Nota: el render layer translucido del cristal del parabrisas ya NO se asigna aqui (NeoForge quito el
 * helper {@code ItemBlockRenderTypes.setRenderLayer}); ahora se declara en el JSON del modelo del bloque
 * con {@code "render_type": "minecraft:translucent"} (parabrisas y ventana de tren). El bloque conserva
 * {@code .noOcclusion()}.
 */
@EventBusSubscriber(modid = "rodalies", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.STATION_SIGNAL_BE.get(), StationSignalRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RAIL_SIGN_BE.get(), RailSignRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TRAIN_MAX_SPEED_BE.get(), TrainMaxSpeedRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HECTOMETER_BE.get(), HectometerSignRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SLIDING_TRAIN_DOOR_BE.get(), SlidingTrainDoorRenderer::new);
    }

    /**
     * Señales de pared: cada estado con offset > 0 usa su modelo envuelto en {@link OffsetSignModel},
     * que lo desplaza hacia el soporte (offset en medios pixeles) sin necesitar un JSON por distancia.
     */
    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        for (DeferredHolder<Block, ? extends Block> entry : ModBlocks.BLOCKS.getEntries()) {
            Block block = entry.get();
            if (!(block instanceof RailSignWallBlock)) {
                continue;
            }
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                int offset = state.getValue(RailSignWallBlock.OFFSET);
                if (offset == 0) {
                    continue;
                }
                Direction toSupport = state.getValue(RailSignWallBlock.FACING).getOpposite();
                float dist = offset / 32f;
                ModelResourceLocation mrl = BlockModelShaper.stateToModelLocation(state);
                models.computeIfPresent(mrl, (k, model) ->
                        new OffsetSignModel(model, toSupport.getStepX() * dist, toSupport.getStepZ() * dist));
            }
        }
    }
}
