package mcjty.rftoolspower.modules.blazing;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.modules.IModule;
import mcjty.rftoolspower.modules.blazing.blocks.BlazingAgitatorTileEntity;
import mcjty.rftoolspower.modules.blazing.blocks.BlazingGeneratorTileEntity;
import mcjty.rftoolspower.modules.blazing.blocks.BlazingInfuserTileEntity;
import mcjty.rftoolspower.modules.blazing.client.BlazingAgitatorRenderer;
import mcjty.rftoolspower.modules.blazing.client.GuiBlazingAgitator;
import mcjty.rftoolspower.modules.blazing.client.GuiBlazingGenerator;
import mcjty.rftoolspower.modules.blazing.client.GuiBlazingInfuser;
import mcjty.rftoolspower.modules.blazing.data.AgitatorData;
import mcjty.rftoolspower.modules.blazing.data.BlazingRodData;
import mcjty.rftoolspower.modules.blazing.items.BlazingRod;
import mcjty.rftoolspower.setup.Config;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.function.Supplier;

import static mcjty.rftoolspower.RFToolsPower.tab;
import static mcjty.rftoolspower.setup.Registration.*;

public class BlazingModule implements IModule {

    public static final RBlock<BaseBlock, BlockItem, BlazingGeneratorTileEntity> BLAZING_GENERATOR = RBLOCKS.registerBlock("blazing_generator",
            BlazingGeneratorTileEntity.class,
            BlazingGeneratorTileEntity::createBlock,
            block -> new BlockItem(block.get(), createStandardProperties()),
            BlazingGeneratorTileEntity::new
    );
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_BLAZING_GENERATOR = CONTAINERS.register("blazing_generator", GenericContainer::createContainerType);

    public static final RBlock<BaseBlock, BlockItem, BlazingAgitatorTileEntity> BLAZING_AGITATOR = RBLOCKS.registerBlock("blazing_agitator",
            BlazingAgitatorTileEntity.class,
            BlazingAgitatorTileEntity::createBlock,
            block -> new BlockItem(block.get(), createStandardProperties()),
            BlazingAgitatorTileEntity::new
    );
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_BLAZING_AGITATOR = CONTAINERS.register("blazing_agitator", GenericContainer::createContainerType);

    public static final RBlock<BaseBlock, BlockItem, BlazingInfuserTileEntity> BLAZING_INFUSER = RBLOCKS.registerBlock("blazing_infuser",
            BlazingInfuserTileEntity.class,
            BlazingInfuserTileEntity::createBlock,
            block -> new BlockItem(block.get(), createStandardProperties()),
            BlazingInfuserTileEntity::new
    );
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_BLAZING_INFUSER = CONTAINERS.register("blazing_infuser", GenericContainer::createContainerType);

    public static final DeferredItem<BlazingRod> BLAZING_ROD = ITEMS.register("blazing_rod", tab(BlazingRod::new));

    public static final Supplier<AttachmentType<AgitatorData>> AGITATOR_DATA = ATTACHMENT_TYPES.register(
            "agitator_data", () -> AttachmentType.builder(() -> new AgitatorData(new ArrayList<>()))
                    .serialize(AgitatorData.CODEC.fieldOf("value"))
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AgitatorData>> ITEM_AGITATOR_DATA = COMPONENTS.registerComponentType(
            "agitator_data",
            builder -> builder
                    .persistent(AgitatorData.CODEC)
                    .networkSynchronized(AgitatorData.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlazingRodData>> ITEM_BLAZING_ROD_DATA = COMPONENTS.registerComponentType(
            "blazing_rod_data",
            builder -> builder
                    .persistent(BlazingRodData.CODEC)
                    .networkSynchronized(BlazingRodData.STREAM_CODEC));

    public BlazingModule(IEventBus bus) {
        bus.addListener(this::registerScreens);
    }

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        BlazingAgitatorRenderer.register();
    }

    public void registerScreens(RegisterMenuScreensEvent event) {
        GuiBlazingAgitator.register(event);
        GuiBlazingGenerator.register(event);
        GuiBlazingInfuser.register(event);
    }

    @Override
    public void initConfig(IEventBus bus) {
        BlazingConfiguration.setup(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }

}
