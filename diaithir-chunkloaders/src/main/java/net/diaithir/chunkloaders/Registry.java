package net.diaithir.chunkloaders;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
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

import java.util.ArrayList;
import java.util.List;

final class Registry {
    static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(DiaithirChunkLoaders.ID);
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DiaithirChunkLoaders.ID);
    static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DiaithirChunkLoaders.ID);

    static final List<DeferredBlock<Block>> LOADERS = new ArrayList<>();
    static final List<DeferredItem<Item>> LOADER_ITEMS = new ArrayList<>();
    static final DeferredItem<Item> CART_SEAL;
    static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LoaderBlockEntity>> LOADER_ENTITY;

    static {
        for (int t = 1; t <= 3; t++) {
            final int tier = t;
            DeferredBlock<Block> b = BLOCKS.registerBlock("chunk_loader_" + t, p -> new LoaderBlock(p, tier), () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(4F + tier, 12F).sound(SoundType.METAL).requiresCorrectToolForDrops().lightLevel(s -> s.getValue(LoaderBlock.LIT) ? 9 : 0));
            LOADERS.add(b);
            LOADER_ITEMS.add(ITEMS.registerItem("chunk_loader_" + t, p -> new LoaderItem(b.get(), p.useBlockDescriptionPrefix(), tier)));
        }
        CART_SEAL = ITEMS.registerItem("cart_seal", CartSealItem::new);
        LOADER_ENTITY = ENTITIES.register("loader", () -> new BlockEntityType<>(LoaderBlockEntity::new, LOADERS.stream().map(DeferredBlock::get).toArray(Block[]::new)));
    }

    static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) LOADER_ITEMS.forEach(i -> e.accept(i.get()));
            else if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) e.accept(CART_SEAL.get());
        });
    }

    private Registry() {}
}
