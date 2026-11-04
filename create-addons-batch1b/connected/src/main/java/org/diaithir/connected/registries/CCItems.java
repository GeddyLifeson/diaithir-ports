package org.diaithir.connected.registries;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.diaithir.connected.CreateConnected;
import org.diaithir.connected.content.VerticalBoxItem;

public class CCItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateConnected.MODID);

    public static final DeferredItem<BlockItem> PARALLEL_GEARBOX = ITEMS.registerSimpleBlockItem(CCBlocks.PARALLEL_GEARBOX);
    public static final DeferredItem<VerticalBoxItem> VERTICAL_PARALLEL_GEARBOX = ITEMS.registerItem("vertical_parallel_gearbox", p -> new VerticalBoxItem(CCBlocks.PARALLEL_GEARBOX.get(), p));
    public static final DeferredItem<BlockItem> SIX_WAY_GEARBOX = ITEMS.registerSimpleBlockItem(CCBlocks.SIX_WAY_GEARBOX);
    public static final DeferredItem<VerticalBoxItem> VERTICAL_SIX_WAY_GEARBOX = ITEMS.registerItem("vertical_six_way_gearbox", p -> new VerticalBoxItem(CCBlocks.SIX_WAY_GEARBOX.get(), p));
    public static final DeferredItem<BlockItem> BRASS_GEARBOX = ITEMS.registerSimpleBlockItem(CCBlocks.BRASS_GEARBOX);
    public static final DeferredItem<VerticalBoxItem> VERTICAL_BRASS_GEARBOX = ITEMS.registerItem("vertical_brass_gearbox", p -> new VerticalBoxItem(CCBlocks.BRASS_GEARBOX.get(), p));
    public static final DeferredItem<BlockItem> INVERTED_CLUTCH = ITEMS.registerSimpleBlockItem(CCBlocks.INVERTED_CLUTCH);
    public static final DeferredItem<BlockItem> INVERTED_GEARSHIFT = ITEMS.registerSimpleBlockItem(CCBlocks.INVERTED_GEARSHIFT);
    public static final DeferredItem<BlockItem> BRAKE = ITEMS.registerSimpleBlockItem(CCBlocks.BRAKE);

    public static final DeferredItem<BlockItem> CROSS_CONNECTOR = ITEMS.registerSimpleBlockItem(CCBlocks.CROSS_CONNECTOR);

    public static final DeferredItem<BlockItem> OVERSTRESS_CLUTCH = ITEMS.registerSimpleBlockItem(CCBlocks.OVERSTRESS_CLUTCH);
    public static final DeferredItem<BlockItem> FREEWHEEL_CLUTCH = ITEMS.registerSimpleBlockItem(CCBlocks.FREEWHEEL_CLUTCH);
    public static final DeferredItem<BlockItem> CENTRIFUGAL_CLUTCH = ITEMS.registerSimpleBlockItem(CCBlocks.CENTRIFUGAL_CLUTCH);

    public static final DeferredItem<BlockItem> SHEAR_PIN = ITEMS.registerSimpleBlockItem(CCBlocks.SHEAR_PIN);

    public static Item[] all() {
        return ITEMS.getEntries().stream().map(h -> (Item) h.get()).toArray(Item[]::new);
    }
}
