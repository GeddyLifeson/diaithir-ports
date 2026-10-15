package mcjty.rftoolsutility.modules.teleporter;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsutility.modules.teleporter.blocks.*;
import mcjty.rftoolsutility.modules.teleporter.client.BeamRenderer;
import mcjty.rftoolsutility.modules.teleporter.client.GuiDialingDevice;
import mcjty.rftoolsutility.modules.teleporter.client.GuiMatterReceiver;
import mcjty.rftoolsutility.modules.teleporter.client.GuiMatterTransmitter;
import mcjty.rftoolsutility.modules.teleporter.data.*;
import mcjty.rftoolsutility.modules.teleporter.items.porter.AdvancedChargedPorterItem;
import mcjty.rftoolsutility.modules.teleporter.items.porter.ChargedPorterItem;
import mcjty.rftoolsutility.modules.teleporter.items.teleportprobe.TeleportProbeItem;
import mcjty.rftoolsutility.setup.Config;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Supplier;

import static mcjty.rftoolsutility.RFToolsUtility.tab;
import static mcjty.rftoolsutility.setup.Registration.*;

public class TeleporterModule implements IModule {

    public static final RBlock<BaseBlock, BlockItem, MatterTransmitterTileEntity> MATTER_TRANSMITTER = RBLOCKS.registerBlock("matter_transmitter",
            MatterTransmitterTileEntity.class,
            MatterTransmitterBlock::new,
            block -> new BlockItem(block.get(), createStandardProperties()),
            MatterTransmitterTileEntity::new
    );
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_MATTER_TRANSMITTER = CONTAINERS.register("matter_transmitter", GenericContainer::createContainerType);

    public static final RBlock<BaseBlock, BlockItem, MatterReceiverTileEntity> MATTER_RECEIVER = RBLOCKS.registerBlock("matter_receiver",
            MatterReceiverTileEntity.class,
            MatterReceiverBlock::new,
            block -> new BlockItem(block.get(), createStandardProperties()),
            MatterReceiverTileEntity::new
    );
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_MATTER_RECEIVER = CONTAINERS.register("matter_receiver", GenericContainer::createContainerType);

    public static final RBlock<BaseBlock, BlockItem, DialingDeviceTileEntity> DIALING_DEVICE = RBLOCKS.registerBlock("dialing_device",
            DialingDeviceTileEntity.class,
            DialingDeviceBlock::new,
            block -> new BlockItem(block.get(), createStandardProperties()),
            DialingDeviceTileEntity::new
    );
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_DIALING_DEVICE = CONTAINERS.register("dialing_device", GenericContainer::createContainerType);

    public static final DeferredBlock<DestinationAnalyzerBlock> DESTINATION_ANALYZER = BLOCKS.register("destination_analyzer", DestinationAnalyzerBlock::new);
    public static final DeferredItem<Item> DESTINATION_ANALYZER_ITEM = ITEMS.register("destination_analyzer", tab(() -> new BlockItem(DESTINATION_ANALYZER.get(), createStandardProperties())));

    public static final DeferredBlock<MatterBoosterBlock> MATTER_BOOSTER = BLOCKS.register("matter_booster", MatterBoosterBlock::new);
    public static final DeferredItem<Item> MATTER_BOOSTER_ITEM = ITEMS.register("matter_booster", tab(() -> new BlockItem(MATTER_BOOSTER.get(), createStandardProperties())));

    public static final RBlock<LogicSlabBlock, BlockItem, SimpleDialerTileEntity> SIMPLE_DIALER = RBLOCKS.registerBlock("simple_dialer",
            SimpleDialerTileEntity.class,
            SimpleDialerBlock::new,
            block -> new SimpleDialerItemBlock(block.get()),
            SimpleDialerTileEntity::new
    );

    public static final DeferredItem<TeleportProbeItem> TELEPORT_PROBE = ITEMS.register("teleport_probe", tab(TeleportProbeItem::new));
    public static final DeferredItem<ChargedPorterItem> CHARGED_PORTER = ITEMS.register("charged_porter", tab(ChargedPorterItem::new));
    public static final DeferredItem<AdvancedChargedPorterItem> ADVANCED_CHARGED_PORTER = ITEMS.register("advanced_charged_porter", tab(AdvancedChargedPorterItem::new));

    public static final Supplier<AttachmentType<DialingDeviceData>> DIALINGDEVICE_DATA = ATTACHMENT_TYPES.register(
            "dialingdevice_data", () -> AttachmentType.builder(DialingDeviceData::createDefault)
                    .serialize(DialingDeviceData.CODEC.fieldOf("value"))
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DialingDeviceData>> ITEM_DIALINGDEVICE_DATA = COMPONENTS.registerComponentType(
            "dialingdevice_data",
            builder -> builder
                    .persistent(DialingDeviceData.CODEC)
                    .networkSynchronized(DialingDeviceData.STREAM_CODEC));

    public static final Supplier<AttachmentType<MatterReceiverData>> MATTERRECEIVER_DATA = ATTACHMENT_TYPES.register(
            "matterreceiver_data", () -> AttachmentType.builder(() -> MatterReceiverData.DEFAULT)
                    .serialize(MatterReceiverData.CODEC.fieldOf("value"))
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MatterReceiverData>> ITEM_MATTERRECEIVER_DATA = COMPONENTS.registerComponentType(
            "matterreceiver_data",
            builder -> builder
                    .persistent(MatterReceiverData.CODEC)
                    .networkSynchronized(MatterReceiverData.STREAM_CODEC));

    public static final Supplier<AttachmentType<MatterTransmitterData>> MATTERTRANSMITTER_DATA = ATTACHMENT_TYPES.register(
            "mattertransmitter_data", () -> AttachmentType.builder(MatterTransmitterData::createDefault)
                    .serialize(MatterTransmitterData.CODEC.fieldOf("value"))
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MatterTransmitterData>> ITEM_MATTERTRANSMITTER_DATA = COMPONENTS.registerComponentType(
            "mattertransmitter_data",
            builder -> builder
                    .persistent(MatterTransmitterData.CODEC)
                    .networkSynchronized(MatterTransmitterData.STREAM_CODEC));

    public static final Supplier<AttachmentType<SimpleDialerData>> SIMPLEDIALER_DATA = ATTACHMENT_TYPES.register(
            "simpledialer_data", () -> AttachmentType.builder(SimpleDialerData::createDefault)
                    .serialize(SimpleDialerData.CODEC.fieldOf("value"))
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleDialerData>> ITEM_SIMPLEDIALER_DATA = COMPONENTS.registerComponentType(
            "simpledialer_data",
            builder -> builder
                    .persistent(SimpleDialerData.CODEC)
                    .networkSynchronized(SimpleDialerData.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ChargedPorterData>> ITEM_CHARGEDPORTER_DATA = COMPONENTS.registerComponentType(
            "chargedporter_data",
            builder -> builder
                    .persistent(ChargedPorterData.CODEC)
                    .networkSynchronized(ChargedPorterData.STREAM_CODEC));

    public TeleporterModule(IEventBus bus) {
        bus.addListener(this::registerMenuScreens);
    }

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    public void registerMenuScreens(RegisterMenuScreensEvent event) {
        GuiDialingDevice.register(event);
        GuiMatterTransmitter.register(event);
        GuiMatterReceiver.register(event);
    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientCommandHandler.registerCommands();
        });
        BeamRenderer.register();
    }

    @Override
    public void initConfig(IEventBus bus) {
        TeleportConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }

}
