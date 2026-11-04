package net.diaithir.claims.parties;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** All parties. A party has an owner, members and pending invites; members trust each other on claims. */
public final class PartyData extends SavedData {
    public record Party(String name, UUID owner, List<UUID> members, List<UUID> invited) {
        public static final Codec<Party> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("name").forGetter(Party::name),
            UUIDUtil.CODEC.fieldOf("owner").forGetter(Party::owner),
            UUIDUtil.CODEC.listOf().fieldOf("members").forGetter(Party::members),
            UUIDUtil.CODEC.listOf().optionalFieldOf("invited", List.of()).forGetter(Party::invited)).apply(i, Party::new));

        Party copy() {
            return new Party(name, owner, new ArrayList<>(members), new ArrayList<>(invited));
        }
    }

    public static final SavedDataType<PartyData> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath("diaithirclaims", "parties"), PartyData::new,
        RecordCodecBuilder.create(i -> i.group(Party.CODEC.listOf().fieldOf("parties").forGetter(d -> d.list)).apply(i, PartyData::new)), DataFixTypes.LEVEL);

    final List<Party> list = new ArrayList<>();

    public PartyData() {}

    private PartyData(List<Party> l) {
        l.forEach(p -> list.add(p.copy()));
    }

    public static PartyData get(MinecraftServer s) {
        return s.getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(TYPE);
    }

    public Party of(UUID player) {
        return list.stream().filter(p -> p.members().contains(player)).findFirst().orElse(null);
    }

    public Party byName(String n) {
        return list.stream().filter(p -> p.name().equalsIgnoreCase(n)).findFirst().orElse(null);
    }

    public List<Party> all() {
        return List.copyOf(list);
    }

    public Party create(String name, UUID owner) {
        Party p = new Party(name, owner, new ArrayList<>(List.of(owner)), new ArrayList<>());
        list.add(p);
        setDirty();
        return p;
    }

    public void remove(Party p) {
        list.remove(p);
        setDirty();
    }
}
