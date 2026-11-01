package folk.sisby.antique_atlas.waypoint;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.client.SurveyorClient;
import folk.sisby.surveyor.landmark.Landmark;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import folk.sisby.surveyor.landmark.component.LandmarkComponentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Client commands for player waypoints (Surveyor landmarks owned by the player, drawn on the atlas).
 * /waypoint add|list|remove, and /waypoint import for one-shot JourneyMap migration.
 * Waypoints sync to the player's share group when Surveyor's networking.waypoints is GROUP or SERVER.
 */
public final class WaypointCommands {
	private WaypointCommands() {
	}

	private static Component msg(String key, Object... args) {
		return Component.literal("[Atlas] ").withStyle(ChatFormatting.GOLD).append(Component.translatable(key, args));
	}

	private static void say(CommandContext<CommandSourceStack> c, Component t) {
		c.getSource().sendSuccess(() -> t, false);
	}

	public static void register(CommandDispatcher<CommandSourceStack> d) {
		d.register(Commands.literal("waypoint")
			.executes(c -> list(c))
			.then(Commands.literal("list").executes(c -> list(c)))
			.then(Commands.literal("add")
				.then(Commands.argument("name", StringArgumentType.string())
					.executes(c -> add(c, null, null))
					.then(Commands.argument("color", StringArgumentType.word())
						.suggests((c, b) -> SharedSuggestionProvider.suggest(WaypointColors.DYE_NAMES, b))
						.executes(c -> add(c, StringArgumentType.getString(c, "color"), null))
						.then(Commands.argument("pos", BlockPosArgument.blockPos())
							.executes(c -> add(c, StringArgumentType.getString(c, "color"), c.getArgument("pos", WorldCoordinates.class).getBlockPos(c.getSource())))))))
			.then(Commands.literal("remove")
				.then(Commands.argument("name", StringArgumentType.string())
					.suggests((c, b) -> SharedSuggestionProvider.suggest(mine().stream().map(l -> "\"" + nameOf(l) + "\""), b))
					.executes(c -> remove(c, StringArgumentType.getString(c, "name")))))
			.then(Commands.literal("import")
				.executes(c -> importJourneyMap(c, null))
				.then(Commands.argument("folder", StringArgumentType.greedyString())
					.suggests((c, b) -> SharedSuggestionProvider.suggest(journeyMapFolders().stream().map(p -> p.getParent().getFileName() + "/" + p.getFileName()), b))
					.executes(c -> importJourneyMap(c, StringArgumentType.getString(c, "folder"))))));
	}

	private static String nameOf(Landmark l) {
		return l.getOrDefault(LandmarkComponentTypes.NAME, Component.literal(l.id().getPath())).getString();
	}

	private static List<Landmark> mine() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return List.of();
		WorldSummary summary = WorldSummary.of(mc.level);
		if (summary == null || summary.landmarks() == null) return List.of();
		return new ArrayList<>(summary.landmarks().asMap(SurveyorClient.getClientUuid(), null).values());
	}

	private static int list(CommandContext<CommandSourceStack> c) {
		List<Landmark> mine = mine();
		if (mine.isEmpty()) {
			say(c, msg("command.antique_atlas.waypoint.none"));
			return 0;
		}
		say(c, msg("command.antique_atlas.waypoint.list", mine.size()));
		for (Landmark l : mine) {
			BlockPos p = l.getOrDefault(LandmarkComponentTypes.POS, BlockPos.ZERO);
			int rgb = 0xFFFFFF & l.getOrDefault(LandmarkComponentTypes.COLOR, 0xFFFFFF);
			say(c, Component.literal(" - ").withStyle(ChatFormatting.GRAY).append(Component.literal(nameOf(l)).withStyle(s -> s.withColor(rgb))).append(Component.literal(" " + p.toShortString()).withStyle(ChatFormatting.GOLD)));
		}
		return mine.size();
	}

	private static int add(CommandContext<CommandSourceStack> c, String color, BlockPos pos) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null) return 0;
		WorldSummary summary = WorldSummary.of(mc.level);
		if (summary == null || summary.landmarks() == null) {
			say(c, msg("command.antique_atlas.waypoint.disabled"));
			return 0;
		}
		int argb = 0xFFFFFFFF;
		if (color != null) {
			var parsed = WaypointColors.parse(color);
			if (parsed.isEmpty()) {
				say(c, msg("command.antique_atlas.waypoint.badcolor", color));
				return 0;
			}
			argb = parsed.get();
		}
		BlockPos at = pos != null ? pos : mc.player.blockPosition();
		String name = StringArgumentType.getString(c, "name");
		final int fArgb = argb;
		Identifier id = AntiqueAtlas.id(WaypointColors.idPath("point", argb, at.getX(), at.getZ()));
		summary.landmarks().put(Landmark.create(SurveyorClient.getClientUuid(), id, b -> b
			.add(LandmarkComponentTypes.POS, at)
			.add(LandmarkComponentTypes.NAME, Component.literal(name))
			.add(LandmarkComponentTypes.COLOR, fArgb)));
		say(c, msg("command.antique_atlas.waypoint.added", Component.literal(name).withStyle(s -> s.withColor(0xFFFFFF & fArgb)), at.toShortString()));
		return 1;
	}

	private static int remove(CommandContext<CommandSourceStack> c, String name) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return 0;
		WorldSummary summary = WorldSummary.of(mc.level);
		int n = 0;
		for (Landmark l : mine()) {
			if (nameOf(l).equalsIgnoreCase(name)) {
				summary.landmarks().remove(l.owner(), l.id());
				n++;
			}
		}
		say(c, msg(n == 0 ? "command.antique_atlas.waypoint.notfound" : "command.antique_atlas.waypoint.removed", name));
		return n;
	}

	/** Every journeymap/data/&lt;sp|mp&gt;/&lt;world&gt;/waypoints folder under the game directory. */
	public static List<Path> journeyMapFolders() {
		Path root = FMLPaths.GAMEDIR.get().resolve("journeymap").resolve("data");
		List<Path> out = new ArrayList<>();
		try (Stream<Path> modes = Files.isDirectory(root) ? Files.list(root) : Stream.empty()) {
			for (Path mode : (Iterable<Path>) modes::iterator) {
				if (!Files.isDirectory(mode)) continue;
				try (Stream<Path> worlds = Files.list(mode)) {
					worlds.map(w -> w.resolve("waypoints")).filter(Files::isDirectory).forEach(out::add);
				}
			}
		} catch (IOException ignored) {
		}
		return out;
	}

	private static int importJourneyMap(CommandContext<CommandSourceStack> c, String folder) {
		Path dir;
		List<Path> all = journeyMapFolders();
		if (folder == null) {
			if (all.isEmpty()) {
				say(c, msg("command.antique_atlas.waypoint.import.none"));
				return 0;
			}
			if (all.size() > 1) {
				say(c, msg("command.antique_atlas.waypoint.import.choose"));
				all.forEach(p -> say(c, Component.literal(" - " + p.getParent().getFileName() + "/" + p.getFileName()).withStyle(ChatFormatting.GRAY)));
				return 0;
			}
			dir = all.get(0);
		} else {
			dir = all.stream().filter(p -> (p.getParent().getFileName() + "/" + p.getFileName()).equals(folder) || p.getParent().getFileName().toString().equals(folder)).findFirst().orElse(null);
			if (dir == null) {
				say(c, msg("command.antique_atlas.waypoint.import.none"));
				return 0;
			}
		}
		int imported = 0;
		try (Stream<Path> files = Files.list(dir)) {
			for (Path f : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".json"))::iterator) {
				var wp = JourneyMapWaypoint.parse(Files.readString(f));
				if (wp.isPresent()) imported += importOne(wp.get());
			}
		} catch (IOException e) {
			AntiqueAtlas.LOGGER.error("[Antique Atlas] Could not read JourneyMap waypoints from {}", dir, e);
		}
		say(c, msg("command.antique_atlas.waypoint.import.done", imported));
		return imported;
	}

	private static int importOne(JourneyMapWaypoint wp) {
		int n = 0;
		for (String dim : wp.dimensions()) {
			Identifier dimId = Identifier.tryParse(dim);
			if (dimId == null) continue;
			ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, dimId);
			WorldSummary summary = SurveyorClient.tryGetSummary(key);
			if (summary == null || summary.landmarks() == null) continue;
			int argb = 0xFF000000 | (wp.death() ? 0x808080 : wp.rgb());
			BlockPos pos = new BlockPos(wp.x(), wp.y(), wp.z());
			Identifier id = AntiqueAtlas.id(WaypointColors.idPath(wp.death() ? "skull" : "point", argb, wp.x(), wp.z()));
			summary.landmarks().put(Landmark.create(SurveyorClient.getClientUuid(), id, b -> b
				.add(LandmarkComponentTypes.POS, pos)
				.add(LandmarkComponentTypes.NAME, Component.literal(wp.name()))
				.add(LandmarkComponentTypes.COLOR, argb)));
			n++;
		}
		return n;
	}
}
