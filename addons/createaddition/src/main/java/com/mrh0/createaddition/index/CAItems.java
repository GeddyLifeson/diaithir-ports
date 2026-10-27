package com.mrh0.createaddition.index;

import com.mrh0.createaddition.item.*;
import com.mrh0.createaddition.reg.ItemEntry;
import com.mrh0.createaddition.reg.R;

import net.minecraft.world.item.Item;

public class CAItems {
    public static final ItemEntry<CapacitorItem> CAPACITOR = R.item("capacitor", CapacitorItem::new, new Item.Properties());
    public static final ItemEntry<Item> DIAMOND_GRIT = R.item("diamond_grit", Item::new, new Item.Properties());
    public static final ItemEntry<DiamondGritSandpaperItem> DIAMOND_GRIT_SANDPAPER = R.item("diamond_grit_sandpaper", DiamondGritSandpaperItem::new, new Item.Properties());
    public static final ItemEntry<Item> BIOMASS = R.item("biomass", Item::new, new Item.Properties().stacksTo(16));
    public static final ItemEntry<BiomassPelletItem> BIOMASS_PELLET = R.item("biomass_pellet", BiomassPelletItem::new, new Item.Properties());
    public static final ItemEntry<ElectrumAmuletItem> ELECTRUM_AMULET = R.item("electrum_amulet", ElectrumAmuletItem::new, new Item.Properties());
    public static final ItemEntry<Item> ELECTRUM_INGOT = R.item("electrum_ingot", Item::new, new Item.Properties());
    public static final ItemEntry<Item> ELECTRUM_NUGGET = R.item("electrum_nugget", Item::new, new Item.Properties());
    public static final ItemEntry<Item> ELECTRUM_SHEET = R.item("electrum_sheet", Item::new, new Item.Properties());
    public static final ItemEntry<Item> ZINC_SHEET = R.item("zinc_sheet", Item::new, new Item.Properties());
    public static final ItemEntry<Item> COPPER_WIRE = R.item("copper_wire", Item::new, new Item.Properties());
    public static final ItemEntry<Item> IRON_WIRE = R.item("iron_wire", Item::new, new Item.Properties());
    public static final ItemEntry<Item> GOLD_WIRE = R.item("gold_wire", Item::new, new Item.Properties());
    public static final ItemEntry<Item> ELECTRUM_WIRE = R.item("electrum_wire", Item::new, new Item.Properties());
    public static final ItemEntry<WireSpool> SPOOL = R.item("spool", WireSpool::new, new Item.Properties());
    public static final ItemEntry<WireSpool> COPPER_SPOOL = R.item("copper_spool", WireSpool::new, new Item.Properties());
    public static final ItemEntry<WireSpool> GOLD_SPOOL = R.item("gold_spool", WireSpool::new, new Item.Properties());
    public static final ItemEntry<WireSpool> ELECTRUM_SPOOL = R.item("electrum_spool", WireSpool::new, new Item.Properties());
    public static final ItemEntry<WireSpool> FESTIVE_SPOOL = R.item("festive_spool", WireSpool::new, new Item.Properties());
    public static final ItemEntry<Item> COPPER_ROD = R.item("copper_rod", Item::new, new Item.Properties());
    public static final ItemEntry<Item> IRON_ROD = R.item("iron_rod", Item::new, new Item.Properties());
    public static final ItemEntry<Item> GOLD_ROD = R.item("gold_rod", Item::new, new Item.Properties());
    public static final ItemEntry<Item> ELECTRUM_ROD = R.item("electrum_rod", Item::new, new Item.Properties());
    public static final ItemEntry<Item> BRASS_ROD = R.item("brass_rod", Item::new, new Item.Properties());
    public static final ItemEntry<Item> CAKE_BASE = R.item("cake_base", Item::new, new Item.Properties());
    public static final ItemEntry<Item> CAKE_BASE_BAKED = R.item("cake_base_baked", Item::new, new Item.Properties());
    public static final ItemEntry<Item> STRAW = R.item("straw", Item::new, new Item.Properties().stacksTo(16));

    public static void register() {}
}
