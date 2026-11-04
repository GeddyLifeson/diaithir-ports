package net.diaithir.fauna;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Natural spawning of Panscriptum Library creatures. A planet is a dimension (data/diaithirfauna/library_planets.tsv: planet id, dimension,
 * opens_age; Diaithir is the Overworld). An entry belongs to the planet named by its universe (second part of its id). A creature of tier t
 * appears from Age opens_age + t - 1. Every few seconds each player gets one attempt near them, capped by how many Library creatures are around.
 */
@EventBusSubscriber(modid = FaunaMod.ID)
public final class LibrarySpawner {
    private static final Map<String, int[]> PLANET_OF_DIM = new HashMap<>(); // dimension id -> {opensAge}
    private static final Map<String, String> PLANET_ID_OF_DIM = new HashMap<>();
    private static final Map<String, List<Species>> BY_PLANET = new HashMap<>();
    private static final int INTERVAL = 100, NEAR_CAP = 8;

    static {
        try (var r = new BufferedReader(new InputStreamReader(LibrarySpawner.class.getResourceAsStream("/data/diaithirfauna/library_planets.tsv"), StandardCharsets.UTF_8))) {
            r.readLine();
            String l;
            while ((l = r.readLine()) != null) {
                String[] c = l.split("\t");
                PLANET_OF_DIM.put(c[1], new int[]{Integer.parseInt(c[2])});
                PLANET_ID_OF_DIM.put(c[1], c[0]);
            }
        } catch (Exception e) {
            throw new IllegalStateException("library_planets.tsv", e);
        }
        for (Species s : Species.LIB) BY_PLANET.computeIfAbsent(planetOf(s), k -> new ArrayList<>()).add(s);
    }

    /** Library entries that exist as a mod creature (Alex's Mobs, Friends and Foes): entry id -> entity type id. */
    private static final Map<String, String> BASE = new HashMap<>();

    static {
        var in = LibrarySpawner.class.getResourceAsStream("/data/diaithirfauna/library_mob_bases.tsv");
        if (in != null) try (var r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String l;
            while ((l = r.readLine()) != null) {
                String[] c = l.split("\t");
                if (c.length == 2) BASE.put(c[0], c[1]);
            }
        } catch (Exception e) {
            throw new IllegalStateException("library_mob_bases.tsv", e);
        }
    }

    private LibrarySpawner() {
    }

    /** "AR.alaima.E1.assay.007385" -> "alaima". */
    public static String planetOf(Species s) {
        String[] p = s.id.split("\\.");
        return p.length > 1 ? p[1].toLowerCase(java.util.Locale.ROOT) : "";
    }

    public static List<Species> onPlanet(String planetId) { return BY_PLANET.getOrDefault(planetId, List.of()); }

    /** Age (1+) at which a creature of this planet can appear. */
    public static int opensAt(Species s, int planetOpens) { return planetOpens + s.tier - 1; }

    /**
     * For structure spawners (reason SPAWNER) naming a diaithirfauna:<plan> entity: a hostile creature of that plan from the planet the level is,
     * allowed by the Age; null if the dimension is not a planet or the planet has none of that plan (the mob then falls back to its usual species).
     */
    public static Species pickFor(net.minecraft.world.level.ServerLevelAccessor la, net.minecraft.core.BlockPos pos, String plan, net.minecraft.util.RandomSource rnd) {
        Species s = pickFor(la, pos, plan, rnd, true);
        return s != null ? s : pickFor(la, pos, plan, rnd, false); // a spawner never produces nothing: ignore the habitat if none fits
    }

    private static Species pickFor(net.minecraft.world.level.ServerLevelAccessor la, net.minecraft.core.BlockPos pos, String plan, net.minecraft.util.RandomSource rnd, boolean byHabitat) {
        ServerLevel level = la.getLevel();
        String dim = level.dimension().identifier().toString();
        int[] planet = PLANET_OF_DIM.get(dim);
        if (planet == null) return null;
        int age = Math.max(1, Revival.age(level));
        int hm = Habitat.planetMask(la, pos);
        List<Species> hostile = new ArrayList<>(), any = new ArrayList<>(), young = new ArrayList<>();
        for (Species s : onPlanet(PLANET_ID_OF_DIM.get(dim))) {
            if (!s.plan.equals(plan) || (byHabitat && s.hab != 0 && (s.hab & hm) == 0)) continue;
            if (opensAt(s, planet[0]) <= age) (s.has(Species.F_AGGRESSIVE) ? hostile : any).add(s);
            else young.add(s);
        }
        // Age-allowed hostile first, then any allowed, then (so a spawner never produces nothing) the planet's own lowest tier of that plan
        List<Species> from = !hostile.isEmpty() ? hostile : !any.isEmpty() ? any : young;
        if (from == young) young.sort(java.util.Comparator.comparingInt(x -> x.tier));
        return from.isEmpty() ? null : from == young ? young.get(0) : from.get(rnd.nextInt(from.size()));
    }

    @SubscribeEvent
    static void tick(LevelTickEvent.Post e) {
        if (!(e.getLevel() instanceof ServerLevel level) || level.getGameTime() % INTERVAL != 0) return;
        String dim = level.dimension().identifier().toString();
        int[] planet = PLANET_OF_DIM.get(dim);
        if (planet == null) return;
        List<Species> all = onPlanet(PLANET_ID_OF_DIM.get(dim));
        if (all.isEmpty()) return;
        int age = Math.max(1, Revival.age(level));
        for (ServerPlayer p : level.players()) attempt(level, p, all, planet[0], age);
    }

    static boolean attempt(ServerLevel level, ServerPlayer p, List<Species> all, int opens, int age) {
        return attempt(level, p.getX(), p.getY(), p.getZ(), p.getBoundingBox().inflate(64), all, opens, age);
    }

    private static int libraryNear(ServerLevel level, double x, double y, double z) {
        return level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(FaunaMob.class),
                m -> m.species() != null && m.species().tier > 0 && m.distanceToSqr(x, y, z) < 64 * 64).size();
    }

    static boolean attempt(ServerLevel level, double px, double py, double pz, net.minecraft.world.phys.AABB near, List<Species> all, int opens, int age) {
        var rnd = level.getRandom();
        if (libraryNear(level, px, py, pz) >= NEAR_CAP) return false;
        Species s = all.get(rnd.nextInt(all.size()));
        if (opensAt(s, opens) > age) return false;
        Plan plan = Plan.ALL.get(s.plan);
        double ang = rnd.nextDouble() * Math.PI * 2, dist = 24 + rnd.nextInt(24);
        int x = (int) (px + Math.cos(ang) * dist), z = (int) (pz + Math.sin(ang) * dist);
        if (!level.hasChunkAt(new BlockPos(x, 0, z))) return false;
        BlockPos pos = new BlockPos(x, level.getHeight(plan.water() ? Heightmap.Types.OCEAN_FLOOR : Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
        if (plan.water()) {
            if (!level.getFluidState(pos).is(FluidTags.WATER)) return false;
        } else {
            if (level.getFluidState(pos).is(FluidTags.WATER) || level.getFluidState(pos.below()).is(FluidTags.WATER)) return false;
            if (!level.getBlockState(pos.below()).isValidSpawn(level, pos.below(), FaunaMod.TYPES.get(s.plan).get())) return false;
        }
        if (s.hab != 0 && (s.hab & Habitat.planetMask(level, pos)) == 0) return false;
        return spawn(level, s, pos.getX() + 0.5, pos.getY() + (plan.fly() ? 3 : 0), pos.getZ() + 0.5, EntitySpawnReason.NATURAL) != null;
    }

    /** Op test: n attempts around a point at the given Age; returns "spawned/attempts planet" . */
    static String test(ServerLevel level, net.minecraft.world.phys.Vec3 at, int n, int age) {
        String dim = level.dimension().identifier().toString();
        int[] planet = PLANET_OF_DIM.get(dim);
        if (planet == null) return "not a planet dimension: " + dim;
        List<Species> all = onPlanet(PLANET_ID_OF_DIM.get(dim));
        int ok = 0;
        var box = new net.minecraft.world.phys.AABB(at, at).inflate(64);
        for (int i = 0; i < n; i++) if (attempt(level, at.x, at.y, at.z, box, all, planet[0], age)) ok++;
        return ok + "/" + n + " (" + libraryNear(level, at.x, at.y, at.z) + " library creatures within 64) on " + PLANET_ID_OF_DIM.get(dim) + " (" + all.size() + " creatures, opens Age " + planet[0] + ")";
    }

    /**
     * Spawns the creature for a Library species at a position: the mod creature it is mapped to when that mod is loaded, else a FaunaMob on its
     * body plan. The Age tier scales health either way. Returns the entity or null.
     */
    public static net.minecraft.world.entity.Entity spawn(ServerLevel level, Species s, double x, double y, double z, EntitySpawnReason reason) {
        net.minecraft.world.entity.Entity e = null;
        String base = BASE.get(s.id);
        if (base != null) {
            var type = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getOptional(net.minecraft.resources.Identifier.parse(base));
            if (type.isPresent()) e = type.get().create(level, reason);
        }
        if (e instanceof net.minecraft.world.entity.LivingEntity le && !(e instanceof FaunaMob)) {
            var hp = le.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
            if (hp != null && s.tier > 1) { hp.setBaseValue(hp.getBaseValue() * (1 + 0.4 * (s.tier - 1))); le.setHealth(le.getMaxHealth()); }
        } else {
            FaunaMob m = FaunaMod.TYPES.get(s.plan).get().create(level, reason);
            if (m == null) return null;
            m.setSpecies(s);
            e = m;
        }
        e.setPos(x, y, z);
        level.addFreshEntity(e);
        return e;
    }
}
