package mcjty.rftoolspower.modules.monitor;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.modules.IModule;
import mcjty.rftoolspower.modules.monitor.blocks.PowerLevelTileEntity;
import mcjty.rftoolspower.modules.monitor.blocks.PowerMonitorTileEntity;
import mcjty.rftoolspower.modules.monitor.client.GuiPowerMonitor;
import mcjty.rftoolspower.modules.monitor.client.PowerLevelRenderer;
import mcjty.rftoolspower.modules.monitor.data.PowerMonitorData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static mcjty.rftoolspower.setup.Registration.*;

public class MonitorModule implements IModule {

    public static final RBlock<BaseBlock, BlockItem, PowerMonitorTileEntity> POWER_MONITOR = RBLOCKS.registerBlock("power_monitor",
            PowerMonitorTileEntity.class,
            PowerMonitorTileEntity::createBlock,
            block -> new BlockItem(block.get(), createStandardProperties()),
            PowerMonitorTileEntity::new
    );
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_POWER_MONITOR = CONTAINERS.register("power_monitor", GenericContainer::createContainerType);

    public static final RBlock<LogicSlabBlock, BlockItem, PowerLevelTileEntity> POWER_LEVEL = RBLOCKS.registerBlock("power_level",
            PowerLevelTileEntity.class,
            PowerLevelTileEntity::createBlock,
            block -> new BlockItem(block.get(), createStandardProperties()),
            PowerLevelTileEntity::new
    );

    public static final Supplier<AttachmentType<PowerMonitorData>> POWER_MONITOR_DATA = ATTACHMENT_TYPES.register(
            "power_monitor_data", () -> AttachmentType.builder(() -> new PowerMonitorData(0, (byte) 0, (byte) 0))
                    .serialize(PowerMonitorData.CODEC.fieldOf("value"))
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PowerMonitorData>> ITEM_POWER_MONITOR_DATA = COMPONENTS.registerComponentType(
            "power_monitor_data",
            builder -> builder
                    .persistent(PowerMonitorData.CODEC)
                    .networkSynchronized(PowerMonitorData.STREAM_CODEC));

    public MonitorModule(IEventBus bus, Dist dist) {
        bus.addListener(this::registerMenuScreens);
    }

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        PowerLevelRenderer.register();
    }

    public void registerMenuScreens(RegisterMenuScreensEvent event) {
        GuiPowerMonitor.register(event);
    }

    @Override
    public void initConfig(IEventBus bus) {

    }

    public static List<Identifier> onTextureStitch() {
        return Arrays.asList(PowerLevelRenderer.DIGITS);
    }

}
