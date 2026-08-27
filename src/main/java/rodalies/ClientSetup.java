package rodalies;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import rodalies.client.RailSignRenderer;
import rodalies.client.StationSignalRenderer;

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
        });
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Renderer que dibuja el texto grande del cartel de estacion.
        event.registerBlockEntityRenderer(ModBlockEntities.STATION_SIGNAL_BE.get(), StationSignalRenderer::new);
        // Renderer del texto de las señales ferroviarias (via, velocidad, LVT, cartel).
        event.registerBlockEntityRenderer(ModBlockEntities.RAIL_SIGN_BE.get(), RailSignRenderer::new);
    }
}
