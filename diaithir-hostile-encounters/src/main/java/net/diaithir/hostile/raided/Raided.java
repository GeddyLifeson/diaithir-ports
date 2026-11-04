package net.diaithir.hostile.raided;

import net.diaithir.hostile.raided.client.ClientHandler;
import net.diaithir.hostile.raided.config.RaidedConfig;
import net.diaithir.hostile.raided.handler.EventHandler;
import net.diaithir.hostile.raided.registry.RaidedRegistry;
import net.diaithir.hostile.raided.registry.RaidedSetup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class Raided {
	public static final String MOD_ID = "raided";
	public static final Logger LOGGER = LogManager.getLogger();

	public static void init(IEventBus eventBus, ModContainer container, Dist dist) {
		container.registerConfig(ModConfig.Type.COMMON, RaidedConfig.commonSpec);
		eventBus.register(RaidedConfig.class);

		eventBus.addListener(Raided::addTabContents);

		RaidedRegistry.ITEMS.register(eventBus);
		RaidedRegistry.ENTITIES.register(eventBus);
		RaidedRegistry.SOUND_EVENTS.register(eventBus);

		NeoForge.EVENT_BUS.register(new EventHandler());

		eventBus.addListener(RaidedSetup::registerEntityAttributes);

		if (dist.isClient()) {
			
			eventBus.addListener(ClientHandler::registerEntityRenders);
			eventBus.addListener(ClientHandler::registerLayerDefinitions);
		}
	}

	private static void addTabContents(final BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
			List<ItemStack> stacks = RaidedRegistry.ITEMS.getEntries().stream().filter(reg ->
					reg.get() instanceof SpawnEggItem).map(reg -> new ItemStack(reg.get())).toList();
			event.acceptAll(stacks);
		}
	}

	public static Identifier modLoc(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
