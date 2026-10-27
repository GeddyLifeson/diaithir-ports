package com.simibubi.create.foundation.data;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.EntityBuilder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/** Shim of Create's CreateEntityBuilder (visual(...) accepted and ignored). */
public class CreateEntityBuilder<T extends Entity, P> extends EntityBuilder<T, P> {
    public static <T extends Entity, P> EntityBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, EntityType.EntityFactory<T> factory, MobCategory classification) {
        return new CreateEntityBuilder<>(owner, parent, name, callback, factory, classification).defaultLang();
    }
    public CreateEntityBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, EntityType.EntityFactory<T> factory, MobCategory classification) {
        super(owner, parent, name, callback, factory, classification);
    }
    public CreateEntityBuilder<T, P> visual(NonNullSupplier<?> f) { return this; }
    public CreateEntityBuilder<T, P> visual(NonNullSupplier<?> f, boolean b) { return this; }
    public CreateEntityBuilder<T, P> visual(NonNullSupplier<?> f, Predicate<?> p) { return this; }
}
