package com.mrh0.createaddition.reg;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.mrh0.createaddition.CreateAddition;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

/** Direct registry registration (called from the RegisterEvent handler), replacing Registrate. */
public final class R {
    public static final List<Item> TAB = new ArrayList<>();
    private R() {}

    public static Properties softMetal() {
        return Properties.of().strength(5f, 6f).requiresCorrectToolForDrops().sound(SoundType.NETHERITE_BLOCK);
    }
    public static Properties stone() {
        return Properties.of().strength(2f, 6f).requiresCorrectToolForDrops().sound(SoundType.STONE);
    }

    public static <T extends Block> BlockEntry<T> block(String id, Function<Properties, T> f, Properties p) {
        return block(id, f, p, BlockItem::new, new Item.Properties());
    }

    public static <T extends Block, I extends BlockItem> BlockEntry<T> block(String id, Function<Properties, T> f, Properties p,
            BiFunction<T, Item.Properties, I> itemFactory, Item.Properties ip) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, CreateAddition.asResource(id));
        T block = Registry.register(BuiltInRegistries.BLOCK, key, f.apply(p.setId(key)));
        if (itemFactory != null) {
            ResourceKey<Item> ikey = ResourceKey.create(Registries.ITEM, CreateAddition.asResource(id));
            I item = itemFactory.apply(block, ip.setId(ikey).useBlockDescriptionPrefix());
            item.registerBlocks(Item.BY_BLOCK, item);
            Registry.register(BuiltInRegistries.ITEM, ikey, item);
            TAB.add(item);
        }
        return new BlockEntry<>(block);
    }

    public static <T extends Item> ItemEntry<T> item(String id, Function<Item.Properties, T> f) {
        return item(id, f, new Item.Properties());
    }

    public static <T extends Item> ItemEntry<T> item(String id, Function<Item.Properties, T> f, Item.Properties p) {
        ResourceKey<Item> ikey = ResourceKey.create(Registries.ITEM, CreateAddition.asResource(id));
        T item = Registry.register(BuiltInRegistries.ITEM, ikey, f.apply(p.setId(ikey)));
        TAB.add(item);
        return new ItemEntry<>(item);
    }

    public static <T extends BlockEntity> BlockEntityEntry<T> blockEntity(String id, BlockEntityType.BlockEntitySupplier<T> f, Block... valid) {
        BlockEntityType<T> t = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, CreateAddition.asResource(id), new BlockEntityType<>(f, Set.of(valid)));
        return new BlockEntityEntry<>(t);
    }
}
