package com.gregtechceu.gtceu.api.registry.registrate;

import com.gregtechceu.gtceu.compat.client.BlockColor;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;


import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.BlockEntityBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.providers.*;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.nullness.*;

import java.util.function.Supplier;

public class GTBlockBuilder<T extends Block, P> extends BlockBuilder<T, P> {

    // spotless:off
    public static <T extends Block, P> GTBlockBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name,
                                                                   BuilderCallback callback,
                                                                   NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return new GTBlockBuilder<>(owner, parent, name, callback, factory, BlockBehaviour.Properties::of)
                .defaultBlockstate().defaultLoot().defaultLang();
    }

    protected GTBlockBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
                             NonNullFunction<BlockBehaviour.Properties, T> factory,
                             NonNullSupplier<BlockBehaviour.Properties> initialProperties) {
        super(owner, parent, name, callback, factory, initialProperties);
    }

    public GTBlockBuilder<T, P> exBlockstate(NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> cons) { return this; }

    public GTBlockBuilder<T, P> gtBlockstate(NonNullBiConsumer<DataGenContext<Block, ? extends T>, GTBlockstateProvider> cons) { return this; }

    // region default overrides

    @Override
    public GTBlockBuilder<T, P> properties(NonNullUnaryOperator<BlockBehaviour.Properties> func) {
        return (GTBlockBuilder<T, P>) super.properties(func);
    }

    @Override
    public GTBlockBuilder<T, P> initialProperties(NonNullSupplier<? extends Block> block) {
        return (GTBlockBuilder<T, P>) super.initialProperties(block);
    }


    @Override
    public GTBlockBuilder<T, P> simpleItem() {
        return (GTBlockBuilder<T, P>) super.simpleItem();
    }

    @Override
    public <BE extends BlockEntity> GTBlockBuilder<T, P> simpleBlockEntity(BlockEntityBuilder.BlockEntityFactory<BE> factory) {
        return (GTBlockBuilder<T, P>) super.simpleBlockEntity(factory);
    }

    @Override
    public <BE extends BlockEntity> BlockEntityBuilder<BE, BlockBuilder<T, P>> blockEntity(BlockEntityBuilder.BlockEntityFactory<BE> factory) {
        return super.blockEntity(factory);
    }


    @Override
    public GTBlockBuilder<T, P> defaultBlockstate() {
        return (GTBlockBuilder<T, P>) super.defaultBlockstate();
    }/* ported: removed */

    @Override
    public GTBlockBuilder<T, P> defaultLang() {
        return (GTBlockBuilder<T, P>) super.defaultLang();
    }

    @Override
    public GTBlockBuilder<T, P> lang(String name) {
        return (GTBlockBuilder<T, P>) super.lang(name);
    }

    @Override
    public GTBlockBuilder<T, P> defaultLoot() {
        return (GTBlockBuilder<T, P>) super.defaultLoot();
    }

    @Override
    public GTBlockBuilder<T, P> loot(NonNullBiConsumer<RegistrateBlockLootTables, T> cons) {
        return (GTBlockBuilder<T, P>) super.loot(cons);
    }/* ported: removed */

    // why is it final >:(
    // @SafeVarargs
    // public final BlockBuilder<T, P> tag(TagKey<Block>... tags) {
    //     return tag(ProviderType.BLOCK_TAGS, tags);
    // }

    public <D extends RegistrateProvider> GTBlockBuilder<T, P> setDataGeneric(ProviderType<? extends D> type, NonNullBiConsumer<DataGenContext<Block, ? extends Block>, D> cons) {
        getOwner().setDataGenerator(this, type, prov -> cons.accept(DataGenContext.from(this), prov));
        return this;
    }

    
    
    public  <D extends RegistrateProvider> GTBlockBuilder<T, P> setData(ProviderType<? extends D> type, NonNullBiConsumer<DataGenContext<Block, T>, D> cons) {
        return (GTBlockBuilder<T, P>) super.setData(type, cons);
    }

    // spotless:on
    // endregion
}
