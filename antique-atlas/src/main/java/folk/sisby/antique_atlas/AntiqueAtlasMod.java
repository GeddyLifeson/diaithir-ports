package folk.sisby.antique_atlas;

import folk.sisby.antique_atlas.item.AtlasItem;
import net.minecraft.resources.Identifier;
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

	private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);

	public static final DeferredItem<AtlasItem> ATLAS = ITEMS.registerItem("atlas", AtlasItem::new, p -> p.stacksTo(1));
	public static final DeferredItem<Item> PARCHMENT = ITEMS.registerSimpleItem("parchment");

	public AntiqueAtlasMod(IEventBus modBus) {
		ITEMS.register(modBus);
		modBus.addListener((BuildCreativeModeTabContentsEvent e) -> {
			if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
				e.accept(ATLAS.get());
			} else if (e.getTabKey() == CreativeModeTabs.INGREDIENTS) {
				e.accept(PARCHMENT.get());
			}
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(ID, path);
	}
}
