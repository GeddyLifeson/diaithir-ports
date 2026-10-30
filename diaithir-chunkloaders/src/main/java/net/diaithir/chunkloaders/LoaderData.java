package net.diaithir.chunkloaders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.List;

/** Every placed loader (dimension, position, tier, owner), so the cap and the list command need no chunk loading. */
public final class LoaderData extends SavedData {
    public record Entry(String dim, BlockPos pos, int tier, String owner, String ownerName) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("dim").forGetter(Entry::dim),
                BlockPos.CODEC.fieldOf("pos").forGetter(Entry::pos),
                Codec.INT.fieldOf("tier").forGetter(Entry::tier),
                Codec.STRING.optionalFieldOf("owner", "").forGetter(Entry::owner),
                Codec.STRING.optionalFieldOf("ownerName", "").forGetter(Entry::ownerName)).apply(i, Entry::new));
    }

    static final Codec<LoaderData> CODEC = Entry.CODEC.listOf().optionalFieldOf("loaders", List.of()).xmap(LoaderData::new, d -> d.loaders).codec();
    static final SavedDataType<LoaderData> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath(DiaithirChunkLoaders.ID, "loaders"), LoaderData::new, CODEC);

    final List<Entry> loaders;

    private LoaderData() { this(List.of()); }

    private LoaderData(List<Entry> loaders) { this.loaders = new ArrayList<>(loaders); }

    public static LoaderData get(MinecraftServer server) { return server.overworld().getDataStorage().computeIfAbsent(TYPE); }

    int count(String owner) { return (int) loaders.stream().filter(e -> e.owner.equals(owner)).count(); }

    boolean has(String dim, BlockPos pos) { return loaders.stream().anyMatch(e -> e.dim.equals(dim) && e.pos.equals(pos)); }

    void add(Entry e) {
        loaders.removeIf(o -> o.dim.equals(e.dim) && o.pos.equals(e.pos));
        loaders.add(e);
        setDirty();
    }

    void remove(String dim, BlockPos pos) {
        if (loaders.removeIf(o -> o.dim.equals(dim) && o.pos.equals(pos))) setDirty();
    }
}
