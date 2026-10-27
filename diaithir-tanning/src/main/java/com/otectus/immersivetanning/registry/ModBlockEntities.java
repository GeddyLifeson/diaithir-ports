package com.otectus.immersivetanning.registry;

import com.otectus.immersivetanning.ImmersiveTanning;
import com.otectus.immersivetanning.blockentity.LeatherworkersBenchBlockEntity;
import com.otectus.immersivetanning.blockentity.TannersRackBlockEntity;
import com.otectus.immersivetanning.blockentity.TannersVatBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ImmersiveTanning.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TannersRackBlockEntity>> TANNERS_RACK = BLOCK_ENTITIES.register("tanners_rack",
            () -> new BlockEntityType<>(TannersRackBlockEntity::new, ModBlocks.TANNERS_RACK.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TannersVatBlockEntity>> TANNERS_VAT = BLOCK_ENTITIES.register("tanners_vat",
            () -> new BlockEntityType<>(TannersVatBlockEntity::new, ModBlocks.TANNERS_VAT.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LeatherworkersBenchBlockEntity>> LEATHERWORKERS_BENCH = BLOCK_ENTITIES.register("leatherworkers_bench",
            () -> new BlockEntityType<>(LeatherworkersBenchBlockEntity::new, ModBlocks.LEATHERWORKERS_BENCH.get()));

    private ModBlockEntities() {}
}
