package com.ssaml.advancedoredeposits;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.ssaml.advancedoredeposits.block.DepositBlock;
import com.ssaml.advancedoredeposits.block.DepositBlockEntity;
import com.ssaml.advancedoredeposits.registry.ModBlockEntities;
import com.ssaml.advancedoredeposits.registry.ModBlocks;
import com.ssaml.advancedoredeposits.registry.ModCreativeTabs;
import com.ssaml.advancedoredeposits.registry.ModItems;
import com.ssaml.advancedoredeposits.registry.ModWorldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.NeoForge;

@Mod(AdvancedOreDeposits.MOD_ID)
public final class AdvancedOreDeposits {
    public static final String MOD_ID = "diaithirminers";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AdvancedOreDeposits(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModWorldgen.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(DepositBlock::onBreak);
        NeoForge.EVENT_BUS.addListener(MinersSelfTest::register);
        NeoForge.EVENT_BUS.addListener(MinersSelfTest::onStarted);

        modEventBus.addListener((net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent e) -> {
            e.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.Item.BLOCK, ModBlockEntities.MINER.get(),
                    (be, side) -> new net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper(be, side));
            e.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.Energy.BLOCK, ModBlockEntities.MINER.get(),
                    (be, side) -> be.energy);
        });
        modContainer.registerConfig(Type.COMMON, ModConfig.SPEC);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static boolean isCreateLoaded() {
        return ModList.get().isLoaded("create");
    }

    public static DepositBlockEntity createDepositBlockEntity(BlockPos pos, BlockState state) {
        return new DepositBlockEntity(pos, state);
    }
}
