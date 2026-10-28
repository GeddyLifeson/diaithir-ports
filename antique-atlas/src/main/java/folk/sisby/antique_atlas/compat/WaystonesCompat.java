package folk.sisby.antique_atlas.compat;

import folk.sisby.antique_atlas.AntiqueAtlasMod;
import folk.sisby.surveyor.Surveyor;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.landmark.Landmark;
import folk.sisby.surveyor.landmark.component.LandmarkComponentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.UUID;

/**
 * Turns every Waystone a player has activated into a Surveyor landmark owned by that player, so it shows on the atlas
 * with its name and syncs to the share group like any other waypoint. Uses Waystones' public API by reflection (optional dependency).
 */
public final class WaystonesCompat {
	private static final int WAYSTONE_BLUE = 0xFF3AB3DA;
	private static Method activated, getName, getPos, getDimension, isValid;

	private WaystonesCompat() {
	}

	private static boolean resolved, ok;

	public static void init() {
		if (!ModList.get().isLoaded("waystones")) return;
		NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post e) -> {
			if (e.getServer().getTickCount() % 100 != 7 || !resolve()) return;
			for (ServerPlayer p : e.getServer().getPlayerList().getPlayers()) sync(p);
		});
	}

	private static boolean resolve() {
		if (!resolved) {
			resolved = true;
			try {
				Class<?> api = Class.forName("net.blay09.mods.waystones.api.WaystonesAPI");
				Class<?> ws = Class.forName("net.blay09.mods.waystones.api.Waystone");
				activated = api.getMethod("getActivatedWaystones", net.minecraft.world.entity.player.Player.class);
				getName = ws.getMethod("getName");
				getPos = ws.getMethod("getPos");
				getDimension = ws.getMethod("getDimension");
				isValid = ws.getMethod("isValid");
				ok = true;
			} catch (ReflectiveOperationException | LinkageError e) {
				AntiqueAtlasMod.LOGGER.warn("[Antique Atlas] Waystones is present but its API changed, waystone landmarks disabled", e);
			}
		}
		return ok;
	}

	@SuppressWarnings("unchecked")
	private static void sync(ServerPlayer player) {
		try {
			Collection<Object> all = (Collection<Object>) activated.invoke(null, player);
			UUID owner = Surveyor.getUuid(player);
			for (Object w : all) {
				if (!(Boolean) isValid.invoke(w)) continue;
				@SuppressWarnings("unchecked") ResourceKey<Level> dim = (ResourceKey<Level>) getDimension.invoke(w);
				ServerLevel level = player.level().getServer().getLevel(dim);
				if (level == null) continue;
				WorldSummary summary = WorldSummary.of(level);
				if (summary == null || summary.landmarks() == null) continue;
				BlockPos pos = (BlockPos) getPos.invoke(w);
				Identifier id = AntiqueAtlasMod.id(landmarkPath(pos));
				if (summary.landmarks().contains(owner, id)) continue;
				Component name = (Component) getName.invoke(w);
				summary.landmarks().put(Landmark.create(owner, id, b -> b
					.add(LandmarkComponentTypes.POS, pos)
					.add(LandmarkComponentTypes.NAME, name.getString().isBlank() ? Component.translatable("gui.antique_atlas.waystone") : name)
					.add(LandmarkComponentTypes.COLOR, WAYSTONE_BLUE)));
			}
		} catch (ReflectiveOperationException | RuntimeException ignored) {
		}
	}

	/** Landmark id path; starts with "waystone" so the atlas picks the waystone marker texture. */
	public static String landmarkPath(BlockPos pos) {
		return "waystone/%d/%d/%d".formatted(pos.getX(), pos.getY(), pos.getZ());
	}
}
