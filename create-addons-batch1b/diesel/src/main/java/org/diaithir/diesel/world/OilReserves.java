package org.diaithir.diesel.world;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.diaithir.diesel.CreateDieselGenerators;

import java.util.HashMap;
import java.util.Map;

/** Crude oil under the ground: roughly one chunk in three holds a pool, sized from the chunk's coordinates; pumping is remembered per chunk. */
public class OilReserves extends SavedData {
    public static final Codec<OilReserves> CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT).xmap(OilReserves::new, r -> r.drawn);
    public static final SavedDataType<OilReserves> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath(CreateDieselGenerators.MODID, "oil_reserves"), OilReserves::new, CODEC);

    private final Map<String, Integer> drawn;

    public OilReserves() {
        drawn = new HashMap<>();
    }

    private OilReserves(Map<String, Integer> m) {
        drawn = new HashMap<>(m);
    }

    public static OilReserves get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    private static long hash(ChunkPos c) {
        long h = c.x() * 734287L ^ c.z() * 912931L ^ 0x5eedL;
        h ^= h >>> 17;
        h *= 0x9E3779B97F4A7C15L;
        h ^= h >>> 29;
        return h & Long.MAX_VALUE;
    }

    /** Total oil in the chunk, in mB, before any pumping. */
    public static int total(ChunkPos c) {
        long h = hash(c);
        return h % 3 == 0 ? 50_000 + (int) ((h >> 3) % 450_000) : 0;
    }

    public int remaining(ChunkPos c) {
        return Math.max(0, total(c) - drawn.getOrDefault(c.x() + "," + c.z(), 0));
    }

    /** Takes up to mb from the chunk and returns what it gave. */
    public int draw(ChunkPos c, int mb) {
        int take = Math.min(mb, remaining(c));
        if (take > 0) {
            drawn.merge(c.x() + "," + c.z(), take, Integer::sum);
            setDirty();
        }
        return take;
    }
}
