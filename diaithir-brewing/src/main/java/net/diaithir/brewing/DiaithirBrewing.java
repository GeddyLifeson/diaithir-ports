package net.diaithir.brewing;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(DiaithirBrewing.ID)
public final class DiaithirBrewing {
    public static final String ID = "diaithirbrewing";

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);

    public static final DeferredBlock<BrewVesselBlock> KEG = BLOCKS.registerBlock("keg",
        p -> new BrewVesselBlock(p, "keg", 6), () -> props().strength(2.0F));
    public static final DeferredBlock<BrewVesselBlock> FERMENTING_BARREL = BLOCKS.registerBlock("fermenting_barrel",
        p -> new BrewVesselBlock(p, "fermenting_barrel", 12), () -> props().strength(2.5F));
    public static final DeferredBlock<BrewVesselBlock> OAK_CASK = BLOCKS.registerBlock("oak_cask",
        p -> new BrewVesselBlock(p, "oak_cask", 8), () -> props().strength(3.0F));
    public static final DeferredItem<BlockItem> OAK_CASK_ITEM = ITEMS.registerSimpleBlockItem(OAK_CASK);
    public static final DeferredItem<BlockItem> KEG_ITEM = ITEMS.registerSimpleBlockItem(KEG);
    public static final DeferredItem<BlockItem> FERMENTING_BARREL_ITEM = ITEMS.registerSimpleBlockItem(FERMENTING_BARREL);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BrewVesselEntity>> VESSEL_ENTITY = ENTITIES.register("vessel",
        () -> new BlockEntityType<>(BrewVesselEntity::new, KEG.get(), FERMENTING_BARREL.get(), OAK_CASK.get()));

    /** The block for a recipe station name. */
    static DeferredBlock<BrewVesselBlock> block(String station) {
        return switch (station) {
            case "keg" -> KEG;
            case "oak_cask" -> OAK_CASK;
            default -> FERMENTING_BARREL;
        };
    }

    private static BlockBehaviour.Properties props() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).noOcclusion().ignitedByLava();
    }

    public DiaithirBrewing(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
        NeoForge.EVENT_BUS.addListener(AddServerReloadListenersEvent.class,
            e -> e.addListener(Identifier.fromNamespaceAndPath(ID, "recipes"), new BrewRecipes()));
        bus.addListener(RegisterCapabilitiesEvent.class,
            e -> {
                e.registerBlockEntity(Capabilities.Item.BLOCK, VESSEL_ENTITY.get(), (be, side) -> be.automation);
                e.registerBlockEntity(Capabilities.Fluid.BLOCK, VESSEL_ENTITY.get(), (be, side) -> be.fluidIntake);
            });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> BrewSelfTest.register(e.getDispatcher()));
    }
}
