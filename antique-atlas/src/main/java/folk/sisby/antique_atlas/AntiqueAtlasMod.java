package folk.sisby.antique_atlas;

import folk.sisby.antique_atlas.compat.WaystonesCompat;
import folk.sisby.antique_atlas.item.AtlasItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.ItemLore;
import java.util.List;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Common (both sides) entry point of the NeoForge port: registers the atlas and parchment items.
 * Everything else in Antique Atlas is client-side and lives in {@link AntiqueAtlas}.
 */
@Mod(AntiqueAtlasMod.ID)
public final class AntiqueAtlasMod {
	public static final String ID = "antique_atlas";
	public static final Logger LOGGER = LogManager.getLogger("Antique Atlas");

	private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);

	public static final DeferredItem<AtlasItem> ATLAS = ITEMS.registerItem("atlas", AtlasItem::new, p -> p.stacksTo(1).component(DataComponents.LORE, nativeName("atlas")));
	public static final DeferredItem<Item> PARCHMENT = ITEMS.registerSimpleItem("parchment", p -> p.component(DataComponents.LORE, nativeName("parchment")));

	public static final DeferredItem<Item> COMPASS_MAP = ITEMS.registerSimpleItem("compass_map", p -> p.stacksTo(1).component(DataComponents.LORE, nativeName("compass_map")));

	/** The Diaithiris name as a grey second line under the English name. */
	private static ItemLore nativeName(String item) {
		return new ItemLore(List.of(Component.translatable("item.antique_atlas." + item + ".native").withStyle(ChatFormatting.GRAY)));
	}

	public AntiqueAtlasMod(IEventBus modBus, net.neoforged.fml.ModContainer container) {
		container.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER, AntiqueAtlasServerConfig.SPEC);
		ITEMS.register(modBus);
		WaystonesCompat.init();
		modBus.addListener((BuildCreativeModeTabContentsEvent e) -> {
			if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
				e.accept(ATLAS.get());
				e.accept(COMPASS_MAP.get());
			} else if (e.getTabKey() == CreativeModeTabs.INGREDIENTS) {
				e.accept(PARCHMENT.get());
			}
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(ID, path);
	}
}
