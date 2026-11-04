package com.diaithir.overworld_biomes.planets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.ToIntFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Colony picker. Everyone starts on Earth; from the space Ages a player or party can found a colony on one of the
 * Panscriptum planets. Each planet's profile (resources, hazards, environment, named flora) is read from
 * data/diaithir_planets/planet_colony.json, which planets-data/colony_profiles.py builds from the planet specs.
 * Commands (everyone): /colony list|info &lt;planet&gt;|found &lt;planet&gt;|where|party add &lt;player&gt;|party leave.
 * API for ships and the Codex: {@link #colonyPlanet(MinecraftServer, UUID)}, {@link #profile(String)},
 * {@link #travelTargets(MinecraftServer, UUID)}, and {@link #AGE} (set it from the Ages module).
 */
public final class PlanetColony {
    private static final Logger LOG = LoggerFactory.getLogger("PlanetColony");
    /** First Age in which colonies can be founded (space Ages onward). */
    public static final int MIN_SPACE_AGE = 7;
    /** Supplies a player's current Age number (1 to 9). Left null, colonies are not age-gated. */
    public static volatile ToIntFunction<ServerPlayer> AGE = null;

    private static JsonObject profiles;
    private static final Map<UUID, String> PLANET = new HashMap<>();   // member -> planet id
    private static final Map<UUID, List<UUID>> PARTY = new HashMap<>(); // member -> party (shared list)
    private static boolean loaded;

    private PlanetColony() {}

    public static void init() {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.RegisterCommandsEvent.class, e -> register(e.getDispatcher()));
        if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient()) ColonyClient.init();
    }

    public static synchronized JsonObject profile(String id) {
        if (profiles == null) {
            try (InputStream in = PlanetColony.class.getResourceAsStream("/data/diaithir_planets/planet_colony.json")) {
                profiles = in == null ? new JsonObject() : JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            } catch (Exception ex) {
                LOG.warn("planet_colony.json failed to load: {}", ex.toString());
                profiles = new JsonObject();
            }
        }
        JsonElement e = profiles.get(id);
        return e == null ? null : e.getAsJsonObject();
    }

    /** All colony planet ids, sorted. */
    public static List<String> planetIds() { return ids(); }

    private static List<String> ids() {
        profile("");
        List<String> l = new ArrayList<>(profiles.keySet());
        l.sort(String::compareTo);
        return l;
    }

    private static Path file(MinecraftServer s) { return s.getWorldPath(LevelResource.ROOT).resolve("diaithir_colonies.json"); }

    private static synchronized void load(MinecraftServer s) {
        if (loaded) return;
        loaded = true;
        try {
            Path f = file(s);
            if (!Files.exists(f)) return;
            JsonArray arr = JsonParser.parseString(Files.readString(f)).getAsJsonArray();
            for (JsonElement e : arr) {
                JsonObject o = e.getAsJsonObject();
                List<UUID> party = new ArrayList<>();
                for (JsonElement m : o.getAsJsonArray("members")) party.add(UUID.fromString(m.getAsString()));
                for (UUID u : party) { PLANET.put(u, o.get("planet").getAsString()); PARTY.put(u, party); }
            }
        } catch (Exception ex) {
            LOG.warn("colonies file unreadable: {}", ex.toString());
        }
    }

    private static synchronized void save(MinecraftServer s) {
        try {
            JsonArray arr = new JsonArray();
            java.util.Set<List<UUID>> seen = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
            for (Map.Entry<UUID, List<UUID>> en : PARTY.entrySet()) {
                if (!seen.add(en.getValue())) continue;
                JsonObject o = new JsonObject();
                o.addProperty("planet", PLANET.get(en.getKey()));
                JsonArray m = new JsonArray();
                en.getValue().forEach(u -> m.add(u.toString()));
                o.add("members", m);
                arr.add(o);
            }
            Files.writeString(file(s), new Gson().toJson(arr));
        } catch (Exception ex) {
            LOG.warn("colonies file not saved: {}", ex.toString());
        }
    }

    /** The planet this player's colony is on, or null. */
    public static String colonyPlanet(MinecraftServer s, UUID player) { load(s); return PLANET.get(player); }

    /** Planets a ship may take this player to: their colony plus Earth. Keelwright reads this. */
    public static List<String> travelTargets(MinecraftServer s, UUID player) {
        List<String> l = new ArrayList<>(List.of("diaithir"));
        String p = colonyPlanet(s, player);
        if (p != null) l.add(p);
        return l;
    }

    private static int age(ServerPlayer p) {
        ToIntFunction<ServerPlayer> f = AGE;
        return f != null ? f.applyAsInt(p) : worldAge(p.level().getServer());
    }

    private static java.lang.reflect.Method agesGet;
    private static java.lang.reflect.Field agesWorld, agesAge;
    private static boolean agesBroken;

    /** The server's Age from diaithir_ages (AgesSavedData.get(server).world.age, soft dependency); 99 (no gate) when that mod is absent. */
    static synchronized int worldAge(MinecraftServer server) {
        if (agesBroken) return 99;
        try {
            if (agesGet == null) {
                if (!net.neoforged.fml.ModList.get().isLoaded("diaithir_ages")) { agesBroken = true; return 99; }
                Class<?> saved = Class.forName("net.diaithir.ages.mc.AgesSavedData", true, Thread.currentThread().getContextClassLoader());
                agesGet = saved.getMethod("get", MinecraftServer.class);
                agesWorld = saved.getField("world");
                agesAge = agesWorld.getType().getField("age");
            }
            Object data = agesGet.invoke(null, server);
            return ageNumber((String) agesAge.get(agesWorld.get(data)));
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            LOG.warn("cannot read the Age from diaithir_ages, colonies are not age-gated: {}", e.toString());
            agesBroken = true;
            return 99;
        }
    }

    static int ageNumber(String id) {
        if (id == null) return 0;
        return switch (id.trim().toUpperCase(java.util.Locale.ROOT)) {
            case "I" -> 1; case "II" -> 2; case "III" -> 3; case "IV" -> 4; case "V" -> 5;
            case "VI" -> 6; case "VII" -> 7; case "VIII" -> 8; case "IX" -> 9;
            default -> 0;
        };
    }

    private static String str(JsonObject o, String k) { return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : ""; }

    private static String list(JsonObject o, String k) {
        if (!o.has(k)) return "none";
        List<String> l = new ArrayList<>();
        o.getAsJsonArray(k).forEach(e -> l.add(e.getAsString().replace('_', ' ')));
        return l.isEmpty() ? "none" : String.join(", ", l);
    }

    private static void say(CommandContext<CommandSourceStack> c, String m) { c.getSource().sendSuccess(() -> Component.literal(m), false); }

    private static int list(CommandContext<CommandSourceStack> c) {
        StringBuilder sb = new StringBuilder("Colony planets (" + ids().size() + "), use /colony info <planet>:\n");
        for (String id : ids()) {
            JsonObject p = profile(id);
            sb.append(id).append(" (").append(str(p, "name")).append(", Age ").append(str(p, "opens_age")).append("+) ");
        }
        say(c, sb.toString());
        return 1;
    }

    private static int info(CommandContext<CommandSourceStack> c) {
        String id = StringArgumentType.getString(c, "planet");
        JsonObject p = profile(id);
        if (p == null) { c.getSource().sendFailure(Component.literal("No colony planet " + id)); return 0; }
        JsonObject n = p.has("named") ? p.getAsJsonObject("named") : new JsonObject();
        say(c, str(p, "name") + " (" + str(p, "soil") + " soil, " + str(p, "realm") + " realm)\n"
            + "Environment: " + str(p, "terrain") + " terrain, " + str(p, "sea") + " seas, " + str(p, "weather") + " weather, wood: " + str(p, "wood").replace('_', ' ') + "\n"
            + "Rich in: " + list(p, "rich_in") + "; poor in: " + list(p, "poor_in") + "\n"
            + "Own flora and ore: " + str(n, "tree") + " trees, " + str(n, "plant") + ", " + str(n, "ore") + " ore\n"
            + "Hazards: " + list(p, "hazards") + "\n"
            + "Existing settlements: " + str(p, "settlements") + ". Colonies open from Age " + Math.max(MIN_SPACE_AGE, p.get("opens_age").getAsInt()) + ". " + (p.has("summary") ? str(p, "summary") : str(p, "notes")));
        return 1;
    }

    private static int found(CommandContext<CommandSourceStack> c) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer pl = c.getSource().getPlayerOrException();
        MinecraftServer s = c.getSource().getServer();
        load(s);
        String id = StringArgumentType.getString(c, "planet");
        JsonObject p = profile(id);
        if (p == null) { c.getSource().sendFailure(Component.literal("No colony planet " + id)); return 0; }
        int need = Math.max(MIN_SPACE_AGE, p.get("opens_age").getAsInt());
        if (age(pl) < need) { c.getSource().sendFailure(Component.literal("Colonies on " + str(p, "name") + " open in Age " + need + ".")); return 0; }
        List<UUID> party = PARTY.computeIfAbsent(pl.getUUID(), u -> new ArrayList<>(List.of(u)));
        if (PLANET.containsKey(pl.getUUID()) && !PLANET.get(pl.getUUID()).equals(id)) {
            c.getSource().sendFailure(Component.literal("Your party already has a colony on " + PLANET.get(pl.getUUID()) + "."));
            return 0;
        }
        for (UUID u : party) PLANET.put(u, id);
        save(s);
        for (UUID u : party) {
            ServerPlayer m = s.getPlayerList().getPlayer(u);
            if (m != null && PlanetGate.enter(m, id)) m.sendSystemMessage(Component.literal("Colony founded on " + str(p, "name") + ". Hazards: " + list(p, "hazards") + "."));
        }
        return 1;
    }

    private static int where(CommandContext<CommandSourceStack> c) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer pl = c.getSource().getPlayerOrException();
        String id = colonyPlanet(c.getSource().getServer(), pl.getUUID());
        say(c, id == null ? "No colony yet. Everyone starts on Earth; use /colony list." : "Your colony is on " + id + ". Travel targets: " + travelTargets(c.getSource().getServer(), pl.getUUID()));
        return 1;
    }

    private static int partyAdd(CommandContext<CommandSourceStack> c) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer pl = c.getSource().getPlayerOrException();
        MinecraftServer s = c.getSource().getServer();
        load(s);
        ServerPlayer other = s.getPlayerList().getPlayerByName(StringArgumentType.getString(c, "player"));
        if (other == null) { c.getSource().sendFailure(Component.literal("Player not online.")); return 0; }
        if (PARTY.containsKey(other.getUUID()) && PARTY.get(other.getUUID()).size() > 1) { c.getSource().sendFailure(Component.literal("They are already in a party.")); return 0; }
        List<UUID> party = PARTY.computeIfAbsent(pl.getUUID(), u -> new ArrayList<>(List.of(u)));
        if (!party.contains(other.getUUID())) party.add(other.getUUID());
        PARTY.put(other.getUUID(), party);
        String pid = PLANET.get(pl.getUUID());
        if (pid != null) PLANET.put(other.getUUID(), pid);
        save(s);
        say(c, other.getName().getString() + " joined your party" + (pid != null ? " and colony on " + pid : "") + ".");
        return 1;
    }

    private static int partyLeave(CommandContext<CommandSourceStack> c) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer pl = c.getSource().getPlayerOrException();
        MinecraftServer s = c.getSource().getServer();
        load(s);
        List<UUID> party = PARTY.remove(pl.getUUID());
        if (party != null) party.remove(pl.getUUID());
        PLANET.remove(pl.getUUID());
        save(s);
        say(c, "You left your party and colony.");
        return 1;
    }

    private static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("colony")
            .then(Commands.literal("list").executes(PlanetColony::list))
            .then(Commands.literal("info").then(Commands.argument("planet", StringArgumentType.word()).executes(PlanetColony::info)))
            .then(Commands.literal("found").then(Commands.argument("planet", StringArgumentType.word()).executes(PlanetColony::found)))
            .then(Commands.literal("where").executes(PlanetColony::where))
            .then(Commands.literal("party")
                .then(Commands.literal("add").then(Commands.argument("player", StringArgumentType.word()).executes(PlanetColony::partyAdd)))
                .then(Commands.literal("leave").executes(PlanetColony::partyLeave))));
    }
}
