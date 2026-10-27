package folk.sisby.surveyor;

import folk.sisby.surveyor.config.NetworkMode;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import folk.sisby.surveyor.packet.S2CGroupAmendedPacket;
import folk.sisby.surveyor.packet.S2CGroupChangedPacket;
import folk.sisby.surveyor.packet.S2CGroupUpdatedPacket;
import folk.sisby.surveyor.structure.WorldStructures;
import folk.sisby.surveyor.terrain.WorldTerrain;
import folk.sisby.surveyor.util.MapUtil;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class ServerSummary {
	public static final String KEY_GROUPS = "groups";
	public static final UUID HOST = UUID.fromString("00000000-0000-0000-0000-000000000000");
	private final MinecraftServer server;
	private final Map<UUID, PlayerSummary> offlineSummaries;
	private final Map<UUID, Set<UUID>> shareGroups;
	private final Map<ResourceKey<Level>, WorldSummary> worlds;
	private boolean dirty = false;

	public ServerSummary(MinecraftServer server, Map<UUID, PlayerSummary> offlineSummaries, @Nullable Map<UUID, Set<UUID>> shareGroups) {
		this.server = server;
		this.offlineSummaries = offlineSummaries;
		this.shareGroups = shareGroups;
		this.worlds = new HashMap<>();
	}

	public static ServerSummary of(MinecraftServer server) {
		return ((SurveyorServer) server).surveyor$getSummary();
	}

	public static Map<UUID, Set<UUID>> loadShareGroups(MinecraftServer server) {
		File folder = Surveyor.getSavePath(Level.OVERWORLD, server);
		CompoundTag sharingNbt = new CompoundTag();
		File sharingFile = new File(folder, "sharing.dat");
		if (sharingFile.exists()) {
			try {
				sharingNbt = NbtIo.readCompressed(sharingFile.toPath(), NbtAccounter.unlimitedHeap());
			} catch (IOException | ReportedNbtException e) {
				Surveyor.LOGGER.error("[Surveyor] Error loading sharing file.", e);
			}
		}
		Map<UUID, Set<UUID>> shareGroups = new ConcurrentHashMap<>();
		sharingNbt.getList(KEY_GROUPS).orElse(new ListTag()).stream().map(l -> ((ListTag) l).stream().map(s -> UUID.fromString(s.asString().orElseThrow())).collect(Collectors.toCollection(HashSet::new))).forEach(set -> {
			for (UUID uuid : set) {
				shareGroups.put(uuid, set);
			}
		});
		return shareGroups;
	}

	public static ServerSummary load(MinecraftServer server) {
		Map<UUID, Set<UUID>> shareGroups = Surveyor.CONFIG.networking.globalSharing ? null : loadShareGroups(server);

		File playerFolder = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).toFile();

		Map<UUID, PlayerSummary> offlineSummaries = new ConcurrentHashMap<>();

		UUID hostProfile = server.getWorldData().getSinglePlayerUUID();

		for (File file : Optional.ofNullable(playerFolder.listFiles((dir, name) -> name.endsWith(".dat"))).orElse(new File[0])) {
			UUID uuid;
			try {
				uuid = UUID.fromString(file.getName().substring(0, file.getName().length() - ".dat".length()));
				if (uuid.equals(hostProfile)) uuid = ServerSummary.HOST;
			} catch (IllegalArgumentException ex) {
				continue;
			}
			if (shareGroups != null && !shareGroups.containsKey(uuid)) continue;
			try {
				CompoundTag playerNbt = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());
				offlineSummaries.put(uuid, new PlayerSummary.OfflinePlayerSummary(uuid, playerNbt, false));
			} catch (IOException | ReportedNbtException e) {
				Surveyor.LOGGER.error("[Surveyor] Error loading offline player data for {}!", uuid, e);
			}
		}

		if (shareGroups != null) {
			for (UUID uuid : shareGroups.keySet()) {
				if (!offlineSummaries.containsKey(uuid)) {
					Surveyor.LOGGER.warn("[Surveyor] Player data was missing for shared player {}! Removing from groups...", uuid);
					shareGroups.get(uuid).remove(uuid);
					shareGroups.remove(uuid);
				}
			}
		}

		return new ServerSummary(server, offlineSummaries, shareGroups);
	}

	public static void onPlayerJoin(ServerGamePacketListenerImpl handler, MinecraftServer server) {
		ServerPlayer player = handler.getPlayer();
		ServerSummary summary = ServerSummary.of(server);
		if (summary == null) return;
		UUID uuid = Surveyor.getUuid(player);
		boolean known = summary.offlineSummaries.containsKey(uuid);
		if (!known) summary.createPlayer(player);
		if (summary.getGroup(uuid).size() > 1) {
			// initial exploration
			Map<UUID, PlayerSummary> groupSummaries = summary.getGroupSummaries(uuid);
			new S2CGroupChangedPacket(groupSummaries, summary.getSharingExploration(uuid, Surveyor.CONFIG.networking.terrain, false).chunks(), summary.getSharingExploration(uuid, Surveyor.CONFIG.networking.structures, false).starts()).send(player);
			// initial offline group positions
			new S2CGroupUpdatedPacket(groupSummaries).send(player);
		}
		// update global group members
		if (!known && Surveyor.CONFIG.networking.globalSharing) new S2CGroupAmendedPacket(uuid).send(uuid, server, NetworkMode.GROUP, false);
	}

	public static void onTick(MinecraftServer server) {
		if ((server.getTickCount() % Surveyor.CONFIG.networking.positionTicks) != 0) return;
		ServerSummary summary = ServerSummary.of(server);
		if (summary == null) return;
		for (Set<UUID> group : summary.getPositionGroups()) {
			Map<UUID, PlayerSummary> onlinePlayers = new HashMap<>();
			for (UUID uuid : group) {
				var player = server.getPlayerList().getPlayer(uuid);
				if (player != null) onlinePlayers.put(uuid, PlayerSummary.of(player));
			}
			if (onlinePlayers.size() > 1) new S2CGroupUpdatedPacket(onlinePlayers).send(null, server, p -> onlinePlayers.containsKey(Surveyor.getUuid(p)), Surveyor.CONFIG.networking.positions, true);
		}
	}

	public WorldSummary getWorld(ResourceKey<Level> dimension) {
		return worlds.computeIfAbsent(dimension, dim -> new WorldSummary(server, dim, server.registryAccess(), Surveyor.getSavePath(dim, server)));
	}

	public void loadWorlds() {
		for (ResourceKey<Level> dimension : server.levelKeys()) {
			WorldSummary summary = getWorld(dimension);
			WorldTerrain terrain = summary.terrain();
			if (terrain != null) SurveyorEvents.Invoke.terrainUpdated(summary, terrain.bitSet(null));
			WorldStructures structures = summary.structures();
			if (structures != null) SurveyorEvents.Invoke.structuresAdded(summary, structures.keySet(null));
			WorldLandmarks landmarks = summary.landmarks();
			if (landmarks != null) SurveyorEvents.Invoke.landmarksAdded(summary, landmarks.keySet(null));
		}
	}

	public void save(boolean force, boolean suppressLogs) {
		if (!isDirty() && StreamSupport.stream(server.getAllLevels().spliterator(), false).map(WorldSummary::of).filter(Objects::nonNull).noneMatch(WorldSummary::isDirty)) return;
		if (!suppressLogs) Surveyor.LOGGER.info("[Surveyor] Saving server data...");
		for (ServerLevel world : server.getAllLevels()) {
			if (!world.noSave || force) {
				WorldSummary summary = WorldSummary.of(world);
				if (summary != null) summary.save(world, Surveyor.getSavePath(world.dimension(), server), suppressLogs);
			}
		}
		File folder = Surveyor.getSavePath(Level.OVERWORLD, server);
		if (isDirty()) {
			File sharingFile = new File(folder, "sharing.dat");
			try {
				NbtIo.writeCompressed(writeNbt(new CompoundTag()), sharingFile.toPath());
				dirty = false;
			} catch (IOException e) {
				Surveyor.LOGGER.error("[Surveyor] Error writing sharing file.", e);
			}
		}
		if (!suppressLogs) Surveyor.LOGGER.info("[Surveyor] Finished saving server data.");
	}

	private CompoundTag writeNbt(CompoundTag nbt) {
		if (shareGroups != null) nbt.put(KEY_GROUPS, new ListTag(shareGroups.values().stream().filter(s -> s.size() > 1).map(s -> (Tag) new ListTag(s.stream().map(u -> (Tag) StringTag.valueOf(u.toString())).toList())).toList()));
		return nbt;
	}

	public PlayerSummary getPlayer(UUID uuid) {
		ServerPlayer player = Surveyor.getPlayer(server, uuid);
		if (player != null) {
			return PlayerSummary.of(player);
		} else {
			return offlineSummaries.get(uuid);
		}
	}

	public SurveyorExploration getExploration(UUID player) {
		PlayerSummary summary = getPlayer(player);
		return summary == null ? null : summary.exploration();
	}

	public void createPlayer(ServerPlayer player) {
		offlineSummaries.put(Surveyor.getUuid(player), new PlayerSummary.OfflinePlayerSummary(player));
	}

	public void updatePlayer(UUID uuid, ValueOutput view, boolean online) {
		PlayerSummary newSummary = offlineSummaries.get(uuid);
		if (newSummary == null) return;
		offlineSummaries.put(uuid, newSummary);
		S2CGroupUpdatedPacket.of(uuid, newSummary).send(null, server, getSharingPlayers(uuid, Surveyor.CONFIG.networking.positions, false)::contains, Surveyor.CONFIG.networking.positions, false);
	}

	public Set<Set<UUID>> getPositionGroups() {
		return Surveyor.CONFIG.networking.positions.atLeast(NetworkMode.SERVER) ? Set.of(offlineSummaries.keySet()) : Surveyor.CONFIG.networking.positions.atMost(NetworkMode.SOLO) ? Set.of() : getGroups();
	}

	public Set<Set<UUID>> getGroups() {
		return shareGroups == null ? new HashSet<>(Set.of(new HashSet<>(offlineSummaries.keySet()))) : new HashSet<>(shareGroups.values());
	}

	public Set<UUID> getGroup(UUID player) {
		return shareGroups == null ? new HashSet<>(offlineSummaries.keySet()) : shareGroups.computeIfAbsent(player, p -> new HashSet<>(Set.of(p)));
	}

	public Map<UUID, PlayerSummary> getAllSummaries() {
		Map<UUID, PlayerSummary> map = new HashMap<>();
		for (UUID u : offlineSummaries.keySet()) {
			if (getPlayer(u) != null) map.put(u, getPlayer(u));
		}
		return map;
	}

	public Map<UUID, PlayerSummary> getGroupSummaries(UUID player) {
		Map<UUID, PlayerSummary> map = new HashMap<>();
		for (UUID u : getGroup(player)) {
			if (getPlayer(u) != null) map.put(u, getPlayer(u));
		}
		return map;
	}

	public void joinGroup(UUID player1, UUID player2) {
		if (shareGroups == null) return;
		if (getGroup(player1).size() > 1 && getGroup(player2).size() > 1) throw new IllegalStateException("Can't merge two groups!");
		if (getGroup(player1).size() > 1) {
			getGroup(player1).add(player2);
			shareGroups.put(player2, getGroup(player1));
		} else {
			getGroup(player2).add(player1);
			shareGroups.put(player1, getGroup(player2));
		}
		for (ServerPlayer friend : getSharingPlayers(player1, NetworkMode.GROUP, true)) {
			UUID uuid = Surveyor.getUuid(friend);
			new S2CGroupChangedPacket(getGroupSummaries(uuid), getSharingExploration(uuid, Surveyor.CONFIG.networking.terrain, false).chunks(), getSharingExploration(uuid, Surveyor.CONFIG.networking.structures, false).starts()).send(friend);
		}
		dirty();
	}

	public void leaveGroup(UUID player) {
		if (shareGroups == null) return;
		Set<ServerPlayer> groupPlayers = getSharingPlayers(player, NetworkMode.GROUP, true);
		getGroup(player).remove(player); // Shares set instance with group members.
		shareGroups.put(player, new HashSet<>());
		getGroup(player).add(player);
		for (ServerPlayer friend : groupPlayers) {
			UUID uuid = Surveyor.getUuid(friend);
			new S2CGroupChangedPacket(getGroupSummaries(uuid), getSharingExploration(uuid, Surveyor.CONFIG.networking.terrain, false).chunks(), getSharingExploration(uuid, Surveyor.CONFIG.networking.structures, false).starts()).send(friend);
		}
		dirty();
	}

	public int groupSize(UUID player) {
		return getGroup(player).size();
	}

	public Set<PlayerSummary> groupPlayers(UUID player) {
		return getGroup(player).stream().map(this::getPlayer).collect(Collectors.toSet());
	}

	public Set<UUID> getSharing(UUID player, NetworkMode mode, boolean withSelf) {
		return mode.atMost(NetworkMode.SOLO) && !withSelf ? Set.of() : mode.atMost(NetworkMode.SOLO) ? Set.of(player) : mode.atMost(NetworkMode.GROUP) ? getGroup(player) : offlineSummaries.keySet();
	}

	public SurveyorExploration getSharingExploration(UUID player, NetworkMode mode, boolean withSelf) {
		Set<UUID> sharing = getSharing(player, mode, withSelf);
		if (mode.atLeast(NetworkMode.SERVER)) return new PlayerSummary.OfflinePlayerSummary.OfflinePlayerExploration(sharing,
			MapUtil.asTable(worlds.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> Optional.ofNullable(e.getValue().terrain()).map(t -> t.bitSet(null)).orElse(Map.of())))),
			MapUtil.asTable(worlds.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> Optional.ofNullable(e.getValue().structures()).map(t -> MapUtil.asListMap(t.keySet(null)).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e2 -> LongSet.of(e2.getValue().stream().mapToLong(ChunkPos::pack).toArray())))).orElse(Map.of())))),
			false
		);
		return PlayerSummary.OfflinePlayerSummary.OfflinePlayerExploration.ofMerged(sharing.stream().map(this::getExploration).filter(Objects::nonNull).collect(Collectors.toSet()));
	}

	public Set<ServerPlayer> getSharingPlayers(UUID player, NetworkMode mode, boolean withSelf) {
		Set<UUID> sharing = getSharing(player, mode, withSelf);
		return server.getPlayerList().getPlayers().stream().filter(p -> sharing.contains(Surveyor.getUuid(p))).collect(Collectors.toSet());
	}

	public boolean isDirty() {
		return dirty && shareGroups != null;
	}

	private void dirty() {
		dirty = true;
	}

	public MinecraftServer getServer() {
		return server;
	}
}
