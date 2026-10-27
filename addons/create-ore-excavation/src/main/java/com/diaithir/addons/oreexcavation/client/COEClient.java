package com.diaithir.addons.oreexcavation.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

import com.diaithir.addons.oreexcavation.CreateOreExcavation;
import com.diaithir.addons.oreexcavation.Registration;
import com.diaithir.addons.oreexcavation.menu.OreVeinAtlasScreen;
import com.diaithir.addons.oreexcavation.recipe.OreRecipes;

@EventBusSubscriber(value = Dist.CLIENT, modid = CreateOreExcavation.MOD_ID)
public class COEClient {

	@SubscribeEvent
	public static void registerScreens(RegisterMenuScreensEvent event) {
		event.register(Registration.VEIN_ATLAS_MENU.get(), OreVeinAtlasScreen::new);
	}

	@SubscribeEvent
	public static void recipes(RecipesReceivedEvent event) {
		OreRecipes.clientMap = event.getRecipeMap();
	}
}
