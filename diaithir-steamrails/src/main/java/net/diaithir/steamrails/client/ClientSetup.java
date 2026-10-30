package net.diaithir.steamrails.client;

import com.zurrtum.create.AllBogeyStyles;
import com.zurrtum.create.client.AllBlockEntityRenders;
import com.zurrtum.create.client.AllBogeyStyleRenders;
import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityRenderer;
import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityVisual;
import com.zurrtum.create.client.content.trains.bogey.SizeRenderer;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import net.diaithir.steamrails.SteamBlocks;
import net.diaithir.steamrails.SteamRails;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Client side: draws the steam bogeys through Create's own bogey renderer and visual, with our parts. Not covered by the self test (it needs a screen). */
@EventBusSubscriber(modid = SteamRails.ID, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {}

    static {
        // the models are asked for by name here, while the mod is built, ahead of Create's model resolve
        SteamBogeyClient.DRIVER.wheel();
    }

    @SubscribeEvent
    static void setup(FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            AllBlockEntityRenders.visual(SteamBlocks.BOGEY_BE.get(), BogeyBlockEntityRenderer::new, BogeyBlockEntityVisual::new);
            BogeyStyle driver = AllBogeyStyles.BOGEY_STYLES.get(Identifier.fromNamespaceAndPath(SteamRails.ID, "driver"));
            BogeyStyle pony = AllBogeyStyles.BOGEY_STYLES.get(Identifier.fromNamespaceAndPath(SteamRails.ID, "pony"));
            AllBogeyStyleRenders.register(driver, () -> new SizeRenderer(new SteamBogeyClient.Renderer(SteamBogeyClient.DRIVER),
                    (ctx, partial, inContraption) -> new SteamBogeyClient.Visual(SteamBogeyClient.DRIVER, ctx, partial, inContraption)));
            AllBogeyStyleRenders.register(pony, () -> new SizeRenderer(new SteamBogeyClient.Renderer(SteamBogeyClient.PONY),
                    (ctx, partial, inContraption) -> new SteamBogeyClient.Visual(SteamBogeyClient.PONY, ctx, partial, inContraption)));
        });
    }
}
