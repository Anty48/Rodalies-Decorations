package rodalies;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Configuracion solo-cliente. Aqui asignamos el render layer de los bloques que lo necesitan.
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
}
