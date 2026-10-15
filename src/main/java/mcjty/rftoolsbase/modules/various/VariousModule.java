package mcjty.rftoolsbase.modules.various;

import mcjty.lib.api.smartwrench.SmartWrenchMode;
import mcjty.lib.modules.IModule;
import mcjty.lib.varia.TagTools;
import mcjty.lib.varia.WrenchChecker;
import mcjty.rftoolsbase.RFToolsBase;
import mcjty.rftoolsbase.modules.various.data.WrenchData;
import mcjty.rftoolsbase.modules.various.items.ManualItem;
import mcjty.rftoolsbase.modules.various.items.SmartWrenchItem;
import mcjty.rftoolsbase.setup.Registration;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

import static mcjty.rftoolsbase.RFToolsBase.tab;
import static mcjty.rftoolsbase.setup.Registration.COMPONENTS;
import static mcjty.rftoolsbase.setup.Registration.ITEMS;

public class VariousModule implements IModule {

    public static final DeferredItem<SmartWrenchItem> SMARTWRENCH = ITEMS.register("smartwrench", tab(() -> new SmartWrenchItem(SmartWrenchMode.MODE_WRENCH)));
    public static final DeferredItem<SmartWrenchItem> SMARTWRENCH_SELECT = ITEMS.register("smartwrench_select", tab(() -> new SmartWrenchItem(SmartWrenchMode.MODE_SELECT)));

    public static final DeferredItem<Item> DIMENSIONALSHARD = ITEMS.register("dimensionalshard", tab(VariousModule::createDimensionalShard));
    public static final DeferredItem<Item> INFUSED_DIAMOND = ITEMS.register("infused_diamond", tab(VariousModule::createItem16));
    public static final DeferredItem<Item> INFUSED_ENDERPEARL = ITEMS.register("infused_enderpearl", tab(VariousModule::createItem16));

    public static final DeferredItem<Item> MACHINE_FRAME = ITEMS.register("machine_frame", tab(() -> new Item(Registration.createStandardProperties())));
    public static final DeferredItem<Item> MACHINE_BASE = ITEMS.register("machine_base", tab(() -> new Item(Registration.createStandardProperties())));

    public static final DeferredItem<ManualItem> MANUAL = ITEMS.register("manual", tab(ManualItem::new));

    public static final Identifier SHARDS = Identifier.fromNamespaceAndPath(RFToolsBase.MODID, "shards");
    public static final TagKey<Item> SHARDS_TAG = TagTools.createItemTagKey(SHARDS);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WrenchData>> ITEM_WRENCH_DATA = COMPONENTS.registerComponentType(
            "wrench_data",
            builder -> builder
                    .persistent(WrenchData.CODEC)
                    .networkSynchronized(WrenchData.STREAM_CODEC));

    private static Item createItem16() {
        return new Item(RFToolsBase.setup.defaultProperties().stacksTo(16));
    }

    private static Item createDimensionalShard() {
        return new Item(RFToolsBase.setup.defaultProperties().stacksTo(64));
    }

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {

    }

    @Override
    public void initConfig(IEventBus bus) {

    }

}
