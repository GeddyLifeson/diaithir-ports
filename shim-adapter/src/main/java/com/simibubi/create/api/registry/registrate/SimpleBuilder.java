package com.simibubi.create.api.registry.registrate;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.zurrtum.create.api.registry.SimpleRegistry;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Shim of Create's SimpleBuilder (association by block / block entity; tag/entity/fluid association variants omitted). */
public class SimpleBuilder<R, T extends R, P> extends AbstractBuilder<R, T, P, SimpleBuilder<R, T, P>> {
    private final Supplier<T> value;
    private BiConsumer<Block, R> byBlock;
    private BiConsumer<BlockEntityType<?>, R> byBlockEntity;

    public SimpleBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceKey<? extends Registry<R>> registryKey, Supplier<T> value) {
        super(owner, parent, name, callback, registryKey);
        this.value = value;
    }

    @Override protected T createEntry() { return value.get(); }

    public SimpleBuilder<R, T, P> byBlock(SimpleRegistry<Block, R> registry) { byBlock = registry::register; return this; }
    public SimpleBuilder<R, T, P> byBlock(SimpleRegistry.Multi<Block, R> registry) { byBlock = registry::add; return this; }
    public SimpleBuilder<R, T, P> byBlockEntity(SimpleRegistry<BlockEntityType<?>, R> registry) { byBlockEntity = registry::register; return this; }
    public SimpleBuilder<R, T, P> byBlockEntity(SimpleRegistry.Multi<BlockEntityType<?>, R> registry) { byBlockEntity = registry::add; return this; }

    public SimpleBuilder<R, T, P> associate(Block block) {
        if (byBlock == null) throw new IllegalStateException("This type does not support Block associations");
        onRegister(v -> byBlock.accept(block, v)); return this;
    }
    public SimpleBuilder<R, T, P> associate(BlockEntityType<?> type) {
        if (byBlockEntity == null) throw new IllegalStateException("This type does not support BlockEntityType associations");
        onRegister(v -> byBlockEntity.accept(type, v)); return this;
    }
}
