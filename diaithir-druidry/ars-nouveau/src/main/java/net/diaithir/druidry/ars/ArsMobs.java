package net.diaithir.druidry.ars;

import net.diaithir.druidry.ars.api.registry.FamiliarRegistry;
import net.diaithir.druidry.ars.api.registry.GlyphRegistry;
import net.diaithir.druidry.ars.common.spell.effect.*;
import net.diaithir.druidry.ars.api.registry.JarBehaviorRegistry;
import net.diaithir.druidry.ars.api.registry.RitualRegistry;
import net.diaithir.druidry.ars.common.familiars.*;
import net.diaithir.druidry.ars.common.mob_jar.*;
import net.diaithir.druidry.ars.common.ritual.*;
import net.diaithir.druidry.ars.setup.registry.ModEntities;
import net.minecraft.world.entity.EntityType;

/** Registers the rituals, familiars and mob-jar behaviours (the entity side of the mod). */
public final class ArsMobs {
    private ArsMobs() {
    }

    /** Before the item registry event: ritual tablets and familiar scripts are built from these maps. */
    public static void registerRitualsAndFamiliars() {
        RitualRegistry.registerRitual(new RitualDig());
        RitualRegistry.registerRitual(new RitualMoonfall());
        RitualRegistry.registerRitual(new RitualCloudshaper());
        RitualRegistry.registerRitual(new RitualSunrise());
        RitualRegistry.registerRitual(new RitualDisintegration());
        RitualRegistry.registerRitual(new RitualPillagerRaid());
        RitualRegistry.registerRitual(new RitualOvergrowth());
        RitualRegistry.registerRitual(new RitualBreed());
        RitualRegistry.registerRitual(new RitualHealing());
        RitualRegistry.registerRitual(new RitualWarp());
        RitualRegistry.registerRitual(new RitualFlight());
        RitualRegistry.registerRitual(new RitualGravity());
        RitualRegistry.registerRitual(new RitualWildenSummoning());
        RitualRegistry.registerRitual(new RitualAnimalSummoning());
        RitualRegistry.registerRitual(new RitualBinding());
        RitualRegistry.registerRitual(new RitualAwakening());
        RitualRegistry.registerRitual(new RitualHarvest());
        RitualRegistry.registerRitual(new RitualMobCapture());
        RitualRegistry.registerRitual(new ConjurePlainsRitual());
        RitualRegistry.registerRitual(new ForestationRitual());
        RitualRegistry.registerRitual(new FloweringRitual());
        RitualRegistry.registerRitual(new ConjureDesertRitual());
        RitualRegistry.registerRitual(new DenySpawnRitual());

        FamiliarRegistry.registerFamiliar(new StarbuncleFamiliarHolder());
        FamiliarRegistry.registerFamiliar(new DrygmyFamiliarHolder());
        FamiliarRegistry.registerFamiliar(new WhirlisprigFamiliarHolder());
        FamiliarRegistry.registerFamiliar(new WixieFamiliarHolder());
        FamiliarRegistry.registerFamiliar(new AmethystFamiliarHolder());
    }

    /** Summon glyphs; called next to ArsGlyphs.register(). */
    public static void registerGlyphs() {
        GlyphRegistry.registerSpell(EffectSummonVex.INSTANCE);
        GlyphRegistry.registerSpell(EffectSummonWolves.INSTANCE);
        GlyphRegistry.registerSpell(EffectSummonSteed.INSTANCE);
        GlyphRegistry.registerSpell(EffectSummonDecoy.INSTANCE);
        GlyphRegistry.registerSpell(EffectSummonUndead.INSTANCE);
        GlyphRegistry.registerSpell(EffectAnimate.INSTANCE);
    }

    /** Common setup: mob jar behaviours (entity types of the mod are resolvable by now). */
    public static void registerJarBehaviors() {
        JarBehaviorRegistry.register(EntityType.ELDER_GUARDIAN, new ElderGuardianBehavior());
        JarBehaviorRegistry.register(EntityType.CREEPER, new CreeperBehavior());
        JarBehaviorRegistry.register(EntityType.CHICKEN, new ChickenBehavior());
        JarBehaviorRegistry.register(EntityType.VILLAGER, new VillagerBehavior());
        JarBehaviorRegistry.register(EntityType.SHEEP, new SheepBehavior());
        JarBehaviorRegistry.register(EntityType.FROG, new FrogBehavior());
        JarBehaviorRegistry.register(EntityType.PIGLIN, new PiglinBehavior());
        JarBehaviorRegistry.register(EntityType.GHAST, new GhastBehavior());
        JarBehaviorRegistry.register(EntityType.SQUID, new SquidBehavior());
        JarBehaviorRegistry.register(EntityType.GLOW_SQUID, new GlowSquidBehavior());
        JarBehaviorRegistry.register(EntityType.BLAZE, new BlazeBehavior());
        JarBehaviorRegistry.register(EntityType.PANDA, new PandaBehavior());
        JarBehaviorRegistry.register(EntityType.MOOSHROOM, new MooshroomBehavior());
        JarBehaviorRegistry.register(EntityType.ENDER_DRAGON, new DragonBehavior());
        JarBehaviorRegistry.register(EntityType.PUFFERFISH, new PufferfishBehavior());
        JarBehaviorRegistry.register(EntityType.ALLAY, new AllayBehavior());
        JarBehaviorRegistry.register(ModEntities.ENTITY_DUMMY.get(), new DecoyBehavior());
        JarBehaviorRegistry.register(EntityType.ITEM, new net.diaithir.druidry.ars.api.mob_jar.JarBehavior<>() {
            @Override
            public void use(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand handIn, net.minecraft.world.phys.BlockHitResult hit, net.diaithir.druidry.ars.common.block.tile.MobJarTile tile) {
                if (player.isShiftKeyDown()) {
                    tile.onDispel(player);
                }
            }
        });
        JarBehaviorRegistry.register(EntityType.WITHER, new WitherBehavior());
    }
}
