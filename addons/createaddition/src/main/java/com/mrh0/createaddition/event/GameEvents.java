package com.mrh0.createaddition.event;

import com.mrh0.createaddition.energy.network.WireGraph;
import com.mrh0.createaddition.index.CABlocks;
import com.mrh0.createaddition.index.CAItems;
import com.mrh0.createaddition.network.ObservePacketPayload;
import com.zurrtum.create.AllBlocks;

import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = "diaithir_createaddition")
public class GameEvents {
	@SubscribeEvent
	public static void levelTickEvent(LevelTickEvent.Pre evt) {
		if(evt.getLevel().isClientSide()) return;
		// if (evt == Phase.END) return;
		WireGraph.tick(evt.getLevel());
	}

	@SubscribeEvent
	public static void serverTickEvent(ServerTickEvent.Pre evt) {
		//if (evt.phase == Phase.END) return;
		// Using ServerTick instead of WorldTick because some contraptions can switch worlds.
	}

	@SubscribeEvent
	public static void chunkLoadEvent(ChunkEvent.Load evt) {
		if (evt.getLevel() instanceof ServerLevel level && evt.getChunk() instanceof LevelChunk chunk)
			WireGraph.chunkLoaded(level, chunk);
	}

	@SubscribeEvent
	public static void LevelUnload(LevelEvent.Unload event) {
		if (event.getLevel() instanceof ServerLevel level) WireGraph.levelUnloaded(level);
	}

}
