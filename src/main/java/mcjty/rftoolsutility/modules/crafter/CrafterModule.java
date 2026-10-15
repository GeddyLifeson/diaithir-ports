package mcjty.rftoolsutility.modules.crafter;


import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsutility.modules.crafter.blocks.*;
import mcjty.rftoolsutility.modules.crafter.client.GuiCrafter;
import mcjty.rftoolsutility.modules.crafter.data.CrafterData;
import mcjty.rftoolsutility.setup.Config;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

import static mcjty.rftoolsutility.setup.Registration.*;

public class CrafterModule implements IModule {

    public static final RBlock<BaseBlock, BlockItem, CrafterBaseTE> CRAFTER1 = RBLOCKS.registerBlock("crafter1",
            CrafterBaseTE.class,
            () -> new CrafterBlock(CrafterBaseTE::createTier1),
            block -> new BlockItem(block.get(), createStandardProperties()),
            CrafterBaseTE::createTier1);
    public static final RBlock<BaseBlock, BlockItem, CrafterBaseTE> CRAFTER2 = RBLOCKS.registerBlock("crafter2",
            CrafterBaseTE.class,
            () -> new CrafterBlock(CrafterBaseTE::createTier2),
            block -> new BlockItem(block.get(), createStandardProperties()),
            CrafterBaseTE::createTier2);
    public static final RBlock<BaseBlock, BlockItem, CrafterBaseTE> CRAFTER3 = RBLOCKS.registerBlock("crafter3",
            CrafterBaseTE.class,
            () -> new CrafterBlock(CrafterBaseTE::createTier3),
            block -> new BlockItem(block.get(), createStandardProperties()),
            CrafterBaseTE::createTier3);

    public static final Supplier<MenuType<CrafterContainer>> CONTAINER_CRAFTER = CONTAINERS.register("crafter", GenericContainer::createContainerType);

    public static final Supplier<AttachmentType<CrafterData>> CRAFTER_DATA = ATTACHMENT_TYPES.register(
            "crafter_data", () -> AttachmentType.builder(CrafterData::createDefault)
                    .serialize(CrafterData.CODEC.fieldOf("value"))
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CrafterData>> ITEM_CRAFTER_DATA = COMPONENTS.registerComponentType(
            "crafter_data",
            builder -> builder
                    .persistent(CrafterData.CODEC)
                    .networkSynchronized(CrafterData.STREAM_CODEC));

    public CrafterModule(IEventBus bus) {
        bus.addListener(this::registerMenuScreens);
    }

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    public void registerMenuScreens(RegisterMenuScreensEvent event) {
        GuiCrafter.register(event);
    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
    }

    @Override
    public void initConfig(IEventBus bus) {
        CrafterConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }

}
