package folk.sisby.surveyor.client;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import folk.sisby.surveyor.PlayerSummary;
import folk.sisby.surveyor.Surveyor;
import folk.sisby.surveyor.WorldSummary;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.ReportedException;
import net.minecraft.world.level.Level;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientSummary {
	public static final String KEY_SHARED = "shared";
	private final long biomeSeed;
	private final ClientPacketListener handler;
	private final Map<UUID, PlayerSummary> players;
	private final Map<ResourceKey<Level>, WorldSummary> worlds;
	public final SurveyorClient.ClientExploration personal;
	public final SurveyorClient.ClientExploration shared;
	public final File saveFile;

	public ClientSummary(long biomeSeed, ClientPacketListener handler) {
		this.biomeSeed = biomeSeed;
		this.handler = handler;
		this.players = new HashMap<>();
		this.worlds = new ConcurrentHashMap<>();
		this.personal = new SurveyorClient.ClientExploration(new HashSet<>(), HashBasedTable.create(), HashBasedTable.create());
		this.shared = new SurveyorClient.ClientExploration(new HashSet<>(), HashBasedTable.create(), HashBasedTable.create());
		this.saveFile = SurveyorClient.getSavePath(biomeSeed).toPath().resolve(SurveyorClient.getClientUuid().toString() + ".dat").toFile();
	}

	public static ClientSummary of(ClientPacketListener handler) {
		return ((SurveyorNetworkHandler) handler).surveyor$getSummary();
	}

	public void mergeSummaries(Map<UUID, PlayerSummary> summaries) {
		players.putAll(summaries);
	}

	public void matchSummaries(Map<UUID, PlayerSummary> summaries) {
		players.clear();
		players.putAll(summaries);
	}

	public Map<UUID, PlayerSummary> players(Set<UUID> group) {
		Map<UUID, PlayerSummary> outMap = new HashMap<>();
		for (UUID uuid : Sets.union(group, players.keySet())) {
			Player player = handler.getLevel().getPlayerByUUID(uuid);
			if (player != null) {
				outMap.put(uuid, new PlayerSummary.PlayerEntitySummary(player));
			} else {
				outMap.put(uuid, players.get(uuid));
			}
		}
		return outMap;
	}

	public WorldSummary getWorld(ResourceKey<Level> dimension) {
		return worlds.computeIfAbsent(dimension, k -> new WorldSummary(null, dimension, handler.registryAccess(), SurveyorClient.getWorldSavePath(dimension, biomeSeed)));
	}

	public void connect() {
		CompoundTag explorationNbt = new CompoundTag();
		if (saveFile.exists()) {
			try {
				explorationNbt = NbtIo.readCompressed(saveFile.toPath(), NbtAccounter.unlimitedHeap());
			} catch (IOException | ReportedException e) {
				Surveyor.LOGGER.error("[Surveyor] Error loading client exploration file.", e);
			}
		}
		this.personal.read(explorationNbt);
		this.shared.read(explorationNbt.getCompound(KEY_SHARED).orElse(new CompoundTag()));
		SurveyorClient.getSummaries(handler); // load it up!
		SurveyorClient.handleInitialLoad(handler); // event it up!
		SurveyorClient.sendKnownData(handler); // network it up!
	}

	public void disconnect() {
		SurveyorClient.clearLoadingChunks();
		for (WorldSummary summary : worlds.values()) {
			summary.save(null, SurveyorClient.getWorldSavePath(summary.dimension(), biomeSeed), false);
		}
		try {
			CompoundTag nbt = personal.write(new CompoundTag());
			CompoundTag sharedNbt = shared.write(new CompoundTag());
			nbt.put(KEY_SHARED, sharedNbt);
			NbtIo.writeCompressed(nbt, saveFile.toPath());
		} catch (IOException e) {
			Surveyor.LOGGER.error("[Surveyor] Error saving client exploration file.", e);
		}
	}

	public void leaveWorld(ResourceKey<Level> dimension) {
		if (worlds.containsKey(dimension)) worlds.get(dimension).save(null, SurveyorClient.getWorldSavePath(dimension, biomeSeed), false);
	}
}
