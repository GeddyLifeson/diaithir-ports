package org.diaithir.diesel.registries;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.diaithir.diesel.CreateDieselGenerators;

public class CDGItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateDieselGenerators.MODID);

    public static final DeferredItem<BlockItem> DIESEL_ENGINE = ITEMS.registerSimpleBlockItem(CDGBlocks.DIESEL_ENGINE);

    public static final DeferredItem<BlockItem> CANISTER = ITEMS.registerSimpleBlockItem(CDGBlocks.CANISTER, (java.util.function.UnaryOperator<net.minecraft.world.item.Item.Properties>) p -> p.stacksTo(1));

    public static final DeferredItem<BlockItem> BURNER = ITEMS.registerSimpleBlockItem(CDGBlocks.BURNER);

    public static Item[] all() {
        return ITEMS.getEntries().stream().map(h -> (Item) h.get()).toArray(Item[]::new);
    }
}
