package mcjty.rftoolsbuilder.modules.shield;

import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbuilder.modules.shield.blocks.*;
import mcjty.rftoolsbuilder.modules.shield.client.GuiShield;
import mcjty.rftoolsbuilder.modules.shield.data.ShieldData;
import mcjty.rftoolsbuilder.setup.Config;
import mcjty.rftoolsbuilder.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static mcjty.rftoolsbuilder.RFToolsBuilder.tab;
import static mcjty.rftoolsbuilder.setup.Registration.*;


public class ShieldModule implements IModule {

    public static final RBlock<ShieldProjectorBlock, BlockItem, ShieldProjectorTileEntity> SHIELD_BLOCK1 = RBLOCKS.registerBlock("shield_block1",
            ShieldProjectorTileEntity.class,
            () -> new ShieldProjectorBlock(ShieldModule::createProjector1, ShieldConfiguration.maxShieldSize),
            block -> new BlockItem(block.get(), createStandardProperties()),
            ShieldModule::createProjector1);
    public static final RBlock<ShieldProjectorBlock, BlockItem, ShieldProjectorTileEntity> SHIELD_BLOCK2 = RBLOCKS.registerBlock("shield_block2",
            ShieldProjectorTileEntity.class,
            () -> new ShieldProjectorBlock(ShieldModule::createProjector2, () -> ShieldConfiguration.maxShieldSize.get() * 4),
            block -> new BlockItem(block.get(), createStandardProperties()),
            ShieldModule::createProjector2);
    public static final RBlock<ShieldProjectorBlock, BlockItem, ShieldProjectorTileEntity> SHIELD_BLOCK3 = RBLOCKS.registerBlock("shield_block3",
            ShieldProjectorTileEntity.class,
            () -> new ShieldProjectorBlock(ShieldModule::createProjector3, () -> ShieldConfiguration.maxShieldSize.get() * 16),
            block -> new BlockItem(block.get(), createStandardProperties()),
            ShieldModule::createProjector3);
    public static final RBlock<ShieldProjectorBlock, BlockItem, ShieldProjectorTileEntity> SHIELD_BLOCK4 = RBLOCKS.registerBlock("shield_block4",
            ShieldProjectorTileEntity.class,
            () -> new ShieldProjectorBlock(ShieldModule::createProjector4, () -> ShieldConfiguration.maxShieldSize.get() * 128),
            block -> new BlockItem(block.get(), createStandardProperties()),
            ShieldModule::createProjector4);
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_SHIELD = CONTAINERS.register("shield", GenericContainer::createContainerType);

    public static final DeferredBlock<ShieldTemplateBlock> TEMPLATE_BLUE = BLOCKS.register("blue_shield_template_block", () -> new ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor.BLUE));
    public static final DeferredBlock<ShieldTemplateBlock> TEMPLATE_RED = BLOCKS.register("red_shield_template_block", () -> new ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor.RED));
    public static final DeferredBlock<ShieldTemplateBlock> TEMPLATE_GREEN = BLOCKS.register("green_shield_template_block", () -> new ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor.GREEN));
    public static final DeferredBlock<ShieldTemplateBlock> TEMPLATE_YELLOW = BLOCKS.register("yellow_shield_template_block", () -> new ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor.YELLOW));

    public static final DeferredItem<Item> TEMPLATE_BLUE_ITEM = ITEMS.register("blue_shield_template_block", tab(() -> new BlockItem(TEMPLATE_BLUE.get(), Registration.createStandardProperties())));
    public static final DeferredItem<Item> TEMPLATE_RED_ITEM = ITEMS.register("red_shield_template_block", tab(() -> new BlockItem(TEMPLATE_RED.get(), Registration.createStandardProperties())));
    public static final DeferredItem<Item> TEMPLATE_GREEN_ITEM = ITEMS.register("green_shield_template_block", tab(() -> new BlockItem(TEMPLATE_GREEN.get(), Registration.createStandardProperties())));
    public static final DeferredItem<Item> TEMPLATE_YELLOW_ITEM = ITEMS.register("yellow_shield_template_block", tab(() -> new BlockItem(TEMPLATE_YELLOW.get(), Registration.createStandardProperties())));

    public static final DeferredBlock<ShieldingBlock> SHIELDING_SOLID = BLOCKS.register("shielding_solid", ShieldingBlock::new);
    public static final DeferredBlock<ShieldingBlock> SHIELDING_TRANSLUCENT = BLOCKS.register("shielding_translucent", ShieldingBlock::new);
    public static final DeferredBlock<ShieldingBlock> SHIELDING_CUTOUT = BLOCKS.register("shielding_cutout", ShieldingBlock::new);
    public static final Supplier<BlockEntityType<?>> TYPE_SHIELDING = TILES.register("shielding", () -> new BlockEntityType<>(ShieldingTileEntity::new,
            SHIELDING_SOLID.get(), SHIELDING_TRANSLUCENT.get(), SHIELDING_CUTOUT.get()));

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ShieldData>> SHIELD_DATA = ATTACHMENT_TYPES.register(
            "shield_data", () -> AttachmentType.builder(() -> ShieldData.DEFAULT)
                    .serialize(ShieldData.CODEC.fieldOf("value"))
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ShieldData>> ITEM_SHIELD_DATA = COMPONENTS.registerComponentType(
            "shield_data",
            builder -> builder
                    .persistent(ShieldData.CODEC)
                    .networkSynchronized(ShieldData.STREAM_CODEC));

    @Nonnull
    public static ShieldProjectorTileEntity createProjector1(BlockPos pos, BlockState state) {
        return new ShieldProjectorTileEntity(SHIELD_BLOCK1.be().get(), pos, state, ShieldConfiguration.maxShieldSize.get(), ShieldConfiguration.MAXENERGY.get(), ShieldConfiguration.RECEIVEPERTICK.get());
    }

    @Nonnull
    public static ShieldProjectorTileEntity createProjector2(BlockPos pos, BlockState state) {
        return new ShieldProjectorTileEntity(SHIELD_BLOCK2.be().get(), pos, state, ShieldConfiguration.maxShieldSize.get() * 4, ShieldConfiguration.MAXENERGY.get(), ShieldConfiguration.RECEIVEPERTICK.get());
    }

    @Nonnull
    public static ShieldProjectorTileEntity createProjector3(BlockPos pos, BlockState state) {
        return new ShieldProjectorTileEntity(SHIELD_BLOCK3.be().get(), pos, state, ShieldConfiguration.maxShieldSize.get() * 16, ShieldConfiguration.MAXENERGY.get() * 3, ShieldConfiguration.RECEIVEPERTICK.get() * 2)
                .setDamageFactor(4.0f)
                .setCostFactor(2.0f);
    }

    @Nonnull
    public static ShieldProjectorTileEntity createProjector4(BlockPos pos, BlockState state) {
        return new ShieldProjectorTileEntity(SHIELD_BLOCK4.be().get(), pos, state, ShieldConfiguration.maxShieldSize.get() * 128, ShieldConfiguration.MAXENERGY.get() * 6, ShieldConfiguration.RECEIVEPERTICK.get() * 6)
                .setDamageFactor(4.0f)
                .setCostFactor(2.0f);
    }

//
//    @SideOnly(Side.CLIENT)
//    public static void initClientPost() {
//        if (!ShieldConfiguration.disableShieldBlocksToUncorruptWorld.get()) {
//            solidShieldBlock.initBlockColors();
//            noTickSolidShieldBlock.initBlockColors();
//            solidShieldBlockOpaque.initBlockColors();
//            noTickSolidShieldBlockOpaque.initBlockColors();
//        }
//    }
//
//    @SideOnly(Side.CLIENT)
//    public static void initColorHandlers(BlockColors blockColors) {
//        if (!ShieldConfiguration.disableShieldBlocksToUncorruptWorld.get()) {
//            camoShieldBlock.initColorHandler(blockColors);
//            noTickCamoShieldBlock.initColorHandler(blockColors);
//            camoShieldBlockOpaque.initColorHandler(blockColors);
//            noTickCamoShieldBlockOpaque.initColorHandler(blockColors);
//        }
//    }


    public ShieldModule(IEventBus bus, Dist dist) {
        bus.addListener(this::registerMenuScreens);
    }

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
    }

    public void registerMenuScreens(RegisterMenuScreensEvent event) {
        GuiShield.register(event);
    }

    @Override
    public void initConfig(IEventBus bus) {
        ShieldConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }

}
