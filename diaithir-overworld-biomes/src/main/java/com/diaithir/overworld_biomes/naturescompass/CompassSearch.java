package com.diaithir.overworld_biomes.naturescompass;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class CompassSearch {
    static final TagKey<Biome> BLACKLIST = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(BiomeCompassMod.MOD_ID, "locatable_blacklist"));
    static final TagKey<Biome> UNDERGROUND = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(BiomeCompassMod.MOD_ID, "underground"));
    private static final int CELL = 512;

    private static final Map<UUID, Task> TASKS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> COOLDOWN = new ConcurrentHashMap<>();
    private static final Set<String> NEGATIVE = ConcurrentHashMap.newKeySet();

    static void clearCaches() { NEGATIVE.clear(); TASKS.clear(); COOLDOWN.clear(); }

    static List<String> biomeList(ServerPlayer sp) {
        ServerLevel level = sp.level();
        var reg = level.registryAccess().lookupOrThrow(Registries.BIOME);
        Set<Identifier> valid = new HashSet<>();
        try {
            for (Holder<Biome> h : level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes()) {
                h.unwrapKey().ifPresent(k -> valid.add(k.identifier()));
            }
        } catch (Throwable t) {
            return List.of();
        }
        List<? extends String> bl = CompassConfig.BLACKLIST.get();
        List<String> out = new ArrayList<>();
        reg.listElements().forEach(ref -> {
            Identifier id = ref.key().identifier();
            if (valid.contains(id) && !ref.is(BLACKLIST) && !bl.contains(id.toString())) out.add(id.toString());
        });
        return out;
    }

    static void start(ServerPlayer sp, Identifier biome, boolean mainHand) {
        if (TASKS.containsKey(sp.getUUID())) return;
        long now = sp.level().getGameTime();
        Long last = COOLDOWN.get(sp.getUUID());
        if (last != null && now - last < CompassConfig.COOLDOWN_TICKS.get()) return;
        if (!CompassConfig.ALLOW_OTHER_DIMENSIONS.get() && sp.level().dimension() != net.minecraft.world.level.Level.OVERWORLD) return;
        ItemStack stack = sp.getItemInHand(mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        if (!(stack.getItem() instanceof BiomeCompassItem)) return;
        ServerLevel level = sp.level();
        var holder = level.registryAccess().lookupOrThrow(Registries.BIOME).get(ResourceKey.create(Registries.BIOME, biome));
        if (holder.isEmpty()) return;
        stack.set(BiomeCompassMod.TARGET_BIOME.get(), biome);
        stack.remove(BiomeCompassMod.LOCATED_POS.get());
        stack.set(BiomeCompassMod.SEARCH_STATE.get(), 0);
        COOLDOWN.put(sp.getUUID(), now);
        boolean under = holder.get().is(UNDERGROUND);
        TASKS.put(sp.getUUID(), new Task(sp.getUUID(), level, biome, under, sp.blockPosition(), mainHand));
        PacketDistributor.sendToPlayer(sp, new CompassNetwork.SearchProgress(0));
    }

    static void tick(net.minecraft.server.MinecraftServer server) {
        if (TASKS.isEmpty()) return;
        long budget = (long) (CompassConfig.BUDGET_MS.get() * 1_000_000L);
        for (Iterator<Task> it = TASKS.values().iterator(); it.hasNext(); ) {
            Task t = it.next();
            ServerPlayer sp = server.getPlayerList().getPlayer(t.player);
            if (sp == null || sp.level() != t.level) { it.remove(); continue; }
            long end = System.nanoTime() + budget;
            boolean done;
            try {
                done = t.step(end);
            } catch (Throwable ex) {
                finish(sp, t, 3, null);
                it.remove();
                continue;
            }
            if (done) {
                finish(sp, t, t.result != null ? 1 : (t.failed ? 3 : 2), t.result);
                it.remove();
            } else if (t.level.getGameTime() % 10 == 0) {
                PacketDistributor.sendToPlayer(sp, new CompassNetwork.SearchProgress(t.ring * CompassConfig.STEP_COARSE.get()));
            }
        }
    }

    private static void finish(ServerPlayer sp, Task t, int status, BlockPos pos) {
        ItemStack stack = sp.getItemInHand(t.mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        int dist = 0;
        if (stack.getItem() instanceof BiomeCompassItem) {
            if (pos != null) {
                stack.set(BiomeCompassMod.LOCATED_POS.get(), GlobalPos.of(t.level.dimension(), pos));
                dist = (int) Math.sqrt(sp.blockPosition().distSqr(pos));
                stack.set(BiomeCompassMod.SEARCH_STATE.get(), 1);
                if (CompassConfig.DURABILITY_USE.get() && stack.isDamageableItem()) {
                    stack.hurtAndBreak(1, sp, t.mainHand ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
                }
                CompassEvents.fireLocated(sp, t.biome, pos);
            } else {
                stack.set(BiomeCompassMod.SEARCH_STATE.get(), 2);
            }
        }
        PacketDistributor.sendToPlayer(sp, new CompassNetwork.SearchResult(status, dist));
    }

    private static final class Task {
        final UUID player;
        final ServerLevel level;
        final Identifier biome;
        final boolean underground;
        final BlockPos origin;
        final boolean mainHand;
        final BiomeSource source;
        final Climate.Sampler sampler;
        final int maxRing;
        int ring = 0, idx = 0;
        boolean failed;
        BlockPos result;

        Task(UUID p, ServerLevel l, Identifier b, boolean u, BlockPos o, boolean m) {
            player = p; level = l; biome = b; underground = u; origin = o; mainHand = m;
            BiomeSource s = null; Climate.Sampler cs = null;
            try {
                s = l.getChunkSource().getGenerator().getBiomeSource();
                cs = l.getChunkSource().randomState().sampler();
            } catch (Throwable ignored) { failed = true; }
            source = s; sampler = cs;
            maxRing = CompassConfig.MAX_RADIUS.get() / CompassConfig.STEP_COARSE.get();
        }

        private String cellKey(int x, int z) {
            return level.dimension().identifier() + "|" + biome + "|" + Math.floorDiv(x, CELL) + "|" + Math.floorDiv(z, CELL);
        }

        private boolean matches(int x, int y, int z) {
            Holder<Biome> h = source.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z), sampler);
            return h.unwrapKey().map(k -> k.identifier().equals(biome)).orElse(false);
        }

        private int sampleY() { return underground ? -32 : level.getSeaLevel(); }

        private int[] ringPoint(int r, int i) {
            if (r == 0) return new int[]{0, 0};
            int side = 2 * r, per = side * 4;
            i %= per;
            int s = i / side, o = i % side;
            return switch (s) {
                case 0 -> new int[]{-r + o, -r};
                case 1 -> new int[]{r, -r + o};
                case 2 -> new int[]{r - o, r};
                default -> new int[]{-r, r - o};
            };
        }

        boolean step(long endNanos) {
            if (failed) return true;
            int coarse = CompassConfig.STEP_COARSE.get();
            Set<String> checkedCells = null;
            while (System.nanoTime() < endNanos) {
                if (ring > maxRing) return true;
                int per = ring == 0 ? 1 : ring * 8;
                if (idx >= per) { ring++; idx = 0; continue; }
                int[] p = ringPoint(ring, idx++);
                int x = origin.getX() + p[0] * coarse, z = origin.getZ() + p[1] * coarse;
                String key = cellKey(x, z);
                if (NEGATIVE.contains(key) && ring > 0 && idx % 2 == 0) continue;
                if (matches(x, sampleY(), z)) { result = refine(x, z); return true; }
                if (ring > 0 && idx == per && NEGATIVE.size() < 100_000) NEGATIVE.add(key);
            }
            return false;
        }

        private BlockPos refine(int cx, int cz) {
            int coarse = CompassConfig.STEP_COARSE.get(), fine = CompassConfig.STEP_FINE.get();
            BlockPos best = new BlockPos(cx, sampleY(), cz);
            double bestD = best.distSqr(origin);
            for (int dx = -coarse; dx <= coarse; dx += fine) {
                for (int dz = -coarse; dz <= coarse; dz += fine) {
                    int x = cx + dx, z = cz + dz;
                    BlockPos c = new BlockPos(x, sampleY(), z);
                    double d = c.distSqr(origin);
                    if (d < bestD && matches(x, sampleY(), z)) { best = c; bestD = d; }
                }
            }
            return best;
        }
    }

    private CompassSearch() {}
}
