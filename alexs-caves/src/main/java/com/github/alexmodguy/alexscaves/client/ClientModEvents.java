package com.github.alexmodguy.alexscaves.client;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.particle.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterParticleGroupsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/** Mod-bus client registrations: particles, fluid models and other 26.1 client hooks. */
@EventBusSubscriber(modid = AlexsCaves.MODID, value = Dist.CLIENT)
public class ClientModEvents {

    private static net.minecraft.client.resources.model.sprite.Material mat(String path) {
        return new net.minecraft.client.resources.model.sprite.Material(Identifier.fromNamespaceAndPath(com.github.alexmodguy.alexscaves.AlexsCaves.MODID, path));
    }

    @SubscribeEvent
    public static void registerFluidModels(RegisterFluidModelsEvent event) {
        event.register(new net.minecraft.client.renderer.block.FluidModel.Unbaked(mat("block/acid_still"), mat("block/acid_flowing"), null, null),
                com.github.alexmodguy.alexscaves.server.block.fluid.ACFluidRegistry.ACID_FLUID_SOURCE, com.github.alexmodguy.alexscaves.server.block.fluid.ACFluidRegistry.ACID_FLUID_FLOWING);
        event.register(new net.minecraft.client.renderer.block.FluidModel.Unbaked(mat("block/purple_soda_still"), mat("block/purple_soda_flowing"), null, null),
                com.github.alexmodguy.alexscaves.server.block.fluid.ACFluidRegistry.PURPLE_SODA_FLUID_SOURCE, com.github.alexmodguy.alexscaves.server.block.fluid.ACFluidRegistry.PURPLE_SODA_FLUID_FLOWING);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath(com.github.alexmodguy.alexscaves.AlexsCaves.MODID, "screen_effects"),
                (graphics, delta) -> {
                    if (com.github.alexmodguy.alexscaves.AlexsCaves.PROXY instanceof ClientProxy cp) cp.renderScreenEffects(graphics, delta.getGameTimeDeltaPartialTick(false));
                });
    }

    @SubscribeEvent
    public static void registerSpecialModels(net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent event) {
        event.register(Identifier.fromNamespaceAndPath(com.github.alexmodguy.alexscaves.AlexsCaves.MODID, "legacy_item"), com.github.alexmodguy.alexscaves.client.render.item.LegacyItemSpecialRenderer.CODEC);
    }

    @SubscribeEvent
    public static void registerArmorLayerDefinitions(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions event) {
        com.github.alexmodguy.alexscaves.client.model.layered.ACModelLayers.register(event);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @SubscribeEvent
    public static void addArmorLayers(net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            var r = event.getPlayerRenderer(skin);
            if (r != null) ((net.minecraft.client.renderer.entity.LivingEntityRenderer) r).addLayer(new com.github.alexmodguy.alexscaves.client.render.entity.layer.CustomArmorLayer((net.minecraft.client.renderer.entity.RenderLayerParent) r));
        }
        for (var skin : event.getSkins()) {
            var r = event.getPlayerRenderer(skin);
            if (r != null) ((net.minecraft.client.renderer.entity.LivingEntityRenderer) r).addLayer(new com.github.alexmodguy.alexscaves.client.render.entity.layer.ACPotionEffectLayer((net.minecraft.client.renderer.entity.RenderLayerParent) r));
        }
        for (var type : event.getEntityTypes()) {
            var r0 = event.getRenderer((net.minecraft.world.entity.EntityType) type);
            if (r0 instanceof net.minecraft.client.renderer.entity.LivingEntityRenderer && type != net.minecraft.world.entity.EntityType.ENDER_DRAGON) {
                ((net.minecraft.client.renderer.entity.LivingEntityRenderer) r0).addLayer(new com.github.alexmodguy.alexscaves.client.render.entity.layer.ACPotionEffectLayer((net.minecraft.client.renderer.entity.RenderLayerParent) r0));
            }
            var r = event.getRenderer((net.minecraft.world.entity.EntityType) type);
            if (r instanceof net.minecraft.client.renderer.entity.HumanoidMobRenderer || r instanceof net.minecraft.client.renderer.entity.ArmorStandRenderer) {
                ((net.minecraft.client.renderer.entity.LivingEntityRenderer) r).addLayer(new com.github.alexmodguy.alexscaves.client.render.entity.layer.CustomArmorLayer((net.minecraft.client.renderer.entity.RenderLayerParent) r));
            }
        }
    }

    @SubscribeEvent
    public static void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(com.github.alexmodguy.alexscaves.server.inventory.ACMenuRegistry.SPELUNKERY_TABLE_MENU.get(), com.github.alexmodguy.alexscaves.client.gui.SpelunkeryTableScreen::new);
        event.register(com.github.alexmodguy.alexscaves.server.inventory.ACMenuRegistry.NUCLEAR_FURNACE_MENU.get(), com.github.alexmodguy.alexscaves.client.gui.NuclearFurnaceScreen::new);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @SubscribeEvent
    public static void registerRenderStateModifiers(net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier((Class) net.minecraft.client.renderer.entity.LivingEntityRenderer.class, (java.util.function.BiConsumer) (Object e, Object st) -> com.github.alexmodguy.alexscaves.client.render.entity.layer.ACPotionEffectLayer.extract((net.minecraft.world.entity.LivingEntity) e, (net.minecraft.client.renderer.entity.state.LivingEntityRenderState) st));
    }

    @SubscribeEvent
    public static void registerParticleGroups(RegisterParticleGroupsEvent event) {
        event.register(LegacyParticle.TYPE, LegacyParticle.Group::new);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpecial(ACParticleRegistry.SCARLET_MAGNETIC_ORBIT.get(),
                new MagneticOrbitParticle.ScarletFactory());
        event.registerSpecial(ACParticleRegistry.AZURE_MAGNETIC_ORBIT.get(),
                new MagneticOrbitParticle.AzureFactory());
        event.registerSpecial(ACParticleRegistry.SCARLET_MAGNETIC_FLOW.get(),
                new MagneticFlowParticle.ScarletFactory());
        event.registerSpecial(ACParticleRegistry.AZURE_MAGNETIC_FLOW.get(), new MagneticFlowParticle.AzureFactory());
        event.registerSpecial(ACParticleRegistry.TESLA_BULB_LIGHTNING.get(),
                new TeslaBulbLightningParticle.Factory());
        event.registerSpecial(ACParticleRegistry.MAGNET_LIGHTNING.get(), new MagnetLightningParticle.Factory());
        event.registerSpriteSet(ACParticleRegistry.GALENA_DEBRIS.get(), GalenaDebrisParticle.Factory::new);
        event.registerSpecial(ACParticleRegistry.MAGNETIC_CAVES_AMBIENT.get(),
                new MagneticCavesAmbientParticle.Factory());
        event.registerSpriteSet(ACParticleRegistry.FERROUSLIME.get(), FerrouslimeParticle.Factory::new);
        event.registerSpecial(ACParticleRegistry.QUARRY_BORDER_LIGHTING.get(),
                new QuarryBorderLightningParticle.Factory());
        event.registerSpecial(ACParticleRegistry.AZURE_SHIELD_LIGHTNING.get(),
                new ResistorShieldLightningParticle.AzureFactory());
        event.registerSpecial(ACParticleRegistry.SCARLET_SHIELD_LIGHTNING.get(),
                new ResistorShieldLightningParticle.ScarletFactory());
        event.registerSpriteSet(ACParticleRegistry.FLY.get(), FlyParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.WATER_TREMOR.get(), WaterTremorParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.AMBER_MONOLITH.get(), AmberMonolithParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.AMBER_EXPLOSION.get(), SmallExplosionParticle.AmberFactory::new);
        event.registerSpecial(ACParticleRegistry.DINOSAUR_TRANSFORMATION_AMBER.get(),
                new DinosaurTransformParticle.AmberFactory());
        event.registerSpecial(ACParticleRegistry.DINOSAUR_TRANSFORMATION_TECTONIC.get(),
                new DinosaurTransformParticle.TectonicFactory());
        event.registerSpecial(ACParticleRegistry.STUN_STAR.get(), new StunStarParticle.Factory());
        event.registerSpriteSet(ACParticleRegistry.TEPHRA.get(), TephraParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.TEPHRA_SMALL.get(), TephraParticle.SmallFactory::new);
        event.registerSpriteSet(ACParticleRegistry.TEPHRA_FLAME.get(), TephraParticle.FlameFactory::new);
        event.registerSpriteSet(ACParticleRegistry.LUXTRUCTOSAURUS_SPIT.get(),
                LuxtructosaurusSpitParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.LUXTRUCTOSAURUS_ASH.get(),
                LuxtructosaurusAshParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.HAPPINESS.get(), HappinessParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.ACID_BUBBLE.get(), AcidBubbleParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.BLACK_VENT_SMOKE.get(), VentSmokeParticle.BlackFactory::new);
        event.registerSpriteSet(ACParticleRegistry.WHITE_VENT_SMOKE.get(), VentSmokeParticle.WhiteFactory::new);
        event.registerSpriteSet(ACParticleRegistry.GREEN_VENT_SMOKE.get(), VentSmokeParticle.GreenFactory::new);
        event.registerSpriteSet(ACParticleRegistry.RED_VENT_SMOKE.get(), VentSmokeParticle.RedFactory::new);
        event.registerSpecial(ACParticleRegistry.MUSHROOM_CLOUD.get(), new MushroomCloudParticle.Factory());
        event.registerSpriteSet(ACParticleRegistry.MUSHROOM_CLOUD_SMOKE.get(),
                SmallExplosionParticle.NukeFactory::new);
        event.registerSpriteSet(ACParticleRegistry.MUSHROOM_CLOUD_EXPLOSION.get(),
                SmallExplosionParticle.NukeFactory::new);
        event.registerSpecial(ACParticleRegistry.PROTON.get(), new ProtonParticle.Factory());
        event.registerSpriteSet(ACParticleRegistry.FALLOUT.get(), FalloutParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.GAMMAROACH.get(), GammaroachParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.HAZMAT_BREATHE.get(), HazmatBreatheParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.BLUE_HAZMAT_BREATHE.get(),
                HazmatBreatheParticle.BlueFactory::new);
        event.registerSpriteSet(ACParticleRegistry.RADGILL_SPLASH.get(), RadgillSplashParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.ACID_DROP.get(), AcidDropParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.NUCLEAR_SIREN_SONAR.get(),
                NuclearSirenSonarParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.RAYGUN_EXPLOSION.get(),
                SmallExplosionParticle.RaygunFactory::new);
        event.registerSpriteSet(ACParticleRegistry.BLUE_RAYGUN_EXPLOSION.get(),
                SmallExplosionParticle.BlueRaygunFactory::new);
        event.registerSpriteSet(ACParticleRegistry.RAYGUN_BLAST.get(), RaygunBlastParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.TREMORZILLA_EXPLOSION.get(),
                SmallExplosionParticle.TremorzillaFactory::new);
        event.registerSpriteSet(ACParticleRegistry.TREMORZILLA_RETRO_EXPLOSION.get(),
                SmallExplosionParticle.TremorzillaRetroFactory::new);
        event.registerSpriteSet(ACParticleRegistry.TREMORZILLA_TECTONIC_EXPLOSION.get(),
                SmallExplosionParticle.TremorzillaTectonicFactory::new);
        event.registerSpecial(ACParticleRegistry.TREMORZILLA_PROTON.get(), new TremorzillaProtonParticle.Factory());
        event.registerSpecial(ACParticleRegistry.TREMORZILLA_RETRO_PROTON.get(),
                new TremorzillaProtonParticle.RetroFactory());
        event.registerSpecial(ACParticleRegistry.TREMORZILLA_TECTONIC_PROTON.get(),
                new TremorzillaProtonParticle.TectonicFactory());
        event.registerSpriteSet(ACParticleRegistry.TREMORZILLA_LIGHTNING.get(),
                TremorzillaLightningParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.TREMORZILLA_RETRO_LIGHTNING.get(),
                TremorzillaLightningParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.TREMORZILLA_TECTONIC_LIGHTNING.get(),
                TremorzillaLightningParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.TREMORZILLA_BLAST.get(),
                RaygunBlastParticle.TremorzillaFactory::new);
        event.registerSpriteSet(ACParticleRegistry.TREMORZILLA_STEAM.get(), TremorzillaSteamParticle.Factory::new);
        event.registerSpecial(ACParticleRegistry.TUBE_WORM.get(), new TubeWormParticle.Factory());
        event.registerSpriteSet(ACParticleRegistry.DEEP_ONE_MAGIC.get(), DeepOneMagicParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.WATER_FOAM.get(), WaterFoamParticle.Factory::new);
        event.registerSpecial(ACParticleRegistry.BIG_SPLASH.get(), new BigSplashParticle.Factory());
        event.registerSpriteSet(ACParticleRegistry.BIG_SPLASH_EFFECT.get(), BigSplashEffectParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.MINE_EXPLOSION.get(), SmallExplosionParticle.MineFactory::new);
        event.registerSpriteSet(ACParticleRegistry.BIO_POP.get(), BioPopParticle.Factory::new);
        event.registerSpecial(ACParticleRegistry.WATCHER_APPEARANCE.get(), new WatcherAppearanceParticle.Factory());
        event.registerSpecial(ACParticleRegistry.VOID_BEING_CLOUD.get(), new VoidBeingCloudParticle.Factory());
        event.registerSpecial(ACParticleRegistry.VOID_BEING_TENDRIL.get(), new VoidBeingTendrilParticle.Factory());
        event.registerSpecial(ACParticleRegistry.VOID_BEING_EYE.get(), new VoidBeingEyeParticle.Factory());
        event.registerSpriteSet(ACParticleRegistry.UNDERZEALOT_MAGIC.get(), UnderzealotMagicParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.UNDERZEALOT_EXPLOSION.get(),
                SmallExplosionParticle.UnderzealotFactory::new);
        event.registerSpriteSet(ACParticleRegistry.FALLING_GUANO.get(), FallingGuanoParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.MOTH_DUST.get(), MothDustParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.FORSAKEN_SPIT.get(), ForsakenSpitParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.FORSAKEN_SONAR.get(), ForsakenSonarParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.FORSAKEN_SONAR_LARGE.get(),
                ForsakenSonarParticle.LargeFactory::new);
        event.registerSpriteSet(ACParticleRegistry.TOTEM_EXPLOSION.get(), SmallExplosionParticle.TotemFactory::new);
        event.registerSpriteSet(ACParticleRegistry.ICE_CREAM_DRIP.get(), IceCreamDripParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.ICE_CREAM_SPLASH.get(), IceCreamSplashParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.PURPLE_SODA_BUBBLE.get(), PurpleSodaBubbleParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.PURPLE_SODA_BUBBLE_EMITTER.get(),
                PurpleSodaBubbleEmitterParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.PURPLE_SODA_FIZZ.get(), PurpleSodaFizzParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.SUNDROP.get(), SundropParticle.Factory::new);
        event.registerSpecial(ACParticleRegistry.RAINBOW.get(), new RainbowParticle.Factory());
        event.registerSpecial(ACParticleRegistry.PLAYER_RAINBOW.get(), new PlayerRainbowParticle.Factory());
        event.registerSpriteSet(ACParticleRegistry.CANDICORN_CHARGE.get(), CandicornChargeParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.BIG_BLOCK_DUST.get(), BigBlockDustParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.CARAMEL_DROP.get(), CaramelDropParticle.Factory::new);
        event.registerSpecial(ACParticleRegistry.JELLY_BEAN_EAT.get(), new JellyBeanEatParticle.Factory());
        event.registerSpriteSet(ACParticleRegistry.SLEEP.get(), SleepParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.WITCH_COOKIE.get(), WitchCookieParticle.Factory::new);
        event.registerSpecial(ACParticleRegistry.PURPLE_WITCH_MAGIC.get(), new PurpleWitchMagicParticle.Factory());
        event.registerSpriteSet(ACParticleRegistry.PURPLE_WITCH_EXPLOSION.get(),
                SmallExplosionParticle.PurpleWitchFactory::new);
        event.registerSpriteSet(ACParticleRegistry.GOBTHUMPER.get(), GobthumperParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.COLORED_DUST.get(), ColoredDustParticle.Factory::new);
        event.registerSpriteSet(ACParticleRegistry.SMALL_COLORED_DUST.get(), ColoredDustParticle.SmallFactory::new);
        event.registerSpriteSet(ACParticleRegistry.CONVERSION_CRUCIBLE_EXPLOSION.get(),
                SmallExplosionParticle.ConversionCrucibleFactory::new);
        event.registerSpriteSet(ACParticleRegistry.FROSTMINT_EXPLOSION.get(),
                SmallExplosionParticle.FrostmintFactory::new);
        event.registerSpriteSet(ACParticleRegistry.SUGAR_FLAKE.get(), SugarFlakeParticle.Factory::new);
    }
}
