package com.simibubi.create.foundation.data;

import com.simibubi.create.api.registry.registrate.SimpleBuilder;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockEntityBuilder.BlockEntityFactory;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Shim of Create's CreateRegistrate for the diaithir_create port (clean-room over the real Registrate 26.1).
 * Client-only features (tooltip modifiers, connected textures, casing connectivity, model swapping) are accepted and ignored on this layer.
 */
public class CreateRegistrate extends AbstractRegistrate<CreateRegistrate> {
    protected Function<Item, ?> currentTooltipModifierFactory;
    protected DeferredHolder<CreativeModeTab, CreativeModeTab> currentTab;

    protected CreateRegistrate(String modid) { super(modid); }

    public static CreateRegistrate create(String modid) { return new CreateRegistrate(modid); }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public CreateRegistrate setTooltipModifierFactory(Function factory) { currentTooltipModifierFactory = factory; return self(); }

    public CreateRegistrate setCreativeTab(DeferredHolder<CreativeModeTab, CreativeModeTab> tab) { currentTab = tab; return self(); }
    public DeferredHolder<CreativeModeTab, CreativeModeTab> getCreativeTab() { return currentTab; }

    @Override
    public CreateRegistrate registerEventListeners(IEventBus bus) { return super.registerEventListeners(bus); }

    @Override
    public <T extends BlockEntity> CreateBlockEntityBuilder<T, CreateRegistrate> blockEntity(String name, BlockEntityFactory<T> factory) {
        return blockEntity(self(), name, factory);
    }

    @Override
    public <T extends BlockEntity, P> CreateBlockEntityBuilder<T, P> blockEntity(P parent, String name, BlockEntityFactory<T> factory) {
        return (CreateBlockEntityBuilder<T, P>) entry(name, cb -> CreateBlockEntityBuilder.create(this, parent, name, cb, factory));
    }

    @Override
    public <T extends Entity> CreateEntityBuilder<T, CreateRegistrate> entity(String name, EntityType.EntityFactory<T> factory, MobCategory classification) {
        return this.entity(self(), name, factory, classification);
    }

    @Override
    public <T extends Entity, P> CreateEntityBuilder<T, P> entity(P parent, String name, EntityType.EntityFactory<T> factory, MobCategory classification) {
        return (CreateEntityBuilder<T, P>) this.entry(name, cb -> CreateEntityBuilder.create(this, parent, name, cb, factory, classification));
    }

    public <T extends MountedItemStorageType<?>> SimpleBuilder<MountedItemStorageType<?>, T, CreateRegistrate> mountedItemStorage(String name, Supplier<T> supplier) {
        return this.entry(name, cb -> new SimpleBuilder<>(this, this, name, cb, CreateRegistryKeys.MOUNTED_ITEM_STORAGE_TYPE, supplier)
            .byBlock(MountedItemStorageType.REGISTRY));
    }

    public <T extends MountedFluidStorageType<?>> SimpleBuilder<MountedFluidStorageType<?>, T, CreateRegistrate> mountedFluidStorage(String name, Supplier<T> supplier) {
        return this.entry(name, cb -> new SimpleBuilder<>(this, this, name, cb, CreateRegistryKeys.MOUNTED_FLUID_STORAGE_TYPE, supplier)
            .byBlock(MountedFluidStorageType.REGISTRY));
    }

    public <T extends DisplaySource> SimpleBuilder<DisplaySource, T, CreateRegistrate> displaySource(String name, Supplier<T> supplier) {
        return this.entry(name, cb -> new SimpleBuilder<>(this, this, name, cb, CreateRegistryKeys.DISPLAY_SOURCE, supplier)
            .byBlock(DisplaySource.BY_BLOCK).byBlockEntity(DisplaySource.BY_BLOCK_ENTITY));
    }

    public <T extends DisplayTarget> SimpleBuilder<DisplayTarget, T, CreateRegistrate> displayTarget(String name, Supplier<T> supplier) {
        return this.entry(name, cb -> new SimpleBuilder<>(this, this, name, cb, CreateRegistryKeys.DISPLAY_TARGET, supplier)
            .byBlock(DisplayTarget.BY_BLOCK).byBlockEntity(DisplayTarget.BY_BLOCK_ENTITY));
    }

    /* client-side hooks: accepted, ignored on this layer */
    public static <T extends Block> NonNullConsumer<? super T> casingConnectivity(BiConsumer<T, ?> consumer) { return e -> {}; }
    public static <T extends Block> NonNullConsumer<? super T> blockModel(Supplier<? extends NonNullFunction<?, ?>> func) { return e -> {}; }
    public static <T extends Item> NonNullConsumer<? super T> itemModel(Supplier<? extends NonNullFunction<?, ?>> func) { return e -> {}; }
    public static NonNullConsumer<? super Block> connectedTextures(Supplier<?> behavior) { return e -> {}; }
}
