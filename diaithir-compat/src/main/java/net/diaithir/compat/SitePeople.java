package net.diaithir.compat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The people of an Age I site (DiaithirRPG world/earth/age1/PEOPLE_SPEC.md). The city engine calls
 * {@code /sitepeople spawn <site> <type> <x1> <z1> <x2> <z2> <culture>} when it raises a site and {@code /sitepeople remove <site>}
 * when the site is ruined. With MCA Reborn loaded they are MCA villagers dressed in their kit's clothing
 * (assets/diaithircompat/skins/clothing); without it, vanilla villagers of the kit's villager type. Either way they are
 * nitwits with no trades, persistent, tagged {@code diaithir_site} and {@code site_<key>}, and carry their culture's main-hand
 * item and carried items. data/diaithircompat/site_people.json (tools/gen_people.py) holds the cultures, kits and counts.
 */
public final class SitePeople {
    private static final Logger LOG = LogUtils.getLogger();
    public static final String TAG = "diaithir_site";
    private static final String RUINED_FILE = "diaithircompat_ruined_sites.txt";
    private static final Set<String> RUINED = ConcurrentHashMap.newKeySet();
    private static final String GEN_FILE = "diaithircompat_site_generations.txt";
    private static final String GEN_TAG = "sitegen_";
    /** The current generation of each re-raised site; people of an older generation are dropped when their chunk loads. */
    private static final Map<String, Integer> GEN = new ConcurrentHashMap<>();
    private static JsonObject data;
    private static MinecraftServer server;

    private SitePeople() {
    }

    static synchronized JsonObject data() {
        if (data == null) {
            try (InputStream in = SitePeople.class.getResourceAsStream("/data/diaithircompat/site_people.json")) {
                data = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            } catch (IOException | RuntimeException e) {
                LOG.error("site_people.json missing or broken; sites get no people", e);
                data = new JsonObject();
            }
        }
        return data;
    }

    public static void onServerStarting(ServerStartingEvent event) {
        server = event.getServer();
        RUINED.clear();
        GEN.clear();
        try {
            Path gens = genFile();
            if (Files.exists(gens)) for (String line : Files.readAllLines(gens)) {
                int eq = line.indexOf('=');
                if (eq > 0) {
                    try {
                        GEN.put(line.substring(0, eq).strip(), Integer.parseInt(line.substring(eq + 1).strip()));
                    } catch (NumberFormatException ignored) {
                        // skip a damaged line
                    }
                }
            }
        } catch (IOException e) {
            LOG.warn("Could not read {}", GEN_FILE, e);
        }
        try {
            Path file = ruinedFile();
            if (Files.exists(file)) Files.readAllLines(file).stream().map(String::strip).filter(s -> !s.isEmpty()).forEach(RUINED::add);
        } catch (IOException e) {
            LOG.warn("Could not read {}", RUINED_FILE, e);
        }
    }

    private static Path ruinedFile() {
        return server.getWorldPath(LevelResource.DATA).resolve(RUINED_FILE);
    }

    private static Path genFile() {
        return server.getWorldPath(LevelResource.DATA).resolve(GEN_FILE);
    }

    private static void saveGens() {
        try {
            Path file = genFile();
            Files.createDirectories(file.getParent());
            Files.write(file, new TreeMap<>(GEN).entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).toList());
        } catch (IOException e) {
            LOG.warn("Could not write {}", GEN_FILE, e);
        }
    }

    private static void saveRuined() {
        try {
            Path file = ruinedFile();
            Files.createDirectories(file.getParent());
            Files.write(file, new TreeSet<>(RUINED));
        } catch (IOException e) {
            LOG.warn("Could not write {}", RUINED_FILE, e);
        }
    }

    /** A ruined site's people still in unloaded chunks are dropped when their chunk loads. */
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if ((RUINED.isEmpty() && GEN.isEmpty()) || event.getLevel().isClientSide() || !entity.entityTags().contains(TAG)) return;
        int gen = 0;
        for (String tag : entity.entityTags()) {
            if (tag.startsWith(GEN_TAG)) {
                try {
                    gen = Integer.parseInt(tag.substring(GEN_TAG.length()));
                } catch (NumberFormatException ignored) {
                    // untagged generation counts as 0
                }
            }
        }
        for (String tag : entity.entityTags()) {
            if (!tag.startsWith("site_")) continue;
            String site = tag.substring(5);
            // ruined, or left over from before the site was re-raised (its people carry an older generation)
            if (RUINED.contains(site) || gen != GEN.getOrDefault(site, 0)) {
                event.setCanceled(true);
                return;
            }
        }
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sitepeople")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("spawn")
                        .then(Commands.argument("site", StringArgumentType.word())
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .then(Commands.argument("x1", IntegerArgumentType.integer())
                                                .then(Commands.argument("z1", IntegerArgumentType.integer())
                                                        .then(Commands.argument("x2", IntegerArgumentType.integer())
                                                                .then(Commands.argument("z2", IntegerArgumentType.integer())
                                                                        .then(Commands.argument("culture", StringArgumentType.greedyString())
                                                                                .executes(SitePeople::spawnCommand)))))))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("site", StringArgumentType.word())
                                .executes(c -> {
                                    int n = remove(c.getSource().getServer(), StringArgumentType.getString(c, "site"));
                                    c.getSource().sendSuccess(() -> Component.literal("Removed " + n + " site people"), false);
                                    return n;
                                }))));
    }

    private static int spawnCommand(CommandContext<CommandSourceStack> c) {
        String site = StringArgumentType.getString(c, "site");
        String culture = StringArgumentType.getString(c, "culture").strip();
        int x1 = IntegerArgumentType.getInteger(c, "x1"), z1 = IntegerArgumentType.getInteger(c, "z1");
        int x2 = IntegerArgumentType.getInteger(c, "x2"), z2 = IntegerArgumentType.getInteger(c, "z2");
        List<Mob> people = spawn(c.getSource().getLevel(), site, StringArgumentType.getString(c, "type"),
                Math.min(x1, x2), Math.min(z1, z2), Math.max(x1, x2), Math.max(z1, z2), culture);
        if (people.isEmpty()) {
            c.getSource().sendFailure(Component.literal("No people for site " + site + " (" + culture + ")"));
            return 0;
        }
        c.getSource().sendSuccess(() -> Component.literal("Spawned " + people.size() + " people of " + culture + " at " + site), false);
        return people.size();
    }

    /** Spawns a site's people inside the box (block coordinates) and returns them; replaces any already there. */
    public static List<Mob> spawn(ServerLevel level, String site, String type, int x1, int z1, int x2, int z2, String culture) {
        List<Mob> out = new ArrayList<>();
        JsonObject d = data();
        JsonObject cultures = d.getAsJsonObject("cultures"), counts = d.getAsJsonObject("counts"), kits = d.getAsJsonObject("kits");
        if (cultures == null || !cultures.has(culture) || counts == null || !counts.has(type)) return out;
        JsonObject people = cultures.getAsJsonObject(culture);
        JsonObject kit = kits == null ? null : kits.getAsJsonObject(people.get("kit").getAsString());
        if (kit == null) return out;
        if (RUINED.remove(site)) saveRuined();
        int generation = GEN.merge(site, 1, Integer::sum); // people still in unloaded chunks now carry an older generation
        saveGens();
        removeLoaded(level.getServer(), site);

        RandomSource random = RandomSource.create(site.hashCode() * 31L + type.hashCode());
        JsonArray range = counts.getAsJsonArray(type);
        int min = range.get(0).getAsInt(), max = range.get(1).getAsInt();
        int count = min + random.nextInt(max - min + 1);
        boolean mca = BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.parse("mca:male_villager"));
        for (int i = 0; i < count; i++) {
            boolean female = random.nextBoolean();
            EntityType<?> entityType = mca
                    ? BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(female ? "mca:female_villager" : "mca:male_villager"))
                    : EntityType.VILLAGER;
            BlockPos pos = standingSpot(level, random, x1, z1, x2, z2);
            if (!(entityType.create(level, EntitySpawnReason.COMMAND) instanceof Mob mob)) continue;
            mob.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360F, 0F);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.COMMAND, null);
            dress(level, mob, people, kit, female, i == 0 && count >= 3 ? "warrior" : null, random); // a camp of three or more keeps a hunter in paint
            mob.addTag(TAG);
            mob.addTag("site_" + site);
            mob.addTag(GEN_TAG + generation);
            mob.setPersistenceRequired();
            if (level.addFreshEntity(mob)) out.add(mob);
        }
        LOG.info("Site {}: {} people of {} ({})", site, out.size(), culture, mca ? "MCA Reborn" : "vanilla villagers");
        return out;
    }

    /**
     * Dresses one mob in a culture's kit, as a site's people are dressed, in a role's outfit (ruler, warrior or priest; anything
     * else is everyday dress). diaithir-ages calls it by reflection for town rulers and followers. False for a culture with no kit.
     */
    public static boolean dressAs(Mob mob, String culture, boolean female, String role) {
        JsonObject d = data();
        JsonObject cultures = d.getAsJsonObject("cultures"), kits = d.getAsJsonObject("kits");
        if (cultures == null || kits == null || culture == null || !cultures.has(culture) || !(mob.level() instanceof ServerLevel level)) return false;
        JsonObject people = cultures.getAsJsonObject(culture);
        dress(level, mob, people, kits.getAsJsonObject(people.get("kit").getAsString()), female, role, RandomSource.create(mob.getUUID().hashCode()));
        return true;
    }

    private static void dress(ServerLevel level, Mob mob, JsonObject people, JsonObject kit, boolean female, String role, RandomSource random) {
        if (mob instanceof Villager villager) {
            var vtype = ResourceKey.create(Registries.VILLAGER_TYPE, Identifier.parse(people.get("villager_type").getAsString()));
            var data = villager.getVillagerData().withProfession(level.registryAccess(), VillagerProfession.NITWIT);
            if (BuiltInRegistries.VILLAGER_TYPE.containsKey(vtype.identifier())) data = data.withType(level.registryAccess(), vtype);
            villager.setVillagerData(data);
            villager.setOffers(new MerchantOffers());
            for (JsonElement e : people.getAsJsonArray("carried")) {
                JsonObject c = e.getAsJsonObject();
                Item item = item(c.get("item").getAsString());
                if (item != null) villager.getInventory().addItem(new ItemStack(item, c.get("count").getAsInt()));
            }
        }
        Item hand = item(people.get("mainhand").getAsString());
        if (hand != null) mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(hand));
        JsonObject roles = kit.getAsJsonObject("roles");
        JsonArray clothes = kit.getAsJsonObject("clothing").getAsJsonArray(female ? "female" : "male");
        if (roles != null && role != null && roles.has(role)) {
            call(mob, "setClothes", String.class, roles.getAsJsonObject(role).get(female ? "female" : "male").getAsString());
            call(mob, "setClothingLocked", boolean.class, true);
        } else if (clothes != null && !clothes.isEmpty()) {
            String id = clothes.get(random.nextInt(clothes.size())).getAsString();
            call(mob, "setClothes", String.class, id);
            call(mob, "setClothingLocked", boolean.class, true);
        }
    }

    // MCA Reborn's VillagerLike interface; no compile-time dependency, so the pack runs without MCA.
    private static void call(Object target, String name, Class<?> arg, Object value) {
        try {
            Method m = target.getClass().getMethod(name, arg);
            m.invoke(target, value);
        } catch (NoSuchMethodException e) {
            // not an MCA villager
        } catch (ReflectiveOperationException e) {
            LOG.warn("MCA {} failed on {}", name, target, e);
        }
    }

    private static Item item(String id) {
        Identifier key = Identifier.tryParse(id);
        return key != null && BuiltInRegistries.ITEM.containsKey(key) ? BuiltInRegistries.ITEM.getValue(key) : null;
    }

    /** The lowest place a person can stand within a few blocks under the surface: inside a hut rather than on its roof. */
    static BlockPos standingSpot(ServerLevel level, RandomSource random, int x1, int z1, int x2, int z2) {
        BlockPos fallback = null;
        for (int attempt = 0; attempt < 24; attempt++) {
            int x = x1 + random.nextInt(x2 - x1 + 1), z = z1 + random.nextInt(z2 - z1 + 1);
            int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (fallback == null) fallback = new BlockPos(x, top, z);
            for (int y = top - 8; y <= top; y++) {
                BlockPos p = new BlockPos(x, y, z);
                if (standable(level, p)) return p;
            }
        }
        return fallback;
    }

    private static boolean standable(ServerLevel level, BlockPos p) {
        BlockPos below = p.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                && level.getFluidState(below).isEmpty()
                && level.getBlockState(p).getCollisionShape(level, p).isEmpty() && level.getFluidState(p).isEmpty()
                && level.getBlockState(p.above()).getCollisionShape(level, p.above()).isEmpty() && level.getFluidState(p.above()).isEmpty();
    }

    /** Removes a ruined site's people: those loaded now, and the rest when their chunk loads. */
    public static int remove(MinecraftServer server, String site) {
        if (RUINED.add(site)) saveRuined();
        return removeLoaded(server, site);
    }

    private static int removeLoaded(MinecraftServer server, String site) {
        String tag = "site_" + site;
        int n = 0;
        for (ServerLevel level : server.getAllLevels()) {
            List<Entity> found = new ArrayList<>();
            for (Entity e : level.getAllEntities()) if (e.entityTags().contains(tag)) found.add(e);
            for (Entity e : found) {
                e.discard();
                n++;
            }
        }
        return n;
    }
}
