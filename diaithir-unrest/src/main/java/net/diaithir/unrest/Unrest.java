package net.diaithir.unrest;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/** The API other mods and modules call. Positive amounts add Unrest, negative amounts quiet it. */
public final class Unrest {
    private Unrest() {}

    private static UnrestData data(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(UnrestData.TYPE);
    }

    public static Field field(ServerLevel level) {
        return data(level).field;
    }

    public static void add(ServerLevel level, BlockPos pos, double amount) {
        ChunkPos c = ChunkPos.containing(pos);
        data(level).field.add(c.x(), c.z(), amount);
        data(level).setDirty();
    }

    public static double at(ServerLevel level, BlockPos pos) {
        ChunkPos c = ChunkPos.containing(pos);
        return data(level).field.get(c.x(), c.z());
    }

    static void step(ServerLevel level) {
        UnrestData d = data(level);
        d.field.step(Config.SPREAD.getAsDouble(), Config.DECAY.getAsDouble(), 0.01);
        d.setDirty();
    }
}
