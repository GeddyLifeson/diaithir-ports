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

    public static final DeferredItem<Item> ENGINE_SILENCER = ITEMS.registerSimpleItem("engine_silencer");
    public static final DeferredItem<Item> ENGINE_TURBO = ITEMS.registerSimpleItem("engine_turbocharger");

    public static final DeferredItem<BlockItem> MODULAR_DIESEL_ENGINE = ITEMS.registerSimpleBlockItem(CDGBlocks.MODULAR_DIESEL_ENGINE);

    public static final DeferredItem<BlockItem> HUGE_DIESEL_ENGINE = ITEMS.registerSimpleBlockItem(CDGBlocks.HUGE_DIESEL_ENGINE);

    public static final DeferredItem<BlockItem> DISTILLATION_TOWER = ITEMS.registerSimpleBlockItem(CDGBlocks.DISTILLATION_TOWER);

    public static final DeferredItem<BlockItem> PUMPJACK = ITEMS.registerSimpleBlockItem(CDGBlocks.PUMPJACK);

    public static Item[] all() {
        return ITEMS.getEntries().stream().map(h -> (Item) h.get()).toArray(Item[]::new);
    }
}
