package io.github.tr100000.researcher;

import io.github.tr100000.researcher.api.trigger.TriggerHandler;
import io.github.tr100000.researcher.api.trigger.TriggerHandlerRegistry;
import io.github.tr100000.researcher.impl.criterion.AnyBlockUseTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.BeeNestDestroyedTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.BlockBrokenTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.BredAnimalsTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.BrewedPotionTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.ChangedDimensionTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.ChanneledLightningTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.ConstructBeaconTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.ConsumeItemTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.CuredZombieVillagerTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.DefaultBlockUseTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.EffectsChangedTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.EnchantedItemTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.EnterBlockTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.EntityHurtPlayerTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.ErrorTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.FallAfterExplosionTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.FilledBucketTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.FishingRodHookedTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.InventoryChangedTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.ItemCraftedTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.ItemDurabilityChangedTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.ItemTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.KilledByArrowTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.LevitationTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.LightningStrikeTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.OnKilledTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.PlayerGeneratesContainerLootTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.PlayerHurtEntityTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.PlayerInteractedWithEntityTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.RecipeCraftedTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.RecipeUnlockedTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.ResearchItemsTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.ResearchUnlockedTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.ShotCrossbowTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.SlideDownBlockTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.SpearMobsTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.StartedRidingTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.SummonedEntityTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.TameAnimalTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.TargetHitTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.ThrownItemPickedUpByEntityTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.TickTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.TradeTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.TravelTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.UsedEnderEyeTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.UsedTotemTriggerHandler;
import io.github.tr100000.researcher.impl.criterion.UsingItemTriggerHandler;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import org.jspecify.annotations.NullMarked;

import java.util.function.Supplier;

@NullMarked
public final class ResearcherTriggerHandlers {
    private ResearcherTriggerHandlers() {}

    public static void register() {
        register(CriteriaTriggers.IMPOSSIBLE, () -> ErrorTriggerHandler.IMPOSSIBLE);
        register(CriteriaTriggers.PLAYER_KILLED_ENTITY, () -> OnKilledTriggerHandler.PLAYER_KILLED_ENTITY);
        register(CriteriaTriggers.ENTITY_KILLED_PLAYER, () -> OnKilledTriggerHandler.ENTITY_KILLED_PLAYER);
        register(CriteriaTriggers.ENTER_BLOCK, EnterBlockTriggerHandler::new);
        register(CriteriaTriggers.INVENTORY_CHANGED, InventoryChangedTriggerHandler::new);
        register(CriteriaTriggers.RECIPE_UNLOCKED, RecipeUnlockedTriggerHandler::new);
        register(CriteriaTriggers.PLAYER_HURT_ENTITY, PlayerHurtEntityTriggerHandler::new);
        register(CriteriaTriggers.ENTITY_HURT_PLAYER, EntityHurtPlayerTriggerHandler::new);
        register(CriteriaTriggers.ENCHANTED_ITEM, EnchantedItemTriggerHandler::new);
        register(CriteriaTriggers.FILLED_BUCKET, FilledBucketTriggerHandler::new);
        register(CriteriaTriggers.BREWED_POTION, BrewedPotionTriggerHandler::new);
        register(CriteriaTriggers.CONSTRUCT_BEACON, ConstructBeaconTriggerHandler::new);
        register(CriteriaTriggers.USED_ENDER_EYE, UsedEnderEyeTriggerHandler::new);
        register(CriteriaTriggers.SUMMONED_ENTITY, SummonedEntityTriggerHandler::new);
        register(CriteriaTriggers.BRED_ANIMALS, BredAnimalsTriggerHandler::new);
        register(CriteriaTriggers.LOCATION, () -> TickTriggerHandler.LOCATION);
        register(CriteriaTriggers.SLEPT_IN_BED, () -> TickTriggerHandler.SLEPT_IN_BED);
        register(CriteriaTriggers.CURED_ZOMBIE_VILLAGER, CuredZombieVillagerTriggerHandler::new);
        register(CriteriaTriggers.TRADE, TradeTriggerHandler::new);
        register(CriteriaTriggers.ITEM_DURABILITY_CHANGED, ItemDurabilityChangedTriggerHandler::new);
        register(CriteriaTriggers.LEVITATION, LevitationTriggerHandler::new);
        register(CriteriaTriggers.CHANGED_DIMENSION, ChangedDimensionTriggerHandler::new);
        register(CriteriaTriggers.TICK, () -> TickTriggerHandler.TICK);
        register(CriteriaTriggers.TAME_ANIMAL, TameAnimalTriggerHandler::new);
        register(CriteriaTriggers.PLACED_BLOCK, () -> ItemTriggerHandler.PLACED_BLOCK);
        register(CriteriaTriggers.CONSUME_ITEM, ConsumeItemTriggerHandler::new);
        register(CriteriaTriggers.EFFECTS_CHANGED, EffectsChangedTriggerHandler::new);
        register(CriteriaTriggers.USED_TOTEM, UsedTotemTriggerHandler::new);
        register(CriteriaTriggers.NETHER_TRAVEL, () -> TravelTriggerHandler.NETHER_TRAVEL);
        register(CriteriaTriggers.FISHING_ROD_HOOKED, FishingRodHookedTriggerHandler::new);
        register(CriteriaTriggers.CHANNELED_LIGHTNING, ChanneledLightningTriggerHandler::new);
        register(CriteriaTriggers.SHOT_CROSSBOW, ShotCrossbowTriggerHandler::new);
        register(CriteriaTriggers.SPEAR_MOBS_TRIGGER, SpearMobsTriggerHandler::new);
        register(CriteriaTriggers.KILLED_BY_ARROW, KilledByArrowTriggerHandler::new);
        register(CriteriaTriggers.RAID_WIN, () -> TickTriggerHandler.HERO_OF_THE_VILLAGE);
        register(CriteriaTriggers.RAID_OMEN, () -> TickTriggerHandler.VOLUNTARY_EXILE);
        register(CriteriaTriggers.HONEY_BLOCK_SLIDE, SlideDownBlockTriggerHandler::new);
        register(CriteriaTriggers.BEE_NEST_DESTROYED, BeeNestDestroyedTriggerHandler::new);
        register(CriteriaTriggers.TARGET_BLOCK_HIT, TargetHitTriggerHandler::new);
        register(CriteriaTriggers.ITEM_USED_ON_BLOCK, () -> ItemTriggerHandler.ITEM_USED_ON_BLOCK);
        register(CriteriaTriggers.DEFAULT_BLOCK_USE, DefaultBlockUseTriggerHandler::new);
        register(CriteriaTriggers.ANY_BLOCK_USE, AnyBlockUseTriggerHandler::new);
        register(CriteriaTriggers.GENERATE_LOOT, PlayerGeneratesContainerLootTriggerHandler::new);
        register(CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY, () -> ThrownItemPickedUpByEntityTriggerHandler.THROWN_ITEM_PICKED_UP_BY_ENTITY);
        register(CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_PLAYER, () -> ThrownItemPickedUpByEntityTriggerHandler.THROWN_ITEM_PICKED_UP_BY_PLAYER);
        register(CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY, () -> PlayerInteractedWithEntityTriggerHandler.PLAYER_INTERACTED_WITH_ENTITY);
        register(CriteriaTriggers.PLAYER_SHEARED_EQUIPMENT, () -> PlayerInteractedWithEntityTriggerHandler.PLAYER_SHEARED_EQUIPMENT);
        register(CriteriaTriggers.START_RIDING_TRIGGER, StartedRidingTriggerHandler::new);
        register(CriteriaTriggers.LIGHTNING_STRIKE, LightningStrikeTriggerHandler::new);
        register(CriteriaTriggers.USING_ITEM, UsingItemTriggerHandler::new);
        register(CriteriaTriggers.FALL_FROM_HEIGHT, () -> TravelTriggerHandler.FALL_FROM_HEIGHT);
        register(CriteriaTriggers.RIDE_ENTITY_IN_LAVA_TRIGGER, () -> TravelTriggerHandler.RIDE_ENTITY_IN_LAVA);
        register(CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST, () -> OnKilledTriggerHandler.KILL_MOB_NEAR_SKULK_CATALYST);
        register(CriteriaTriggers.ALLAY_DROP_ITEM_ON_BLOCK, () -> ItemTriggerHandler.ALLAY_DROP_ITEM_ON_BLOCK);
        register(CriteriaTriggers.AVOID_VIBRATION, () -> TickTriggerHandler.AVOID_VIBRATION);
        register(CriteriaTriggers.RECIPE_CRAFTED, () -> RecipeCraftedTriggerHandler.RECIPE_CRAFTED);
        register(CriteriaTriggers.CRAFTER_RECIPE_CRAFTED, () -> RecipeCraftedTriggerHandler.CRAFTER_RECIPE_CRAFTED);
        register(CriteriaTriggers.FALL_AFTER_EXPLOSION, FallAfterExplosionTriggerHandler::new);

        register(ResearcherCriteriaTriggers.BLOCK_BROKEN, BlockBrokenTriggerHandler::new);
        register(ResearcherCriteriaTriggers.ITEM_CRAFTED, ItemCraftedTriggerHandler::new);
        register(ResearcherCriteriaTriggers.RESEARCH_ITEMS, ResearchItemsTriggerHandler::new);
        register(ResearcherCriteriaTriggers.HAS_RESEARCH, ResearchUnlockedTriggerHandler::new);
    }

    private static <T extends CriterionTriggerInstance> void register(CriterionTrigger<T> criterion, Supplier<TriggerHandler<T>> handler) {
        TriggerHandlerRegistry.register(criterion, handler);
    }
}
