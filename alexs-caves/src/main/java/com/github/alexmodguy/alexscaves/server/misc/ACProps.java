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
        return withId(BlockBehaviour.Properties.of());
    }

    public static BlockBehaviour.Properties blockCopy(BlockBehaviour source) {
        return withId(BlockBehaviour.Properties.ofFullCopy(source));
    }

    public static BlockBehaviour.Properties withId(BlockBehaviour.Properties p) {
        String n = CURRENT_BLOCK.get();
        return n == null ? p : p.setId(blockKey(n));
    }

    public static Item.Properties item() {
        Item.Properties p = new Item.Properties();
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

    public static net.minecraft.world.entity.ai.targeting.TargetingConditions.Selector sel(java.util.function.Predicate<net.minecraft.world.entity.LivingEntity> p) {
        return p == null ? null : (t, l) -> p.test(t);
    }

    public static boolean rule(net.minecraft.world.level.Level level, net.minecraft.world.level.gamerules.GameRule<Boolean> rule) {
        return !(level instanceof net.minecraft.server.level.ServerLevel serverLevel) || serverLevel.getGameRules().get(rule);
    }

    public static boolean canGrief(net.minecraft.world.level.Level level, net.minecraft.world.entity.Entity entity) {
        return level instanceof net.minecraft.server.level.ServerLevel serverLevel && net.neoforged.neoforge.event.EventHooks.canEntityGrief(serverLevel, entity);
    }

    public static void blockParticle(net.minecraft.world.level.Level level, net.minecraft.core.particles.ParticleOptions options, boolean force, double x, double y, double z, double xd, double yd, double zd) {
        level.addParticle(options, force, false, x, y, z, xd, yd, zd);
    }
}
