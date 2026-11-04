package folk.sisby.surveyor.client;

import com.google.common.collect.Multimap;
import folk.sisby.surveyor.PlayerSummary;
import folk.sisby.surveyor.Surveyor;
import folk.sisby.surveyor.SurveyorExploration;
import folk.sisby.surveyor.SurveyorNetworking;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import folk.sisby.surveyor.packet.S2CGroupAmendedPacket;
import folk.sisby.surveyor.packet.S2CGroupChangedPacket;
import folk.sisby.surveyor.packet.S2CGroupUpdatedPacket;
import folk.sisby.surveyor.packet.S2CPacket;
import folk.sisby.surveyor.packet.S2CStructuresAddedPacket;
import folk.sisby.surveyor.packet.S2CUpdateRegionPacket;
import folk.sisby.surveyor.packet.SyncLandmarksAddedPacket;
import folk.sisby.surveyor.packet.SyncLandmarksRemovedPacket;
import folk.sisby.surveyor.packet.SyncLandmarksRequestedPacket;
import folk.sisby.surveyor.structure.WorldStructures;
import folk.sisby.surveyor.terrain.WorldTerrain;
import folk.sisby.surveyor.util.MapUtil;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.BitSet;
import java.util.UUID;
import java.util.stream.Collectors;

public class SurveyorClientNetworking {
	public static void init() {
		SurveyorNetworking.C2S_SENDER = (r, p) -> {
			ClientPacketListener connection = Minecraft.getInstance().getConnection();
			if (connection == null || !connection.hasChannel(p.type())) return;
			p.toPayloads(r).forEach(ClientPacketDistributor::sendToServer);
		};
	}

	public static void register(RegisterClientPayloadHandlersEvent event) {
		event.register(S2CUpdateRegionPacket.ID, (packet, context) -> handleClient(packet, context, SurveyorClientNetworking::handleTerrainAdded));
		event.register(S2CStructuresAddedPacket.ID, (packet, context) -> handleClient(packet, context, SurveyorClientNetworking::handleStructuresAdded));
		event.register(S2CGroupChangedPacket.ID, (packet, context) -> handleClient(packet, context, SurveyorClientNetworking::handleGroupChanged));
		event.register(S2CGroupAmendedPacket.ID, (packet, context) -> handleClient(packet, context, SurveyorClientNetworking::handleGroupAmended));
		event.register(S2CGroupUpdatedPacket.ID, (packet, context) -> handleClient(packet, context, SurveyorClientNetworking::handleGroupUpdated));
		event.register(SyncLandmarksAddedPacket.ID, (packet, context) -> handleClient(packet, context, SurveyorClientNetworking::handleLandmarksAdded));
		event.register(SyncLandmarksRemovedPacket.ID, (packet, context) -> handleClient(packet, context, SurveyorClientNetworking::handleLandmarksRemoved));
		event.register(SyncLandmarksRequestedPacket.ID, (packet, context) -> handleClient(packet, context, SurveyorClientNetworking::handleLandmarksRequested));
	}

	private static void handleTerrainAdded(ClientPacketListener handler, S2CUpdateRegionPacket packet) {
		WorldSummary summary = SurveyorClient.getSummary(packet.dimension(), handler);
		WorldTerrain terrain = summary == null ? null : summary.terrain();
		if (terrain == null) return;
		BitSet changed = terrain.getRegion(packet.regionPos()).readUpdatePacket(packet);
		(packet.shared() ? SurveyorClient.getSharedExploration() : SurveyorClient.getPersonalExploration()).mergeRegion(packet.dimension(), packet.regionPos(), packet.set(), false);
		if (changed.cardinality() > 1) {
			Surveyor.LOGGER.info("[Surveyor] Received {} chunks in {} from the server.", changed.cardinality(), packet.regionPos());
		}
	}

	private static void handleStructuresAdded(ClientPacketListener handler, S2CStructuresAddedPacket packet) {
		WorldSummary summary = SurveyorClient.getSummary(packet.dimension(), handler);
		WorldStructures structures = summary == null ? null : summary.structures();
		if (structures == null) return;
		Multimap<ResourceKey<Structure>, ChunkPos> starts = structures.readUpdatePacket(packet);
		if (Minecraft.getInstance().player != null && !starts.isEmpty()) {
			SurveyorExploration exploration = (packet.shared() ? SurveyorClient.getSharedExploration() : SurveyorClient.getPersonalExploration());
			starts.forEach((key, pos) -> exploration.addStructure(summary.dimension(), key, pos));
			Surveyor.LOGGER.info("[Surveyor] Received {} structures from the server - {}", starts.size(), starts.keySet().stream().map(r -> r.identifier().toString()).collect(Collectors.joining(", ")));
		}
	}

	private static void handleGroupChanged(ClientPacketListener handler, S2CGroupChangedPacket packet) {
		if (!SurveyorClient.getSharedExploration().groupPlayers().equals(packet.players().keySet())) {
			SurveyorClient.getSharedExploration().groupPlayers().clear();
			SurveyorClient.getSharedExploration().groupPlayers().addAll(packet.players().keySet());
		}
		ClientSummary.of(Minecraft.getInstance().getConnection()).matchSummaries(packet.players());
		SurveyorClient.getSharedExploration().replaceTerrain(packet.chunks(), false);
		SurveyorClient.getSharedExploration().replaceStructures(packet.starts());
		SurveyorClient.getSummaries(handler).values().forEach(summary -> SurveyorClient.getSharedExploration().updateClientForLandmarks(summary));
		SurveyorClient.sendKnownData(handler);
		Surveyor.LOGGER.info("[Surveyor] Received updated share group of {} from the server - {}", packet.players().size(), packet.players().values().stream().map(PlayerSummary::username).collect(Collectors.joining(", ")));
	}

	private static void handleGroupAmended(ClientPacketListener handler, S2CGroupAmendedPacket packet) {
		SurveyorClient.getSharedExploration().groupPlayers().add(packet.player());
		Player player = Minecraft.getInstance().level == null ? null : Minecraft.getInstance().level.getPlayerByUUID(packet.player());
		Surveyor.LOGGER.info("[Surveyor] Received additional share group player {}", player == null ? packet.player() : player.getGameProfile().name());
	}

	private static void handleGroupUpdated(ClientPacketListener handler, S2CGroupUpdatedPacket packet) {
		ClientSummary.of(Minecraft.getInstance().getConnection()).mergeSummaries(packet.players());
	}

	private static void handleLandmarksAdded(ClientPacketListener handler, SyncLandmarksAddedPacket packet) {
		WorldSummary summary = SurveyorClient.getSummary(packet.dimension(), handler);
		WorldLandmarks landmarks = summary == null ? null : summary.landmarks();
		if (landmarks == null) return;
		landmarks.readUpdatePacket(packet, null);
		Multimap<UUID, Identifier> keys = MapUtil.keyMultiMap(packet.landmarks());
		Surveyor.LOGGER.info("[Surveyor] Received {} landmarks from the server - {}", keys.size(), keys.values().stream().map(Identifier::toString).collect(Collectors.joining(", ")));
	}

	private static void handleLandmarksRemoved(ClientPacketListener handler, SyncLandmarksRemovedPacket packet) {
		WorldSummary summary = SurveyorClient.getSummary(packet.dimension(), handler);
		WorldLandmarks landmarks = summary == null ? null : summary.landmarks();
		if (landmarks == null) return;
		landmarks.readUpdatePacket(packet, null);
		Surveyor.LOGGER.info("[Surveyor] Received {} landmark removals from the server - {}", packet.landmarks().size(), packet.landmarks().values().stream().map(Identifier::toString).collect(Collectors.joining(", ")));
	}

	private static void handleLandmarksRequested(ClientPacketListener handler, SyncLandmarksRequestedPacket packet) {
		WorldSummary summary = SurveyorClient.getSummary(packet.dimension(), handler);
		WorldLandmarks landmarks = summary == null ? null : summary.landmarks();
		if (landmarks == null) return;
		landmarks.createUpdatePacket(packet.landmarks()).send(summary.manager());
		Surveyor.LOGGER.info("[Surveyor] Received {} landmark requests from the server - {}", packet.landmarks().size(), packet.landmarks().values().stream().map(Identifier::toString).collect(Collectors.joining(", ")));
	}

	private static <T extends S2CPacket> void handleClient(T packet, IPayloadContext context, ClientPacketHandler<T> handler) {
		ClientLevel world = Minecraft.getInstance().level;
		WorldSummary summary = world == null ? null : WorldSummary.of(world);
		if (summary != null && !summary.isClient()) return;
		Minecraft.getInstance().execute(() -> handler.handle(Minecraft.getInstance().getConnection(), packet));
	}

	public interface ClientPacketHandler<T> {
		void handle(ClientPacketListener handler, T packet);
	}
}
