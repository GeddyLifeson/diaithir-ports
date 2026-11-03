package net.diaithir.computing;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

final class Registry {
    static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(DiaithirComputing.ID);
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DiaithirComputing.ID);
    static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DiaithirComputing.ID);

    static final DeferredBlock<ReckonerBlock> RECKONER = BLOCKS.registerBlock("reckoner", ReckonerBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3F, 9F).sound(SoundType.METAL).requiresCorrectToolForDrops());
    static final DeferredItem<net.minecraft.world.item.BlockItem> RECKONER_ITEM = ITEMS.registerSimpleBlockItem("reckoner", RECKONER);
    static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReckonerEntity>> RECKONER_ENTITY =
            ENTITIES.register("reckoner", () -> new BlockEntityType<>(ReckonerEntity::new, RECKONER.get()));

    static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) e.accept(RECKONER_ITEM.get());
        });
    }

    private Registry() {}
}
