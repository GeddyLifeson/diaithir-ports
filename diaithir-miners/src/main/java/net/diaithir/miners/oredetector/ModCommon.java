package net.diaithir.miners.oredetector;

import net.diaithir.miners.oredetector.config.ModConfigData;
import net.diaithir.miners.oredetector.item.ModItems;
import net.diaithir.miners.oredetector.sound.ModSounds;
import net.diaithir.miners.oredetector.tag.ImportBlockTags;
import net.diaithir.miners.oredetector.tag.ModBlockTags;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.jspecify.annotations.NonNull;


/**
 * Common startup.
 */
@Mod(Global.MODID)
public final class ModCommon {

	@SuppressWarnings("unused")
	public ModCommon(IEventBus modEventBus, @NonNull ModContainer modContainer, Dist dist) {
		// Use configuration parameters on both sides and load them on startup.
		modContainer.registerConfig(ModConfig.Type.STARTUP, ModConfigData.SPEC);
		// Register sounds.
		ModSounds.init(modEventBus);
		// Register tags.
		ImportBlockTags.init();
		ModBlockTags.init();
		// Register items.
		ModItems.init(modEventBus);
	}
}
