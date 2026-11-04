package net.diaithir.gear.fold;

import net.diaithir.gear.DiaithirGear;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Folded survey items (survey part 2, combat and gear). Each feature is a small original class in this package; credits and licences
 * are in NOTICE-FOLDS.md. Item magnets (CC0 design), anvil repair (LGPL-2.1 behaviour, own package {@code lgpl}), repair kits (MIT design).
 */
public final class Folds {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DiaithirGear.ID);

    public static final DeferredItem<Item> ITEM_MAGNET = ITEMS.registerItem("item_magnet", Item::new, () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> SUPER_ITEM_MAGNET = ITEMS.registerItem("super_item_magnet", Item::new, () -> new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> DUCT_TAPE = kit("duct_tape", 190, Rarity.COMMON, 0, 0);
    public static final DeferredItem<Item> COPPER_REPAIR_KIT = kit("copper_repair_kit", 300, Rarity.COMMON, 0, 0);
    public static final DeferredItem<Item> IRON_REPAIR_KIT = kit("iron_repair_kit", 780, Rarity.UNCOMMON, 0, 0);
    public static final DeferredItem<Item> GOLDEN_REPAIR_KIT = kit("golden_repair_kit", 930, Rarity.UNCOMMON, 0, 1);
    public static final DeferredItem<Item> DIAMOND_REPAIR_KIT = kit("diamond_repair_kit", 1950, Rarity.RARE, 1, 1);
    public static final DeferredItem<Item> AMETHYST_REPAIR_KIT = kit("amethyst_repair_kit", 2150, Rarity.RARE, 1, 2);
    public static final DeferredItem<Item> NETHERITE_REPAIR_KIT = kit("netherite_repair_kit", 5000, Rarity.EPIC, 3, 3);

    private static DeferredItem<Item> kit(String id, int pool, Rarity r, int slow, int fatigue) {
        return ITEMS.registerItem(id, p -> new RepairKit(p, slow, fatigue), () -> new Item.Properties().durability(pool).rarity(r));
    }

    private Folds() {}

    public static void init(IEventBus bus) {
        ITEMS.register(bus);
        bus.addListener(Folds::tabs);
        NeoForge.EVENT_BUS.register(new Magnets());
        NeoForge.EVENT_BUS.register(new net.diaithir.gear.fold.lgpl.AnvilRepairing());
        NeoForge.EVENT_BUS.register(new RepairKit.Carry());
    }

    private static void tabs(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
            e.accept(ITEM_MAGNET.get());
            e.accept(SUPER_ITEM_MAGNET.get());
            for (DeferredItem<Item> k : new DeferredItem[] {DUCT_TAPE, COPPER_REPAIR_KIT, IRON_REPAIR_KIT, GOLDEN_REPAIR_KIT, DIAMOND_REPAIR_KIT, AMETHYST_REPAIR_KIT, NETHERITE_REPAIR_KIT}) e.accept(k.get());
        }
    }
}
