package com.diaithir.addons.newage;

import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import com.diaithir.addons.newage.content.electricity.wire.ElectricWireItem;


public class CNAItems {
    public static final DeferredItem<Item> OVERCHARGED_GOLD =
            Reg
            .item("overcharged_gold", Item::new)
            .register();

    public static final DeferredItem<Item> OVERCHARGED_IRON =
            Reg
            .item("overcharged_iron", Item::new)
            .register();

    public static final DeferredItem<Item> OVERCHARGED_DIAMOND =
            Reg
            .item("overcharged_diamond", Item::new)
            .register();
    
    public static final DeferredItem<Item> NUCLEAR_FUEL =
            Reg
            .item("nuclear_fuel", Item::new)
            .register();

    public static final DeferredItem<Item> THORIUM =
            Reg
            .item("thorium", Item::new)
            .register();

    public static final DeferredItem<Item> RADIOACTIVE_THORIUM =
            Reg
            .item("radioactive_thorium", Item::new)
            .register();

    public static final DeferredItem<SequencedAssemblyItem> INCOMPLETE_FUEL =
            Reg
            .item("incomplete_fuel", SequencedAssemblyItem::new)
            .removeTab(null)
            .register();

    public static final DeferredItem<SequencedAssemblyItem> INCOMPLETE_REACTOR_CASING =
            Reg
            .item("incomplete_reactor_casing", SequencedAssemblyItem::new)
            .removeTab(null)
            .register();

    public static final DeferredItem<SequencedAssemblyItem> INCOMPLETE_WIRE =
            Reg
            .item("incomplete_wire", SequencedAssemblyItem::new)
            .removeTab(null)
            .register();

    public static final DeferredItem<SequencedAssemblyItem> INCOMPLETE_ENCHANTED_GOLDEN_APPLE =
            Reg
            .item("incomplete_enchanted_golden_apple", SequencedAssemblyItem::new)
            .removeTab(null)
            .register();


    public static final DeferredItem<Item> OVERCHARGED_IRON_SHEET =
            Reg
            .item("overcharged_iron_sheet", Item::new)
            .register();

    public static final DeferredItem<Item> OVERCHARGED_GOLDEN_SHEET =
            Reg
            .item("overcharged_golden_sheet", Item::new)
            .register();

    public static final DeferredItem<Item> BLANK_CIRCUIT =
            Reg
            .item("blank_circuit", Item::new)
            .register();
    public static final DeferredItem<Item> COPPER_CIRCUIT =
            Reg
            .item("copper_circuit", Item::new)
            .register();


    public static final DeferredItem<ElectricWireItem> COPPER_WIRE =
            Reg
            .item("copper_wire", ElectricWireItem::newCopperWire)
            .register();

    public static final DeferredItem<ElectricWireItem> OVERCHARGED_IRON_WIRE =
            Reg
            .item("overcharged_iron_wire", ElectricWireItem::newIronWire)
            .register();

    public static final DeferredItem<ElectricWireItem> OVERCHARGED_GOLDEN_WIRE =
            Reg
            .item("overcharged_golden_wire", ElectricWireItem::newGoldenWire)
            .register();

    public static final DeferredItem<ElectricWireItem> OVERCHARGED_DIAMOND_WIRE =
            Reg
            .item("overcharged_diamond_wire", ElectricWireItem::newDiamondWire)
            .register();

    public static void load() {  }
}
