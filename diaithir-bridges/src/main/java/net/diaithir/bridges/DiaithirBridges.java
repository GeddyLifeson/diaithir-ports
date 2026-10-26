package net.diaithir.bridges;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.BlockItem;
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

import java.util.List;
import java.util.function.Supplier;

/** Tobardealain (magic reagents to Forge Energy), Lòchrandraoidheachd (Forge Energy to a magic reagent) and Aibhnedealain (Forge Energy between tech mods), each in tiers; see {@link Spec}. */
@Mod(DiaithirBridges.MOD)
public final class DiaithirBridges {
    public static final String MOD = "diaithirbridges";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD);
    public static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD);

    /** One block and one item per machine tier, in Spec.ALL order. */
    public static final List<DeferredBlock<BridgeBlock>> TIER_BLOCKS = Spec.ALL.stream()
            .map(sp -> BLOCKS.registerBlock(sp.id(), p -> new BridgeBlock(p, sp), DiaithirBridges::props)).toList();
    public static final List<DeferredItem<BlockItem>> TIER_ITEMS = TIER_BLOCKS.stream().map(ITEMS::registerSimpleBlockItem).toList();
    public static final Supplier<BlockEntityType<BridgeEntity>> ENTITY = ENTITIES.register("bridge",
            () -> new BlockEntityType<>(BridgeEntity::new, TIER_BLOCKS.stream().map(DeferredBlock::get).toArray(Block[]::new)));

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
                TIER_ITEMS.forEach(i -> e.accept(i.get()));
            }
        });
    }
}
