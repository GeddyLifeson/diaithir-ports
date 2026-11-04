package net.diaithir.druidry.ars.client;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.client.renderer.ArsMobRenderer;
import net.diaithir.druidry.ars.client.renderer.ArsMobRenderer.Profile;
import net.diaithir.druidry.ars.setup.registry.ModEntities;
import net.minecraft.client.renderer.entity.EvokerFangsRenderer;
import net.minecraft.client.renderer.entity.HorseRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.VexRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Client renderers for the restored mobs. GeckoLib is not available and the pack's animation module is not built yet, so every mob is drawn
 * with a procedural placeholder model (no assets); summoned vanilla-based mobs reuse the vanilla renderers.
 */
@EventBusSubscriber(modid = ArsNouveau.HOST, value = Dist.CLIENT)
public final class ArsClientEntities {
    private ArsClientEntities() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.STARBUNCLE_TYPE.get(), ArsMobRenderer.of(Profile.QUADRUPED, "orange_wool"));
        event.registerEntityRenderer(ModEntities.GIFT_STARBY.get(), ArsMobRenderer.of(Profile.QUADRUPED, "yellow_wool"));
        event.registerEntityRenderer(ModEntities.ENTITY_DRYGMY.get(), ArsMobRenderer.of(Profile.QUADRUPED, "brown_wool"));
        event.registerEntityRenderer(ModEntities.WHIRLISPRIG_TYPE.get(), ArsMobRenderer.of(Profile.FLOATING, "lime_wool"));
        event.registerEntityRenderer(ModEntities.ENTITY_WIXIE_TYPE.get(), ArsMobRenderer.of(Profile.FLOATING, "magenta_wool"));
        event.registerEntityRenderer(ModEntities.AMETHYST_GOLEM.get(), ArsMobRenderer.of(Profile.BIPED, "amethyst_block"));
        event.registerEntityRenderer(ModEntities.ENTITY_CASCADING_WEALD.get(), ArsMobRenderer.of(Profile.LARGE, "cyan_wool"));
        event.registerEntityRenderer(ModEntities.ENTITY_FLOURISHING_WEALD.get(), ArsMobRenderer.of(Profile.LARGE, "green_wool"));
        event.registerEntityRenderer(ModEntities.ENTITY_BLAZING_WEALD.get(), ArsMobRenderer.of(Profile.LARGE, "orange_wool"));
        event.registerEntityRenderer(ModEntities.ENTITY_VEXING_WEALD.get(), ArsMobRenderer.of(Profile.LARGE, "gray_wool"));
        event.registerEntityRenderer(ModEntities.WILDEN_HUNTER.get(), ArsMobRenderer.of(Profile.BIPED, "brown_wool"));
        event.registerEntityRenderer(ModEntities.WILDEN_STALKER.get(), ArsMobRenderer.of(Profile.FLOATING, "red_wool"));
        event.registerEntityRenderer(ModEntities.WILDEN_GUARDIAN.get(), ArsMobRenderer.of(Profile.LARGE, "white_wool"));
        event.registerEntityRenderer(ModEntities.WILDEN_BOSS.get(), ArsMobRenderer.of(Profile.LARGE, "black_wool"));
        event.registerEntityRenderer(ModEntities.ENTITY_DUMMY.get(), ArsMobRenderer.of(Profile.BIPED, "light_gray_wool"));
        event.registerEntityRenderer(ModEntities.ANIMATED_BLOCK.get(), ArsMobRenderer.of(Profile.BIPED, "stone"));
        event.registerEntityRenderer(ModEntities.ANIMATED_HEAD.get(), ArsMobRenderer.of(Profile.BIPED, "stone"));
        event.registerEntityRenderer(ModEntities.LILY.get(), ArsMobRenderer.of(Profile.QUADRUPED, "pink_wool"));

        event.registerEntityRenderer(ModEntities.ENTITY_FAMILIAR_STARBUNCLE.get(), ArsMobRenderer.of(Profile.QUADRUPED, "orange_wool"));
        event.registerEntityRenderer(ModEntities.ENTITY_FAMILIAR_DRYGMY.get(), ArsMobRenderer.of(Profile.QUADRUPED, "brown_wool"));
        event.registerEntityRenderer(ModEntities.ENTITY_FAMILIAR_SYLPH.get(), ArsMobRenderer.of(Profile.FLOATING, "lime_wool"));
        event.registerEntityRenderer(ModEntities.ENTITY_FAMILIAR_WIXIE.get(), ArsMobRenderer.of(Profile.FLOATING, "magenta_wool"));
        event.registerEntityRenderer(ModEntities.FAMILIAR_AMETHYST_GOLEM.get(), ArsMobRenderer.of(Profile.BIPED, "amethyst_block"));

        event.registerEntityRenderer(ModEntities.SUMMON_SKELETON.get(), SkeletonRenderer::new);
        event.registerEntityRenderer(ModEntities.SUMMON_WOLF.get(), WolfRenderer::new);
        event.registerEntityRenderer(ModEntities.SUMMON_HORSE.get(), HorseRenderer::new);
        event.registerEntityRenderer(ModEntities.ALLY_VEX.get(), VexRenderer::new);
        event.registerEntityRenderer(ModEntities.ENTITY_EVOKER_FANGS_ENTITY_TYPE.get(), EvokerFangsRenderer::new);

        event.registerEntityRenderer(ModEntities.ENTITY_RITUAL.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.ENTITY_CHIMERA_SPIKE.get(), NoopRenderer::new);
    }
}
