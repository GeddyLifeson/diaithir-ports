package net.diaithir.druidry.ars.common.event;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.ArsNouveauAPI;
import net.diaithir.druidry.ars.api.event.DispelEvent;
import net.diaithir.druidry.ars.api.event.EventQueue;
import net.diaithir.druidry.ars.api.event.ITimedEvent;
import net.diaithir.druidry.ars.api.perk.PerkAttributes;
import net.diaithir.druidry.ars.api.registry.*;
import net.diaithir.druidry.ars.api.util.BlockUtil;
import net.diaithir.druidry.ars.api.util.CuriosUtil;
import net.diaithir.druidry.ars.api.util.PerkUtil;
import net.diaithir.druidry.ars.client.ClientInfo;
import net.diaithir.druidry.ars.client.particle.ParticleUtil;
import net.diaithir.druidry.ars.common.crafting.recipes.DispelEntityRecipe;
import net.diaithir.druidry.ars.common.datagen.ItemTagProvider;
import net.diaithir.druidry.ars.common.entity.EnchantedFallingBlock;
import net.diaithir.druidry.ars.common.entity.debug.FixedStack;
import net.diaithir.druidry.ars.common.items.RitualTablet;
import net.diaithir.druidry.ars.common.items.VoidJar;
import net.diaithir.druidry.ars.common.lib.PotionEffectTags;
import net.diaithir.druidry.ars.common.network.Networking;
import net.diaithir.druidry.ars.common.network.PacketJoinedServer;
import net.diaithir.druidry.ars.common.network.PotionSyncPacket;
import net.diaithir.druidry.ars.common.perk.JumpHeightPerk;
import net.diaithir.druidry.ars.common.spell.effect.EffectGlide;
import net.diaithir.druidry.ars.setup.config.Config;
import net.diaithir.druidry.ars.setup.registry.*;
import net.diaithir.druidry.ars.setup.reward.Rewards;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockGrowFeatureEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.*;


@EventBusSubscriber(modid = ArsNouveau.HOST)
public class EventHandler {

    /**
     * Port note: AddReloadListenerEvent no longer exists in 26.1; the data caches are rebuilt once the server has started.
     * TODO: also rebuild on /reload through an AddServerReloadListenersEvent listener key.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void serverStartedReload(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
        var recipes = event.getServer().getRecipeManager();
        GenericRecipeRegistry.reloadAll(recipes);
        BuddingConversionRegistry.reloadBuddingConversionRecipes(recipes);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void itemPickupEvent(ItemEntityPickupEvent.Pre event) {
        Player player = event.getPlayer();
        ItemStack pickingUp = event.getItemEntity().getItem();
        VoidJar.tryVoiding(player, pickingUp);
    }

    @SubscribeEvent
    public static void shieldEvent(LivingShieldBlockEvent e) {
        if (!e.getEntity().level().isClientSide() && e.getEntity() instanceof Player player && player.isBlocking()) {
            if (player.getUseItem().getItem() == ItemsRegistry.ENCHANTERS_SHIELD.asItem()) {
                player.addEffect(new MobEffectInstance(ModPotions.MANA_REGEN_EFFECT, 200, 1));
                player.addEffect(new MobEffectInstance(ModPotions.SPELL_DAMAGE_EFFECT, 200, 1));
            }
        }
    }

    @SubscribeEvent
    public static void livingHurtEvent(LivingDamageEvent.Post e) {
        if (e.getEntity().level().isClientSide())
            return;
        if (e.getSource().getEntity() instanceof LivingEntity livingUser) {
            if (livingUser instanceof Player)
                return;
        }
    }


    @SubscribeEvent
    public static void jumpEvent(LivingEvent.LivingJumpEvent e) {
        e.getEntity();
        if (e.getEntity().hasEffect(ModPotions.SNARE_EFFECT)) {
            e.getEntity().setDeltaMovement(0, 0, 0);
            return;
        }
    }


    @SubscribeEvent
    public static void playerLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity().level().isClientSide())
            return;
        if (e.getEntity() instanceof ServerPlayer serverPlayer) {
            boolean isContributor = Rewards.CONTRIBUTORS.contains(serverPlayer.getUUID());
            if (isContributor) {
                Networking.sendToPlayerClient(new PacketJoinedServer(true), (ServerPlayer) e.getEntity());
            }
        }
        CompoundTag tag = e.getEntity().getPersistentData().getCompoundOrEmpty(Player.PERSISTED_NBT_TAG);
        String book_tag = "an_book_";
        if (!tag.getBooleanOr(book_tag, false) && Config.SPAWN_BOOK.get()) {
            Player entity = e.getEntity();
            tag.putBoolean(book_tag, true);
            e.getEntity().getPersistentData().put(Player.PERSISTED_NBT_TAG, tag);
        }
    }


    @SubscribeEvent
    public static void clientTickEnd(ClientTickEvent.Post event) {

        ClientInfo.ticksInGame++;
        if (ClientInfo.redTicks()) {
            ClientInfo.redOverlayTicks--;
        }

    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGlideTick(PlayerTickEvent.Pre event) {
        var player = event.getEntity();
        if (player.hasEffect(ModPotions.FLIGHT_EFFECT)
                && player.level().getGameTime() % 20 == 0
                && player.getEffect(ModPotions.FLIGHT_EFFECT).getDuration() <= 30 * 20
                && player instanceof ServerPlayer serverPlayer) {
            // TODO ritual flight refresh
        }

    }

    @SubscribeEvent
    public static void entityHurt(LivingDamageEvent.Pre e) {
        var container = e.getContainer();
        var source = container.getSource();
        var amount = container.getNewDamage();
        if (e.getEntity().hasEffect(ModPotions.DEFENCE_EFFECT) && (source.is(DamageTypes.MAGIC) || source.is(DamageTypes.GENERIC) || source.is(DamageTypes.MOB_ATTACK))) {
            if (amount > 0.5) {
                container.setNewDamage((float) Math.max(0.5, amount - 1.0f - e.getEntity().getEffect(ModPotions.DEFENCE_EFFECT).getAmplifier()));
            }
        }

        if (source.is(DamageTypes.LIGHTNING_BOLT) && e.getEntity().hasEffect(ModPotions.SHOCKED_EFFECT)) {
            float damage = amount + 3.0f + 3.0f * e.getEntity().getEffect(ModPotions.SHOCKED_EFFECT).getAmplifier();
            container.setNewDamage(Math.max(0, damage));
        }
        LivingEntity entity = e.getEntity();
        if (entity.hasEffect(ModPotions.HEX_EFFECT)
                && (entity.hasEffect(MobEffects.POISON)
                || entity.hasEffect(MobEffects.WITHER)
                || entity.isOnFire()
                || entity.hasEffect(ModPotions.SHOCKED_EFFECT)
                || entity.getTicksFrozen() >= entity.getTicksRequiredToFreeze())) {
            container.setNewDamage(amount + 0.5f + 0.33f * entity.getEffect(ModPotions.HEX_EFFECT).getAmplifier());
        }
        double warding = PerkUtil.valueOrZero(entity, PerkAttributes.WARDING);
        double feather = PerkUtil.valueOrZero(entity, PerkAttributes.FEATHER);
        if (source.is(Tags.DamageTypes.IS_MAGIC)) {
            container.setNewDamage((float) (amount - warding));
        }

        if (source.is(DamageTypes.FALL)) {
            container.setNewDamage((float) (amount - (amount * feather)));
        }
    }

    @SubscribeEvent
    public static void fallEvent(LivingFallEvent fallEvent) {
        double jumpBonus = PerkUtil.countForPerk(JumpHeightPerk.INSTANCE, fallEvent.getEntity());
        fallEvent.setDistance((float) (fallEvent.getDistance() - (jumpBonus / 0.1)));
    }

    @SubscribeEvent
    public static void entityHeal(LivingHealEvent e) {
        LivingEntity entity = e.getEntity();
        if (entity.hasEffect(ModPotions.HEX_EFFECT)) {
            e.setAmount(e.getAmount() / 2.0f);
        }

        if (entity.hasEffect(ModPotions.RECOVERY_EFFECT)) {
            e.setAmount(e.getAmount() + 1 + entity.getEffect(ModPotions.RECOVERY_EFFECT).getAmplifier());
        }
    }

    @SubscribeEvent
    public static void eatEvent(LivingEntityUseItemEvent.Finish event) {
        if (!event.getEntity().level().isClientSide() && event.getItem().get(net.minecraft.core.component.DataComponents.FOOD) != null) {
            if (event.getEntity() instanceof Player player) {
                FoodData stats = player.getFoodData();
                stats.setSaturation((float) (stats.getSaturationLevel() * PerkUtil.perkValue(player, PerkAttributes.WHIRLIESPRIG)));
            }
        }
    }

    private static void replaceEntityWithItems(ServerLevel level, Entity entity, ItemStack... items) {
        entity.remove(Entity.RemovalReason.KILLED);
        ParticleUtil.spawnPoof(level, entity.blockPosition());
        for (ItemStack item : items) {
            level.addFreshEntity(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), item));
        }
    }

    @SubscribeEvent
    public static void dispelEvent(DispelEvent event) {
        if (event.rayTraceResult instanceof EntityHitResult hit && event.world instanceof ServerLevel level) {
            Entity entity = hit.getEntity();
            if (!entity.isAlive()) return;
            // TODO: Replace with EntitySubPredicate when it becomes a registry in 1.21
            if (entity instanceof Witch witch) {
                if (witch.getHealth() <= witch.getMaxHealth() / 2) {
                    replaceEntityWithItems(level, witch, new ItemStack(ItemsRegistry.WIXIE_SHARD));
                    return;
                }
            }
            for (RecipeHolder<DispelEntityRecipe> holder : net.diaithir.druidry.ars.common.util.PortUtil.recipes(RecipeRegistry.DISPEL_ENTITY_TYPE.get())) {
                var recipe = holder.value();
                if (recipe.matches(event.shooter, entity)) {
                    replaceEntityWithItems(level, entity, recipe.result(event.shooter, entity).toArray(ItemStack[]::new));
                    return;
                }
            }
        }
    }

    //TODO: restore looting level event

//    @SubscribeEvent
//    public static void onLootingEvent(LootingLevelEvent event) {
//        if (event.getDamageSource() != null && event.getDamageSource().getEntity() instanceof LivingEntity living) {
//            event.setLootingLevel(event.getLootingLevel() + Math.round(PerkUtil.countForPerk(LootingPerk.INSTANCE, living)));
//        }
//    }

    @SubscribeEvent
    public static void onPotionAdd(MobEffectEvent.Added event) {
        LivingEntity target = event.getEntity();
        Entity applier = event.getEffectSource();
        if (target.level().isClientSide())
            return;
        double bonus = 0.0;
        Holder<MobEffect> holder = event.getEffectInstance().getEffect();
        MobEffect effect = holder.value();
        if (effect.isBeneficial()) {
            bonus = PerkUtil.valueOrZero(target, PerkAttributes.WIXIE);
        } else if (applier instanceof LivingEntity living) {
            bonus = PerkUtil.valueOrZero(living, PerkAttributes.WIXIE);
        }

        if (bonus > 0.0) {
            // TODO: MobEffectInstance.duration is private in 26.1; scale via a new instance
            event.getEffectInstance().update(new MobEffectInstance(holder, (int) (event.getEffectInstance().getDuration() * bonus), event.getEffectInstance().getAmplifier(), event.getEffectInstance().isAmbient(), event.getEffectInstance().isVisible(), event.getEffectInstance().showIcon()));
        }

        if (holder.is(PotionEffectTags.TO_SYNC)) {
            Networking.sendToNearbyClient(target.level(), target, new PotionSyncPacket(target.getId(), effect, event.getEffectInstance().getDuration()));
        }
    }

    @SubscribeEvent
    public static void onPotionRemove(MobEffectEvent.Remove event) {
        syncPotionRemoval(event);
    }

    @SubscribeEvent
    public static void onPotionExpire(MobEffectEvent.Expired event) {
        syncPotionRemoval(event);
    }

    private static void syncPotionRemoval(MobEffectEvent event) {
        if (event.getEntity() instanceof LivingEntity && event.getEffectInstance() != null && !event.getEntity().level().isClientSide()) {
            LivingEntity target = event.getEntity();
            Holder<MobEffect> holder = event.getEffectInstance().getEffect();
            MobEffect effect = holder.value();
            if (holder.is(PotionEffectTags.TO_SYNC)) {
                Networking.sendToNearbyClient(target.level(), target, new PotionSyncPacket(target.getId(), effect, -1));
            }
        }
    }

    @SubscribeEvent
    public static void endDupePatch(EntityTravelToDimensionEvent event) {
        if (event.getDimension() == Level.END) {
            if (event.getEntity() instanceof EnchantedFallingBlock) {
                event.setCanceled(true);
            }
        }
    }

    private EventHandler() {
    }

}
