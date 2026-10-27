package com.simibubi.create.foundation.data;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockEntityBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Shim of Create's CreateBlockEntityBuilder. visual(...) is accepted and ignored (flywheel visuals are client-only). */
public class CreateBlockEntityBuilder<T extends BlockEntity, P> extends BlockEntityBuilder<T, P> {
    private final List<NonNullSupplier<? extends Collection<NonNullSupplier<? extends Block>>>> deferredValidBlocks = new ArrayList<>();

    public static <T extends BlockEntity, P> BlockEntityBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, BlockEntityFactory<T> factory) {
        return new CreateBlockEntityBuilder<>(owner, parent, name, callback, factory);
    }

    protected CreateBlockEntityBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, BlockEntityFactory<T> factory) {
        super(owner, parent, name, callback, factory);
    }

    public CreateBlockEntityBuilder<T, P> validBlocksDeferred(NonNullSupplier<? extends Collection<NonNullSupplier<? extends Block>>> blocks) {
        deferredValidBlocks.add(blocks);
        return this;
    }

    @Override
    protected BlockEntityType<T> createEntry() {
        deferredValidBlocks.stream().map(Supplier::get).flatMap(Collection::stream).forEach(this::validBlock);
        return super.createEntry();
    }

    public CreateBlockEntityBuilder<T, P> displaySource(RegistryEntry<DisplaySource, ? extends DisplaySource> source) {
        this.onRegisterAfter(CreateRegistryKeys.DISPLAY_SOURCE, type -> DisplaySource.BY_BLOCK_ENTITY.add(type, source.get()));
        return this;
    }

    public CreateBlockEntityBuilder<T, P> displayTarget(RegistryEntry<DisplayTarget, ? extends DisplayTarget> target) {
        this.onRegisterAfter(CreateRegistryKeys.DISPLAY_TARGET, type -> DisplayTarget.BY_BLOCK_ENTITY.register(type, target.get()));
        return this;
    }

    public CreateBlockEntityBuilder<T, P> visual(NonNullSupplier<?> visualFactory) { return this; }
    public CreateBlockEntityBuilder<T, P> visual(NonNullSupplier<?> visualFactory, boolean renderNormally) { return this; }
    public CreateBlockEntityBuilder<T, P> visual(NonNullSupplier<?> visualFactory, Predicate<?> renderNormally) { return this; }
}
