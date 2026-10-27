package net.diaithir.woodvariants;

import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = DiaithirWoodVariants.ID, value = Dist.CLIENT)
public final class SeatClient {
    private SeatClient() {}

    /** The native (Diaithiris) name as a grey second line, from the lang key native.<ns>.<id>. */
    @SubscribeEvent
    public static void tooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event) {
        var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (DiaithirWoodVariants.ID.equals(id.getNamespace())) {
            event.getToolTip().add(1, net.minecraft.network.chat.Component.translatable("native." + id.getNamespace() + "." + id.getPath())
                .withStyle(net.minecraft.ChatFormatting.GRAY));
        }
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(DiaithirWoodVariants.SEAT.get(), NoopRenderer::new);
    }
}
