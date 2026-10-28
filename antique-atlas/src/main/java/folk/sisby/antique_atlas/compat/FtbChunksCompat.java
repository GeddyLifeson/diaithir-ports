package folk.sisby.antique_atlas.compat;

import folk.sisby.antique_atlas.AntiqueAtlas;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Read-only bridge to FTB Chunks (client map data) and FTB Teams (team colours, names, members).
 * Everything is reached by reflection so both stay optional, and an API change only turns the overlay off.
 * Claims come from FTB Chunks' client MapManager, the same data its fullscreen map draws.
 */
public final class FtbChunksCompat {
	public record Claim(UUID team, int argb, Component name) {
	}

	public record Member(UUID id, String name, double x, double z) {
	}

	/** Claims for one dimension, resolved once per frame. */
	public interface View {
		@Nullable Claim at(int chunkX, int chunkZ);
	}

	private static Boolean available;
	private static Method mmGetInstance, mmGetDimension, dimGetOwningTeam;
	private static Method teamGetId, teamGetName, teamGetProperty, teamGetMembers, color4iRgba;
	private static Method apiApi, apiGetClientManager, cmSelfTeam;
	private static Object colorProperty;

	private FtbChunksCompat() {
	}

	public static boolean available() {
		if (available == null) {
			available = false;
			if (ModList.get().isLoaded("ftbchunks") && ModList.get().isLoaded("ftbteams")) {
				try {
					Class<?> mm = Class.forName("dev.ftb.mods.ftbchunks.client.map.MapManager");
					mmGetInstance = mm.getMethod("getInstance");
					mmGetDimension = mm.getMethod("getDimension", ResourceKey.class);
					dimGetOwningTeam = Class.forName("dev.ftb.mods.ftbchunks.client.map.MapDimension").getMethod("getOwningTeam", ChunkPos.class);
					Class<?> team = Class.forName("dev.ftb.mods.ftbteams.api.Team");
					teamGetId = team.getMethod("getId");
					teamGetName = team.getMethod("getName");
					teamGetMembers = team.getMethod("getMembers");
					Class<?> prop = Class.forName("dev.ftb.mods.ftbteams.api.property.TeamProperty");
					teamGetProperty = team.getMethod("getProperty", prop);
					colorProperty = Class.forName("dev.ftb.mods.ftbteams.api.property.TeamProperties").getField("COLOR").get(null);
					color4iRgba = Class.forName("dev.ftb.mods.ftblibrary.icon.Color4I").getMethod("rgba");
					Class<?> api = Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI");
					apiApi = api.getMethod("api");
					apiGetClientManager = Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI$API").getMethod("getClientManager");
					cmSelfTeam = Class.forName("dev.ftb.mods.ftbteams.api.client.ClientTeamManager").getMethod("selfTeam");
					available = true;
				} catch (ReflectiveOperationException | LinkageError e) {
					AntiqueAtlas.LOGGER.warn("[Antique Atlas] FTB Chunks is present but its API changed, claim overlay disabled", e);
				}
			}
		}
		return available;
	}

	/** A claims lookup for a dimension, or null when FTB Chunks has no map data for it. */
	@SuppressWarnings("unchecked")
	public static @Nullable View view(ResourceKey<Level> dimension) {
		if (!available()) return null;
		try {
			Optional<Object> manager = (Optional<Object>) mmGetInstance.invoke(null);
			if (manager.isEmpty()) return null;
			Object dim = mmGetDimension.invoke(manager.get(), dimension);
			if (dim == null) return null;
			Map<UUID, Claim> byTeam = new HashMap<>();
			return (cx, cz) -> {
				try {
					Object team = dimGetOwningTeam.invoke(dim, new ChunkPos(cx, cz));
					if (team == null) return null;
					UUID id = (UUID) teamGetId.invoke(team);
					Claim c = byTeam.get(id);
					if (c == null) {
						Object color = teamGetProperty.invoke(team, colorProperty);
						int rgba = color == null ? 0xFFFFFF : (Integer) color4iRgba.invoke(color);
						c = new Claim(id, 0xFF000000 | rgba, (Component) teamGetName.invoke(team));
						byTeam.put(id, c);
					}
					return c;
				} catch (ReflectiveOperationException | RuntimeException e) {
					return null;
				}
			};
		} catch (ReflectiveOperationException | RuntimeException e) {
			return null;
		}
	}

	public enum Op { CLAIM, UNCLAIM, LOAD, UNLOAD }

	public record Counts(int claimed, int maxClaimed, int loaded, int maxLoaded) {
	}

	private static Method send, xzOf, genData;
	private static java.lang.reflect.Constructor<?> packetCtor;
	private static Class<?> opClass;
	private static Object ftbClientInstance;
	private static boolean mgmtResolved, mgmtOk;

	private static boolean resolveManagement() {
		if (!mgmtResolved) {
			mgmtResolved = true;
			try {
				opClass = Class.forName("dev.ftb.mods.ftbchunks.api.event.ChunkChangeEvent$Operation");
				Class<?> xz = Class.forName("dev.ftb.mods.ftblibrary.math.XZ");
				xzOf = xz.getMethod("of", int.class, int.class);
				packetCtor = Class.forName("dev.ftb.mods.ftbchunks.net.RequestChunkChangePacket").getConstructor(opClass, Set.class, boolean.class, Optional.class);
				send = Class.forName("dev.ftb.mods.ftblibrary.platform.network.Play2ServerNetworking").getMethod("send", net.minecraft.network.protocol.common.custom.CustomPacketPayload.class);
				Class<?> client = Class.forName("dev.ftb.mods.ftbchunks.client.FTBChunksClient");
				ftbClientInstance = client.getField("INSTANCE").get(null);
				genData = client.getMethod("getGeneralChunkData");
				mgmtOk = true;
			} catch (ReflectiveOperationException | LinkageError e) {
				AntiqueAtlas.LOGGER.warn("[Antique Atlas] FTB Chunks claim management unavailable", e);
			}
		}
		return mgmtOk;
	}

	/** Asks the server to claim, unclaim, force-load or unload one chunk. The server applies FTB Chunks' own permission and limit checks. */
	public static boolean requestChange(Op op, int chunkX, int chunkZ) {
		if (!available() || !resolveManagement()) return false;
		try {
			Object operation = opClass.getField(op.name()).get(null);
			Object packet = packetCtor.newInstance(operation, Set.of(xzOf.invoke(null, chunkX, chunkZ)), false, Optional.empty());
			send.invoke(null, packet);
			return true;
		} catch (ReflectiveOperationException | RuntimeException e) {
			return false;
		}
	}

	/** Claimed and force-loaded counts with their limits, as last sent by the server, or null when unknown. */
	public static @Nullable Counts counts() {
		if (!available() || !resolveManagement()) return null;
		try {
			Object d = genData.invoke(ftbClientInstance);
			if (d == null) return null;
			Class<?> c = d.getClass();
			int maxClaim = (Integer) c.getMethod("maxClaimChunks").invoke(d);
			if (maxClaim <= 0) return null;
			return new Counts((Integer) c.getMethod("claimed").invoke(d), maxClaim, (Integer) c.getMethod("loaded").invoke(d), (Integer) c.getMethod("maxForceLoadChunks").invoke(d));
		} catch (ReflectiveOperationException | RuntimeException e) {
			return null;
		}
	}

	/** Other members of the local player's FTB team that the client currently has loaded, with their positions. */
	@SuppressWarnings("unchecked")
	public static List<Member> teamMembers() {
		List<Member> out = new ArrayList<>();
		Minecraft mc = Minecraft.getInstance();
		if (!available() || mc.level == null || mc.player == null) return out;
		try {
			Object cm = apiGetClientManager.invoke(apiApi.invoke(null));
			Object team = cmSelfTeam.invoke(cm);
			if (team == null) return out;
			for (UUID id : (Set<UUID>) teamGetMembers.invoke(team)) {
				if (id.equals(mc.player.getUUID())) continue;
				Player p = mc.level.getPlayerByUUID(id);
				if (p != null) out.add(new Member(id, p.getGameProfile().name(), p.getX(), p.getZ()));
			}
		} catch (ReflectiveOperationException | RuntimeException ignored) {
		}
		return out;
	}
}
