package de.ellpeck.actuallyadditions.registration;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Since 1.21.2 block and item properties need their registry id before the block or item is built. Actually Additions
 * builds its properties deep inside constructors, so these registration wrappers remember which id is being built and
 * {@link #withId} stamps it on. Registry suppliers run one at a time on the registering thread.
 */
public final class RegistryIds {
    private static ResourceKey<Block> block;
    private static ResourceKey<Item> item;
    private static boolean blockItem;

    private RegistryIds() {}

    public static <B extends Block> DeferredBlock<B> block(DeferredRegister.Blocks register, String name, Supplier<B> supplier) {
        return register.register(name, id -> {
            block = ResourceKey.create(Registries.BLOCK, id);
            try {
                return supplier.get();
            } finally {
                block = null;
            }
        });
    }

    public static <I extends Item> DeferredItem<I> item(DeferredRegister.Items register, String name, Supplier<I> supplier) {
        return item(register, name, supplier, false);
    }

    /** A block's item: named after the block ({@code block.<mod>.<name>}), as before 1.21.2. */
    public static <I extends Item> DeferredItem<I> blockItem(DeferredRegister.Items register, String name, Supplier<I> supplier) {
        return item(register, name, supplier, true);
    }

    private static <I extends Item> DeferredItem<I> item(DeferredRegister.Items register, String name, Supplier<I> supplier, boolean forBlock) {
        return register.register(name, id -> {
            item = ResourceKey.create(Registries.ITEM, id);
            blockItem = forBlock;
            try {
                return supplier.get();
            } finally {
                item = null;
            }
        });
    }

    public static BlockBehaviour.Properties withId(BlockBehaviour.Properties properties) {
        return block == null ? properties : properties.setId(block);
    }

    public static Item.Properties withId(Item.Properties properties) {
        if (item == null) return properties;
        properties.setId(item);
        return blockItem ? properties.useBlockDescriptionPrefix() : properties;
    }
}
