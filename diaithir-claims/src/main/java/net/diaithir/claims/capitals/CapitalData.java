package net.diaithir.claims.capitals;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** All capitals in the world. */
public final class CapitalData extends SavedData {
    public static final SavedDataType<CapitalData> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath("diaithirclaims", "capitals"), CapitalData::new,
        RecordCodecBuilder.create(i -> i.group(Capital.CODEC.listOf().fieldOf("capitals").forGetter(d -> d.list)).apply(i, CapitalData::new)), DataFixTypes.LEVEL);

    private final List<Capital> list = new ArrayList<>();

    public CapitalData() {}

    private CapitalData(List<Capital> l) {
        list.addAll(l);
    }

    public static CapitalData get(MinecraftServer s) {
        return s.getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(TYPE);
    }

    public List<Capital> all() {
        return List.copyOf(list);
    }

    public Capital byName(String n) {
        return list.stream().filter(c -> c.name().equalsIgnoreCase(n)).findFirst().orElse(null);
    }

    public Capital ruledBy(UUID p) {
        return list.stream().filter(c -> c.ruler().isPresent() && c.ruler().get().equals(p)).findFirst().orElse(null);
    }

    public Capital subjectOf(UUID p) {
        return list.stream().filter(c -> c.subjects().contains(p)).findFirst().orElse(null);
    }

    public void put(Capital c) {
        list.removeIf(o -> o.name().equalsIgnoreCase(c.name()));
        list.add(c);
        setDirty();
    }

    public void remove(Capital c) {
        list.remove(c);
        setDirty();
    }
}
