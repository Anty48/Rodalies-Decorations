package rodalies;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;
import rodalies.client.HectometerSignRenderer;
import rodalies.client.OffsetSignModel;
import rodalies.client.RailSignRenderer;
import rodalies.client.SlidingTrainDoorRenderer;
import rodalies.client.StationSignalRenderer;
import rodalies.client.TrainMaxSpeedRenderer;

import java.util.Map;

/**
 * Configuracion solo-cliente. Aqui asignamos el render layer de los bloques que lo necesitan
 * y registramos los renderers de BlockEntity.
 */
@Mod.EventBusSubscriber(modid = "rodalies", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // El cristal tintado necesita render translucido; sin esto Forge trata el bloque
            // como solido y "recorta" lo que hay detras.
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.PARABRISAS_447.get(), RenderType.translucent());
            // Ventana de tren: cristal tintado semitransparente -> capa translucida.
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.TRAIN_WINDOW.get(), RenderType.translucent());
            // Las puertas correderas se dibujan siempre por su BER (nunca en el mesh del chunk), asi que no
            // necesitan render layer aqui; el BER ya pinta el cristal en la capa translucida de bloque.
        });
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Renderer que dibuja el texto grande del cartel de estacion.
        event.registerBlockEntityRenderer(ModBlockEntities.STATION_SIGNAL_BE.get(), StationSignalRenderer::new);
        // Renderer del texto de las señales ferroviarias (via, velocidad, LVT, cartel).
        event.registerBlockEntityRenderer(ModBlockEntities.RAIL_SIGN_BE.get(), RailSignRenderer::new);
        // Texto (<=4) del bloque de velocidad maxima, centrado en el rombo.
        event.registerBlockEntityRenderer(ModBlockEntities.TRAIN_MAX_SPEED_BE.get(), TrainMaxSpeedRenderer::new);
        // Dos filas de texto de la señal de hectometro.
        event.registerBlockEntityRenderer(ModBlockEntities.HECTOMETER_BE.get(), HectometerSignRenderer::new);
        // Anima el deslizamiento de las hojas de la puerta en el mundo (con VISIBLE=false).
        event.registerBlockEntityRenderer(ModBlockEntities.SLIDING_TRAIN_DOOR_BE.get(), SlidingTrainDoorRenderer::new);
    }

    /**
     * Señales de pared: cada estado con offset > 0 usa su modelo envuelto en {@link OffsetSignModel},
     * que lo desplaza hacia el soporte (offset en medios pixeles) sin necesitar un JSON por distancia.
     */
    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ResourceLocation, BakedModel> models = event.getModels();
        for (RegistryObject<Block> entry : ModBlocks.BLOCKS.getEntries()) {
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
