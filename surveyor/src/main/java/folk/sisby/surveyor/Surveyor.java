package folk.sisby.surveyor;

import folk.sisby.surveyor.config.NetworkMode;
import folk.sisby.surveyor.config.SurveyorConfig;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import folk.sisby.surveyor.landmark.component.LandmarkComponentTypes;
import folk.sisby.surveyor.mixin.AccessServerPlayerEntity;
import folk.sisby.surveyor.structure.StructureStartSummary;
import folk.sisby.surveyor.structure.WorldStructures;
import folk.sisby.surveyor.terrain.WorldTerrain;
import folk.sisby.surveyor.util.RaycastUtil;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.UUID;

@Mod(Surveyor.ID)
public class Surveyor {
	public static final String ID = "surveyor";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);
	public static final String DATA_SUBFOLDER = "data";
	public static final SurveyorConfig CONFIG = SurveyorConfig.createToml(FMLPaths.CONFIGDIR.get(), "", "surveyor", SurveyorConfig.class);

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(ID, path);
	}

	public static File getSavePath(ResourceKey<Level> dimension, MinecraftServer server) {
		return DimensionType.getStorageFolder(dimension, server.getWorldPath(LevelResource.ROOT)).resolve(DATA_SUBFOLDER).resolve(Surveyor.ID).toFile();
	}

	public static void checkStructureExploration(ServerLevel world, ServerPlayer player, BlockPos pos) {
		if (!world.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) return;
		WorldStructures structures = WorldStructures.of(world);
		if (structures == null) return;
		Registry<Structure> structureRegistry = world.registryAccess().lookupOrThrow(Registries.STRUCTURE);
		SurveyorExploration exploration = SurveyorExploration.of(player);
		Map<Structure, LongSet> structureReferences = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();
		if (!structureReferences.isEmpty()) {
			for (Structure structure : structureReferences.keySet()) {
				ResourceKey<Structure> structureKey = structureRegistry.getResourceKey(structure).orElseThrow();
				for (Long longPos : structureReferences.get(structure)) {
					ChunkPos startPos = ChunkPos.unpack(longPos);
					if (exploration.exploredStructure(world.dimension(), structureKey, startPos)) continue;
					StructureStartSummary start = structures.get(structureKey, startPos);
					if (start == null) continue;
					if (start.getBoundingBox().inflatedBy(2).isInside(pos)) {
						for (StructurePiece piece : start.getChildren()) {
							if (piece.getBoundingBox().inflatedBy(2).isInside(pos)) {
								exploration.addStructure(world.dimension(), structureKey, startPos);
								if (CONFIG.discoveryMessages) {
									player.sendSystemMessage(Component.literal("Discovered ").append(Component.literal(WordUtils.capitalize(structureKey.identifier().getPath().replace("_", " "))).withStyle(ChatFormatting.GREEN)).append(Component.literal(" at ")).append(Component.literal("[%s,%s]".formatted(startPos.x() << 4, startPos.z() << 4)).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY), true);
								}
								break;
							}
						}
					}
				}
			}
		}
	}

	public static boolean canModify(UUID landmarkOwner, @Nullable ServerPlayer player) {
		return player == null || player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) || landmarkOwner.equals(Surveyor.getUuid(player)) || !landmarkOwner.equals(WorldLandmarks.GLOBAL) && (Surveyor.CONFIG.networking.waypoints.atLeast(NetworkMode.SERVER) || (Surveyor.CONFIG.networking.waypoints.atLeast(NetworkMode.GROUP) && ServerSummary.of(((AccessServerPlayerEntity) player).getServer()).getGroup(Surveyor.getUuid(player)).contains(landmarkOwner)));
	}

	public static UUID getUuid(ServerPlayer player) {
		return ((AccessServerPlayerEntity) player).getServer() != null && ((AccessServerPlayerEntity) player).getServer().isSingleplayerOwner(player.nameAndId()) ? ServerSummary.HOST : player.getUUID();
	}

	public static ServerPlayer getPlayer(MinecraftServer server, UUID uuid) {
		return server.getPlayerList().getPlayer(uuid == ServerSummary.HOST && server.getSingleplayerProfile() != null ? server.getSingleplayerProfile().id() : uuid);
	}

	static SurveyorExploration explorationForMode(NetworkMode mode, ServerPlayer player) {
		return mode.atLeast(NetworkMode.SERVER) ? null : mode.atLeast(NetworkMode.GROUP) ? SurveyorExploration.ofShared(player) : mode.atLeast(NetworkMode.SOLO) ? SurveyorExploration.of(player) : PlayerSummary.OfflinePlayerSummary.OfflinePlayerExploration.empty(player.getUUID());
	}

	public Surveyor(IEventBus modBus) {
		modBus.addListener(SurveyorNetworking::register);
		LandmarkComponentTypes.touch();
		NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> SurveyorCommands.registerCommands(e.getDispatcher(), e.getBuildContext(), e.getCommandSelection()));
		NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent e) -> {
			if (e.getEntity() instanceof ServerPlayer sp && sp.level().getServer() != null) ServerSummary.onPlayerJoin(sp.connection, sp.level().getServer());
		});
		NeoForge.EVENT_BUS.addListener((ChunkEvent.Load e) -> {
			if (e.getLevel() instanceof ServerLevel world && e.getChunk() instanceof LevelChunk chunk) {
				WorldTerrain.onChunkLoad(world, chunk, e.isNewChunk());
				WorldStructures.onChunkLoad(world, chunk, e.isNewChunk());
			}
		});
		NeoForge.EVENT_BUS.addListener((ChunkEvent.Unload e) -> {
			if (e.getLevel() instanceof ServerLevel world && e.getChunk() instanceof LevelChunk chunk) WorldTerrain.onChunkUnload(world, chunk);
		});
		NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post e) -> ServerSummary.onTick(e.getServer()));
		NeoForge.EVENT_BUS.addListener((LevelTickEvent.Post e) -> {
			if (!(e.getLevel() instanceof ServerLevel world)) return;
			WorldTerrain.onTick(world);
			if ((world.getGameTime() & 7) != 0) return;
			for (ServerPlayer player : world.players()) {
				checkStructureExploration(world, player, player.blockPosition());
				checkStructureExploration(world, player, BlockPos.containing(RaycastUtil.playerViewRaycast(player, PlayerSummary.of(player).viewDistance()).getLocation()));
			}
		});
		LOGGER.info("[Surveyor] is not a map mod");
	}
}
