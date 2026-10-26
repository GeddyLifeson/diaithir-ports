package net.diaithir.bridges;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/** Tobardealain (magic reagents to Forge Energy, Age VI) and Lòchrandraoidheachd (Forge Energy to a magic reagent, Age VII). */
@Mod(DiaithirBridges.MOD)
public final class DiaithirBridges {
    public static final String MOD = "diaithirbridges";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD);
    public static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD);

    public static final DeferredBlock<Block> TOBARDEALAIN = BLOCKS.registerBlock("tobardealain", p -> new BridgeBlock(p, BridgeEntity.Kind.SOURCE), DiaithirBridges::props);
    public static final DeferredBlock<Block> LOCHRANDRAOIDHEACHD = BLOCKS.registerBlock("lochrandraoidheachd", p -> new BridgeBlock(p, BridgeEntity.Kind.CHARGER), DiaithirBridges::props);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TOBARDEALAIN_ITEM = ITEMS.registerSimpleBlockItem(TOBARDEALAIN);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> LOCHRANDRAOIDHEACHD_ITEM = ITEMS.registerSimpleBlockItem(LOCHRANDRAOIDHEACHD);
    public static final Supplier<BlockEntityType<BridgeEntity>> ENTITY = ENTITIES.register("bridge",
            () -> new BlockEntityType<>(BridgeEntity::new, TOBARDEALAIN.get(), LOCHRANDRAOIDHEACHD.get()));

    private static BlockBehaviour.Properties props() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    public DiaithirBridges(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
        bus.addListener(RegisterCapabilitiesEvent.class, e ->
                e.registerBlockEntity(Capabilities.Energy.BLOCK, ENTITY.get(), (be, side) -> be.energy()));
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                e.accept(TOBARDEALAIN_ITEM.get());
                e.accept(LOCHRANDRAOIDHEACHD_ITEM.get());
            }
        });
    }
}
