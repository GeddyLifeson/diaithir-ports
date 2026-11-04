package com.ssaml.advancedoredeposits.registry;

import java.util.function.Supplier;

import com.ssaml.advancedoredeposits.AdvancedOreDeposits;
import com.ssaml.advancedoredeposits.block.DepositBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AdvancedOreDeposits.MOD_ID);

    public static final Supplier<BlockEntityType<DepositBlockEntity>> DEPOSIT = BLOCK_ENTITY_TYPES.register("deposit",
            () -> new BlockEntityType<>(AdvancedOreDeposits::createDepositBlockEntity,
                    ModBlocks.depositBlockArray()));

    public static final Supplier<BlockEntityType<com.ssaml.advancedoredeposits.miner.MinerBlockEntity>> MINER =
            BLOCK_ENTITY_TYPES.register("miner",
                    () -> new BlockEntityType<>(com.ssaml.advancedoredeposits.miner.MinerBlockEntity::new,
                            ModBlocks.MINERS.get(com.ssaml.advancedoredeposits.miner.MinerKind.BURNER).get(),
                            ModBlocks.MINERS.get(com.ssaml.advancedoredeposits.miner.MinerKind.ELECTRIC).get()));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
