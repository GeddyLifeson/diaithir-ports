package mcjty.rftoolsbuilder.modules.builder;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.modules.builder.blocks.SpaceChamberControllerBlock;
import mcjty.rftoolsbuilder.modules.builder.blocks.SpaceChamberControllerTileEntity;
import mcjty.rftoolsbuilder.modules.builder.blocks.SupportBlock;
import mcjty.rftoolsbuilder.modules.builder.client.BuilderRenderer;
import mcjty.rftoolsbuilder.modules.builder.client.GuiBuilder;
import mcjty.rftoolsbuilder.modules.builder.data.BuilderData;
import mcjty.rftoolsbuilder.modules.builder.data.ChamberControllerData;
import mcjty.rftoolsbuilder.modules.builder.data.ShapeCardData;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardType;
import mcjty.rftoolsbuilder.modules.builder.items.SpaceChamberCardItem;
import mcjty.rftoolsbuilder.modules.builder.items.SuperHarvestingTool;
import mcjty.rftoolsbuilder.setup.Config;
import mcjty.rftoolsbuilder.setup.Registration;
import mcjty.rftoolsbase.tools.ManualHelper;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Supplier;

import static mcjty.rftoolsbuilder.RFToolsBuilder.MODID;
import static mcjty.rftoolsbuilder.RFToolsBuilder.tab;
import static mcjty.rftoolsbuilder.setup.Registration.*;

public class BuilderModule implements IModule {

    public static final DeferredBlock<SupportBlock> SUPPORT = BLOCKS.register("support_block", SupportBlock::new);

    public static final DeferredBlock<BaseBlock> SPACE_CHAMBER = BLOCKS.register("space_chamber", () -> new BaseBlock(new BlockBuilder()
            .manualEntry(ManualHelper.create("rftoolsbuilder:builder/space_chambers"))
        .properties(BlockBehaviour.Properties.of()
                .strength(2.0f)
                .sound(SoundType.METAL)
                .noOcclusion())));
    public static final DeferredItem<Item> SPACE_CHAMBER_ITEM = ITEMS.register("space_chamber", tab(() -> new BlockItem(SPACE_CHAMBER.get(), Registration.createStandardProperties())));

    public static final DeferredBlock<SpaceChamberControllerBlock> SPACE_CHAMBER_CONTROLLER = BLOCKS.register("space_chamber_controller", SpaceChamberControllerBlock::new);
    public static final DeferredItem<Item> SPACE_CHAMBER_CONTROLLER_ITEM = ITEMS.register("space_chamber_controller", tab(() -> new BlockItem(SPACE_CHAMBER_CONTROLLER.get(), Registration.createStandardProperties())));
    public static final Supplier<BlockEntityType<SpaceChamberControllerTileEntity>> TYPE_SPACE_CHAMBER_CONTROLLER = TILES.register("space_chamber_controller", () -> new BlockEntityType<>(SpaceChamberControllerTileEntity::new, SPACE_CHAMBER_CONTROLLER.get()));

    public static final DeferredItem<Item> SPACE_CHAMBER_CARD = ITEMS.register("space_chamber_card", tab(SpaceChamberCardItem::new));

    public static final RBlock<BaseBlock, BlockItem, BuilderTileEntity> BUILDER = RBLOCKS.registerBlock("builder",
            BuilderTileEntity.class,
            BuilderTileEntity::createBlock,
            block -> new BlockItem(block.get(), createStandardProperties()),
            BuilderTileEntity::new
    );
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_BUILDER = CONTAINERS.register("builder", GenericContainer::createContainerType);

    public static final DeferredItem<Item> SUPER_HARVESTING_TOOL = ITEMS.register("superharvestingtool", SuperHarvestingTool::new);

    public static final DeferredItem<ShapeCardItem> SHAPE_CARD_DEF = ITEMS.register("shape_card_def", tab(() -> new ShapeCardItem(ShapeCardType.CARD_SHAPE)));
    public static final DeferredItem<ShapeCardItem> SHAPE_CARD_LIQUID = ITEMS.register("shape_card_liquid", tab(() -> new ShapeCardItem(ShapeCardType.CARD_PUMP_LIQUID)));
    public static final DeferredItem<ShapeCardItem> SHAPE_CARD_PUMP = ITEMS.register("shape_card_pump", tab(() -> new ShapeCardItem(ShapeCardType.CARD_PUMP)));
    public static final DeferredItem<ShapeCardItem> SHAPE_CARD_PUMP_CLEAR = ITEMS.register("shape_card_pump_clear", tab(() -> new ShapeCardItem(ShapeCardType.CARD_PUMP_CLEAR)));
    public static final DeferredItem<ShapeCardItem> SHAPE_CARD_QUARRY = ITEMS.register("shape_card_quarry", tab(() -> new ShapeCardItem(ShapeCardType.CARD_QUARRY)));
    public static final DeferredItem<ShapeCardItem> SHAPE_CARD_QUARRY_CLEAR = ITEMS.register("shape_card_quarry_clear", tab(() -> new ShapeCardItem(ShapeCardType.CARD_QUARRY_CLEAR)));
    public static final DeferredItem<ShapeCardItem> SHAPE_CARD_QUARRY_CLEAR_FORTUNE = ITEMS.register("shape_card_quarry_clear_fortune", tab(() -> new ShapeCardItem(ShapeCardType.CARD_QUARRY_CLEAR_FORTUNE)));
    public static final DeferredItem<ShapeCardItem> SHAPE_CARD_QUARRY_CLEAR_SILK = ITEMS.register("shape_card_quarry_clear_silk", tab(() -> new ShapeCardItem(ShapeCardType.CARD_QUARRY_CLEAR_SILK)));
    public static final DeferredItem<ShapeCardItem> SHAPE_CARD_QUARRY_FORTUNE = ITEMS.register("shape_card_quarry_fortune", tab(() -> new ShapeCardItem(ShapeCardType.CARD_QUARRY_FORTUNE)));
    public static final DeferredItem<ShapeCardItem> SHAPE_CARD_QUARRY_SILK = ITEMS.register("shape_card_quarry_silk", tab(() -> new ShapeCardItem(ShapeCardType.CARD_QUARRY_SILK)));
    public static final DeferredItem<ShapeCardItem> SHAPE_CARD_VOID = ITEMS.register("shape_card_void", tab(() -> new ShapeCardItem(ShapeCardType.CARD_VOID)));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ShapeCardData>> ITEM_SHAPECARD_DATA = COMPONENTS.registerComponentType(
            "shapecard_data",
            builder -> builder
                    .persistent(ShapeCardData.CODEC)
                    .networkSynchronized(ShapeCardData.STREAM_CODEC));

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BuilderData>> BUILDER_DATA = ATTACHMENT_TYPES.register(
            "builder_data", () -> AttachmentType.builder(() -> BuilderData.DEFAULT)
                    .serialize(BuilderData.CODEC.fieldOf("value"))
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BuilderData>> ITEM_BUILDER_DATA = COMPONENTS.registerComponentType(
            "builder_data",
            builder -> builder
                    .persistent(BuilderData.CODEC)
                    .networkSynchronized(BuilderData.STREAM_CODEC));

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ChamberControllerData>> CHAMBER_DATA = ATTACHMENT_TYPES.register(
            "chamber_data", () -> AttachmentType.builder(() -> ChamberControllerData.DEFAULT)
                    .serialize(ChamberControllerData.CODEC.fieldOf("value"))
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ChamberControllerData>> ITEM_CHAMBER_DATA = COMPONENTS.registerComponentType(
            "chamber_data",
            builder -> builder
                    .persistent(ChamberControllerData.CODEC)
                    .networkSynchronized(ChamberControllerData.STREAM_CODEC));

    public BuilderModule(IEventBus bus) {
        bus.addListener(this::registerMenuScreens);
        bus.addListener(this::onRegisterTicketController);
    }

    @Override
    public void init(FMLCommonSetupEvent event) {
    }

    private void onRegisterTicketController(RegisterTicketControllersEvent event) {
        RFToolsBuilder.setup.ticketController = new TicketController(Identifier.fromNamespaceAndPath(MODID, "builder"));
        event.register(RFToolsBuilder.setup.ticketController);
    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        BuilderRenderer.register();
    }

    public void registerMenuScreens(RegisterMenuScreensEvent event) {
        GuiBuilder.register(event);
    }


    @Override
    public void initConfig(IEventBus bus) {
        BuilderConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }

}
