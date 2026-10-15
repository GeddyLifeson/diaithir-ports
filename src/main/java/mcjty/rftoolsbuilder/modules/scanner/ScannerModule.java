package mcjty.rftoolsbuilder.modules.scanner;

import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ComposerBlock;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ComposerTileEntity;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ProjectorBlock;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ProjectorTileEntity;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ScannerBlock;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ScannerTileEntity;
import mcjty.rftoolsbuilder.modules.scanner.client.GuiComposer;
import mcjty.rftoolsbuilder.modules.scanner.client.GuiProjector;
import mcjty.rftoolsbuilder.modules.scanner.client.GuiScanner;
import mcjty.rftoolsbuilder.modules.scanner.client.ProjectorRenderer;
import mcjty.rftoolsbuilder.setup.Config;
import mcjty.rftoolsbuilder.setup.Registration;
import mcjty.rftoolsbuilder.shapes.ShapeDataManagerClient;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.function.Supplier;

import static mcjty.rftoolsbuilder.setup.Registration.CONTAINERS;
import static mcjty.rftoolsbuilder.setup.Registration.RBLOCKS;

public class ScannerModule implements IModule {

    public static final RBlock<ScannerBlock, BlockItem, ScannerTileEntity> SCANNER = RBLOCKS.registerBlock("scanner",
            ScannerTileEntity.class,
            ScannerBlock::new,
            block -> new BlockItem(block.get(), Registration.createStandardProperties()),
            ScannerTileEntity::new);
    public static final Supplier<BlockEntityType<ScannerTileEntity>> TYPE_SCANNER = SCANNER.be();
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_SCANNER = CONTAINERS.register("scanner", GenericContainer::createContainerType);

    public static final RBlock<ComposerBlock, BlockItem, ComposerTileEntity> COMPOSER = RBLOCKS.registerBlock("composer",
            ComposerTileEntity.class,
            ComposerBlock::new,
            block -> new BlockItem(block.get(), Registration.createStandardProperties()),
            ComposerTileEntity::new);
    public static final Supplier<BlockEntityType<ComposerTileEntity>> TYPE_COMPOSER = COMPOSER.be();
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_COMPOSER = CONTAINERS.register("composer", GenericContainer::createContainerType);

    public static final RBlock<ProjectorBlock, BlockItem, ProjectorTileEntity> PROJECTOR = RBLOCKS.registerBlock("projector",
            ProjectorTileEntity.class,
            ProjectorBlock::new,
            block -> new BlockItem(block.get(), Registration.createStandardProperties()),
            ProjectorTileEntity::new);
    public static final Supplier<BlockEntityType<ProjectorTileEntity>> TYPE_PROJECTOR = PROJECTOR.be();
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_PROJECTOR = CONTAINERS.register("projector", GenericContainer::createContainerType);

    public ScannerModule(IEventBus bus) {
        bus.addListener(this::registerMenuScreens);
    }

    @Override
    public void init(FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.register(new ShapeHandler());
    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(ShapeDataManagerClient::cleanupOldRenderers);
        ProjectorRenderer.register();
    }

    public void registerMenuScreens(RegisterMenuScreensEvent event) {
        GuiComposer.register(event);
        GuiProjector.register(event);
        GuiScanner.register(event);
    }

    @Override
    public void initConfig(IEventBus bus) {
        ScannerConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }

}
