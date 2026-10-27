package com.github.alexmodguy.alexscaves.server.misc;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 26.1 needs a registry id on every Block/Item.Properties.  The original code builds properties inline inside registry
 * suppliers, so the register helpers publish the id being registered here and the property factories pick it up.
 */
public final class ACProps {
    private static final ThreadLocal<String> CURRENT_BLOCK = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_ITEM = new ThreadLocal<>();

    private ACProps() {}

    public static ResourceKey<Block> blockKey(String name) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(AlexsCaves.MODID, name));
    }

    public static ResourceKey<Item> itemKey(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AlexsCaves.MODID, name));
    }

    public static void setBlock(String name) { if (name == null) CURRENT_BLOCK.remove(); else CURRENT_BLOCK.set(name); }

    public static void setItem(String name) { if (name == null) CURRENT_ITEM.remove(); else CURRENT_ITEM.set(name); }

    public static BlockBehaviour.Properties block() {
        return withId(com.github.alexmodguy.alexscaves.server.misc.ACProps.block());
    }

    public static BlockBehaviour.Properties blockCopy(BlockBehaviour source) {
        return withId(com.github.alexmodguy.alexscaves.server.misc.ACProps.blockCopy(source));
    }

    public static BlockBehaviour.Properties withId(BlockBehaviour.Properties p) {
        String n = CURRENT_BLOCK.get();
        return n == null ? p : p.setId(blockKey(n));
    }

    public static Item.Properties item() {
        Item.Properties p = com.github.alexmodguy.alexscaves.server.misc.ACProps.item();
        String n = CURRENT_ITEM.get();
        return n == null ? p : p.setId(itemKey(n));
    }

    /** ValueInput has no contains(); a passthrough read is present exactly when the key exists. */
    public static boolean has(net.minecraft.world.level.storage.ValueInput input, String key) {
        return input.read(key, com.mojang.serialization.Codec.PASSTHROUGH).isPresent();
    }

    public static boolean has(net.minecraft.nbt.CompoundTag tag, String key) {
        return tag.contains(key);
    }

    public static java.util.Optional<net.minecraft.world.entity.EntityType<?>> entityTypeOf(net.minecraft.nbt.CompoundTag tag) {
        String id = tag.getStringOr("id", "");
        Identifier key = Identifier.tryParse(id);
        if (key == null) return java.util.Optional.empty();
        return net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getOptional(key);
    }

    public static net.minecraft.nbt.CompoundTag serializeEntity(net.minecraft.world.entity.Entity entity) {
        net.minecraft.world.level.storage.TagValueOutput out = net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                net.minecraft.util.ProblemReporter.DISCARDING, entity.registryAccess());
        entity.save(out);
        return out.buildResult();
    }
}
