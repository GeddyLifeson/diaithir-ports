package net.diaithir.bestiary.client;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import net.diaithir.bestiary.Creature;
import net.diaithir.bestiary.DiaithirBestiary;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import java.util.List;

@Mod(value = DiaithirBestiary.ID, dist = Dist.CLIENT)
public final class BestiaryClient {
    public BestiaryClient(IEventBus modBus) {
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, e -> {
            for (EntityType<? extends Creature> type : List.of(DiaithirBestiary.BEAST.get(), DiaithirBestiary.FLYER.get(),
                    DiaithirBestiary.MECH.get(), DiaithirBestiary.AVATAR.get(), DiaithirBestiary.STALKER.get()))
                e.registerEntityRenderer(type, BestiaryClient::renderer);
        });
    }

    private static <R extends LivingEntityRenderState & GeoRenderState> GeoEntityRenderer<Creature, R> renderer(EntityRendererProvider.Context context) {
        GeoEntityRenderer<Creature, R> renderer = new GeoEntityRenderer<>(context, new CreatureModel());
        // emissive parts come from textures/entity/<id>_glowmask.png, where one exists
        renderer.withRenderLayer(AutoGlowingGeoLayer::new);
        return renderer;
    }
}
