package mcjty.rftoolsutility.modules.screen;

import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbase.modules.tablet.items.TabletItem;
import mcjty.rftoolsutility.modules.screen.blocks.*;
import mcjty.rftoolsutility.modules.screen.client.GuiScreen;
import mcjty.rftoolsutility.modules.screen.client.GuiScreenController;
import mcjty.rftoolsutility.modules.screen.client.ScreenRenderer;
import mcjty.rftoolsutility.modules.screen.data.ScreenData;
import mcjty.rftoolsutility.modules.screen.items.ScreenLinkItem;
import mcjty.rftoolsutility.modules.screen.items.modules.*;
import mcjty.rftoolsutility.modules.screen.modules.*;
import mcjty.rftoolsutility.setup.Config;
import mcjty.rftoolsutility.modules.screen.items.ScreenTabletItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Supplier;

import static mcjty.rftoolsutility.RFToolsUtility.tab;
import static mcjty.rftoolsutility.setup.Registration.*;


public class ScreenModule implements IModule {

    public static final RBlock<ScreenBlock, BlockItem, ScreenTileEntity> SCREEN = RBLOCKS.registerBlock("screen",
            ScreenTileEntity.class,
            () -> new ScreenBlock(ScreenTileEntity::new, false),
            block -> new BlockItem(block.get(), createStandardProperties()),
            ScreenTileEntity::new
    );
    public static final RBlock<ScreenBlock, BlockItem, CreativeScreenTileEntity> CREATIVE_SCREEN = RBLOCKS.registerBlock("creative_screen",
            CreativeScreenTileEntity.class,
            () -> new ScreenBlock(CreativeScreenTileEntity::new, true),
            block -> new BlockItem(block.get(), createStandardProperties()),
            CreativeScreenTileEntity::new
    );

    public static final Supplier<MenuType<ScreenContainer>> CONTAINER_SCREEN = CONTAINERS.register("screen", GenericContainer::createContainerType);
    public static final Supplier<MenuType<ScreenContainer>> CONTAINER_SCREEN_REMOTE = CONTAINERS.register("screen_remote",
            () -> GenericContainer.createRemoteContainerType(ScreenTileEntity::new, ScreenContainer::createRemote, ScreenContainer.SCREEN_MODULES));
    public static final Supplier<MenuType<ScreenContainer>> CONTAINER_SCREEN_REMOTE_CREATIVE = CONTAINERS.register("screen_remote_creative",
            () -> GenericContainer.createRemoteContainerType(CreativeScreenTileEntity::new, ScreenContainer::createRemoteCreative, ScreenContainer.SCREEN_MODULES));

    public static final RBlock<ScreenHitBlock, BlockItem, ScreenHitTileEntity> SCREEN_HIT = RBLOCKS.registerBlock("screen_hitblock",
            ScreenHitTileEntity.class,
            ScreenHitBlock::new,
            null,
            ScreenHitTileEntity::new
    );

    public static final RBlock<ScreenControllerBlock, BlockItem, ScreenControllerTileEntity> SCREEN_CONTROLLER = RBLOCKS.registerBlock("screen_controller",
            ScreenControllerTileEntity.class,
            ScreenControllerBlock::new,
            block -> new BlockItem(block.get(), createStandardProperties()),
            ScreenControllerTileEntity::new
    );
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_SCREEN_CONTROLLER = CONTAINERS.register("screen_controller", GenericContainer::createContainerType);

    public static final DeferredItem<Item> TEXT_MODULE = ITEMS.register("text_module", tab(TextModuleItem::new));
    public static final DeferredItem<Item> ENERGY_MODULE = ITEMS.register("energy_module", tab(EnergyModuleItem::new));
    public static final DeferredItem<Item> ENERGYPLUS_MODULE = ITEMS.register("energyplus_module", tab(EnergyPlusModuleItem::new));
    public static final DeferredItem<Item> INVENTORY_MODULE = ITEMS.register("inventory_module", tab(InventoryModuleItem::new));
    public static final DeferredItem<Item> INVENTORYPLUS_MODULE = ITEMS.register("inventoryplus_module", tab(InventoryPlusModuleItem::new));
    public static final DeferredItem<Item> CLOCK_MODULE = ITEMS.register("clock_module", tab(ClockModuleItem::new));
    public static final DeferredItem<Item> FLUID_MODULE = ITEMS.register("fluid_module", tab(FluidModuleItem::new));
    public static final DeferredItem<Item> FLUIDPLUS_MODULE = ITEMS.register("fluidplus_module", tab(FluidPlusModuleItem::new));
    public static final DeferredItem<Item> MACHINEINFORMATION_MODULE = ITEMS.register("machineinformation_module", tab(MachineInformationModuleItem::new));
    public static final DeferredItem<Item> BUTTON_MODULE = ITEMS.register("button_module", tab(ButtonModuleItem::new));
    public static final DeferredItem<Item> REDSTONE_MODULE = ITEMS.register("redstone_module", tab(RedstoneModuleItem::new));
    public static final DeferredItem<Item> COUNTER_MODULE = ITEMS.register("counter_module", tab(CounterModuleItem::new));
    public static final DeferredItem<Item> COUNTERPLUS_MODULE = ITEMS.register("counterplus_module", tab(CounterPlusModuleItem::new));

    public static final DeferredItem<TabletItem> TABLET_SCREEN = ITEMS.register("tablet_screen", tab(ScreenTabletItem::new));
    public static final DeferredItem<ScreenLinkItem> SCREEN_LINK = ITEMS.register("screen_link", tab(ScreenLinkItem::new));

    public static final Supplier<AttachmentType<ScreenData>> SCREEN_DATA = ATTACHMENT_TYPES.register(
            "screen_data", () -> AttachmentType.builder(ScreenData::createDefault)
                    .serialize(ScreenData.CODEC.fieldOf("value"))
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ScreenData>> ITEM_SCREEN_DATA = COMPONENTS.registerComponentType(
            "screen_data",
            builder -> builder
                    .persistent(ScreenData.CODEC)
                    .networkSynchronized(ScreenData.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ClockScreenModule>> MODULE_CLOCK_DATA = COMPONENTS.registerComponentType(
            "module_clock_data",
            builder -> builder
                    .persistent(ClockScreenModule.CODEC)
                    .networkSynchronized(ClockScreenModule.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EnergyBarScreenModule>> MODULE_ENERGY_BAR_DATA = COMPONENTS.registerComponentType(
            "module_energy_bar_data",
            builder -> builder
                    .persistent(EnergyBarScreenModule.CODEC)
                    .networkSynchronized(EnergyBarScreenModule.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ButtonScreenModule>> MODULE_BUTTON_DATA = COMPONENTS.registerComponentType(
            "module_button_data",
            builder -> builder
                    .persistent(ButtonScreenModule.CODEC)
                    .networkSynchronized(ButtonScreenModule.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CounterScreenModule>> MODULE_COUNTER_DATA = COMPONENTS.registerComponentType(
            "module_counter_data",
            builder -> builder
                    .persistent(CounterScreenModule.CODEC)
                    .networkSynchronized(CounterScreenModule.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FluidBarScreenModule>> MODULE_FLUIDBAR_DATA = COMPONENTS.registerComponentType(
            "module_fluidbar_data",
            builder -> builder
                    .persistent(FluidBarScreenModule.CODEC)
                    .networkSynchronized(FluidBarScreenModule.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<InventoryScreenModule>> MODULE_INVENTORY_DATA = COMPONENTS.registerComponentType(
            "module_inventory_data",
            builder -> builder
                    .persistent(InventoryScreenModule.CODEC)
                    .networkSynchronized(InventoryScreenModule.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MachineInformationScreenModule>> MODULE_MACHINEINFO_DATA = COMPONENTS.registerComponentType(
            "module_machineinfo_data",
            builder -> builder
                    .persistent(MachineInformationScreenModule.CODEC)
                    .networkSynchronized(MachineInformationScreenModule.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RedstoneScreenModule>> MODULE_REDSTONE_DATA = COMPONENTS.registerComponentType(
            "module_redstone_data",
            builder -> builder
                    .persistent(RedstoneScreenModule.CODEC)
                    .networkSynchronized(RedstoneScreenModule.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TextScreenModule>> MODULE_TEXT_DATA = COMPONENTS.registerComponentType(
            "module_text_data",
            builder -> builder
                    .persistent(TextScreenModule.CODEC)
                    .networkSynchronized(TextScreenModule.STREAM_CODEC));

    public ScreenModule(IEventBus bus) {
        bus.addListener(this::registerMenuScreens);
    }

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    public void registerMenuScreens(RegisterMenuScreensEvent event) {
        GuiScreen.register(event);
        GuiScreenController.register(event);
    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        ScreenRenderer.register();
    }

    @Override
    public void initConfig(IEventBus bus) {
        ScreenConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }

}
