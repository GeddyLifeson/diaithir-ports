package mcjty.lib.setup;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

/**
 * Since 1.21.2 block and item properties need their registry id before the block or item is built. McJty mods build
 * their properties inside constructors, so {@link DeferredBlocks} and {@link DeferredItems} remember which id is being
 * built and {@link #withId} stamps it on. Registry suppliers run one at a time on the registering thread.
 */
public final class RegistryIds {

    private static ResourceKey<Block> block;
    private static ResourceKey<Item> item;

    private RegistryIds() {
    }

    static <T extends Block> T block(Identifier id, Supplier<T> supplier) {
        block = ResourceKey.create(Registries.BLOCK, id);
        try {
            return supplier.get();
        } finally {
            block = null;
        }
    }

    static <T extends Item> T item(Identifier id, Supplier<T> supplier) {
        item = ResourceKey.create(Registries.ITEM, id);
        try {
            return supplier.get();
        } finally {
            item = null;
        }
    }

    public static BlockBehaviour.Properties withId(BlockBehaviour.Properties properties) {
        return block == null ? properties : properties.setId(block);
    }

    /** An item named like a block is that block's item and keeps the block's name ({@code block.<mod>.<name>}). */
    public static Item.Properties withId(Item.Properties properties) {
        if (item == null) {
            return properties;
        }
        properties.setId(item);
        return BuiltInRegistries.BLOCK.containsKey(item.identifier()) ? properties.useBlockDescriptionPrefix() : properties;
    }
}
