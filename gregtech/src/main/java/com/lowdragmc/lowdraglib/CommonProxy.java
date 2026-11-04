package com.lowdragmc.lowdraglib;

import net.minecraft.world.level.block.entity.TestBlockEntity;

import com.lowdragmc.lowdraglib.gui.factory.*;


import com.lowdragmc.lowdraglib.networking.LDLNetworking;


import com.lowdragmc.lowdraglib.syncdata.TypedPayloadRegistries;




import com.lowdragmc.lowdraglib.utils.ReflectionUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CommonProxy {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, LDLib.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, LDLib.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LDLib.MOD_ID);/* ported: removed */

    static {
        if (Platform.isDevEnv()) {/* ported: removed */
        } else {/* ported: removed */
        }
    }

    public CommonProxy(IEventBus eventBus) {
        // used for forge events (ClientProxy + CommonProxy)
        // eventBus.register(this); //TODO: uncomment if @SubscribeEvent is used in this class
        eventBus.addListener(FMLLoadCompleteEvent.class, CommonProxy::loadComplete);
        eventBus.addListener(LDLNetworking::registerPayloads);
        // register server commands
        NeoForge.EVENT_BUS.addListener(this::registerCommand);
        // init common features
        CommonProxy.init();
        TypedPayloadRegistries.postInit();
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITY_TYPES.register(eventBus);
    }

    public static void init() {  /* ported stub */ }

    public static void loadComplete(FMLLoadCompleteEvent e) {
        e.enqueueWork(TypedPayloadRegistries::postInit);
    }

    public void registerCommand(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        ServerCommands.createServerCommands().forEach(dispatcher::register);
    }
}
