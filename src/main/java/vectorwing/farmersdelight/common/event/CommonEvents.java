package vectorwing.farmersdelight.common.event;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.level.AlterGroundEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.common.FoodValues;
import vectorwing.farmersdelight.common.registry.ModBlocks;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

@EventBusSubscriber(modid = FarmersDelight.MODID)
public class CommonEvents
{
	/** Clients only get the recipe types the server sends; the JEI plugin reads these. */
	/** Giant spruces lay podzol around them; rich soil is left alone. */
	@SubscribeEvent
	public static void keepRichSoilUnderGiantTrees(AlterGroundEvent event) {
		AlterGroundEvent.StateProvider old = event.getStateProvider();
		event.setStateProvider((level, random, pos) -> level.getBlockState(pos).is(ModBlocks.RICH_SOIL.get()) ? null : old.getState(level, random, pos));
	}

	@SubscribeEvent
	public static void sendRecipesToClients(OnDatapackSyncEvent event) {
		event.sendRecipes(ModRecipeTypes.COOKING.get(), ModRecipeTypes.CUTTING.get());
	}

	@SubscribeEvent
	public static void handleVanillaSoupEffects(LivingEntityUseItemEvent.Finish event) {
		Item food = event.getItem().getItem();
		LivingEntity entity = event.getEntity();

		if (Configuration.ENABLE_RABBIT_STEW_BUFF.get() && food.equals(Items.RABBIT_STEW)) {
			return;
		}

		if (Configuration.ENABLE_VANILLA_SOUP_EXTRA_EFFECTS.get()) {
			ApplyStatusEffectsConsumeEffect soupEffects = FoodValues.ConsumableValues.VANILLA_SOUP_EFFECTS.get(food);

			if (soupEffects != null) {
				for (MobEffectInstance effect : soupEffects.effects()) {
					entity.addEffect(effect);
				}
			}
		}
	}
}
