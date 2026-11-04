package net.diaithir.linkedstorage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** All shared stores, saved with the overworld so every dimension sees the same data. */
public final class LinkedData extends SavedData {
    public static final int CHEST_SLOTS = 27;

    public record ChestEntry(String key, List<ItemStack> stacks) {
        static final Codec<ChestEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("key").forGetter(ChestEntry::key),
                ItemStack.OPTIONAL_CODEC.listOf().fieldOf("stacks").forGetter(ChestEntry::stacks)).apply(i, ChestEntry::new));
    }

    public record TankEntry(String key, FluidStack fluid) {
        static final Codec<TankEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("key").forGetter(TankEntry::key),
                FluidStack.OPTIONAL_CODEC.fieldOf("fluid").forGetter(TankEntry::fluid)).apply(i, TankEntry::new));
    }

    public record CellEntry(String key, int energy) {
        static final Codec<CellEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("key").forGetter(CellEntry::key),
                Codec.INT.fieldOf("energy").forGetter(CellEntry::energy)).apply(i, CellEntry::new));
    }

    public record Team(UUID owner, List<UUID> members) {
        static final Codec<Team> CODEC = RecordCodecBuilder.create(i -> i.group(
                net.minecraft.core.UUIDUtil.CODEC.fieldOf("owner").forGetter(Team::owner),
                net.minecraft.core.UUIDUtil.CODEC.listOf().fieldOf("members").forGetter(Team::members)).apply(i, Team::new));
    }

    static final Codec<LinkedData> CODEC = RecordCodecBuilder.create(i -> i.group(
            ChestEntry.CODEC.listOf().optionalFieldOf("chests", List.of()).forGetter(d -> d.exportChests()),
            TankEntry.CODEC.listOf().optionalFieldOf("tanks", List.of()).forGetter(d -> d.exportTanks()),
            CellEntry.CODEC.listOf().optionalFieldOf("cells", List.of()).forGetter(d -> d.exportCells()),
            Team.CODEC.listOf().optionalFieldOf("teams", List.of()).forGetter(d -> d.exportTeams())).apply(i, LinkedData::new));

    public static final SavedDataType<LinkedData> TYPE = new SavedDataType<LinkedData>(
            Identifier.fromNamespaceAndPath(DiaithirLinkedStorage.ID, "stores"), LinkedData::new, CODEC, null);

    private final Map<String, NonNullList<ItemStack>> chests = new HashMap<>();
    private final Map<String, FluidStack> tanks = new HashMap<>();
    private final Map<String, Integer> cells = new HashMap<>();
    private final Map<UUID, List<UUID>> teams = new HashMap<>();

    public LinkedData() {}

    private LinkedData(List<ChestEntry> c, List<TankEntry> t, List<CellEntry> e, List<Team> tm) {
        for (ChestEntry x : c) {
            NonNullList<ItemStack> list = NonNullList.withSize(CHEST_SLOTS, ItemStack.EMPTY);
            for (int i = 0; i < x.stacks().size() && i < CHEST_SLOTS; i++) list.set(i, x.stacks().get(i));
            chests.put(x.key(), list);
        }
        for (TankEntry x : t) tanks.put(x.key(), x.fluid());
        for (CellEntry x : e) cells.put(x.key(), x.energy());
        for (Team x : tm) teams.put(x.owner(), new ArrayList<>(x.members()));
    }

    private List<ChestEntry> exportChests() {
        List<ChestEntry> out = new ArrayList<>();
        chests.forEach((k, v) -> out.add(new ChestEntry(k, new ArrayList<>(v))));
        return out;
    }

    private List<TankEntry> exportTanks() {
        List<TankEntry> out = new ArrayList<>();
        tanks.forEach((k, v) -> out.add(new TankEntry(k, v)));
        return out;
    }

    private List<CellEntry> exportCells() {
        List<CellEntry> out = new ArrayList<>();
        cells.forEach((k, v) -> out.add(new CellEntry(k, v)));
        return out;
    }

    private List<Team> exportTeams() {
        List<Team> out = new ArrayList<>();
        teams.forEach((k, v) -> out.add(new Team(k, new ArrayList<>(v))));
        return out;
    }

    public static LinkedData get(ServerLevel any) {
        return any.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public NonNullList<ItemStack> chest(ChannelKey key) {
        return chests.computeIfAbsent(key.id(), k -> NonNullList.withSize(CHEST_SLOTS, ItemStack.EMPTY));
    }

    public FluidStack tank(ChannelKey key) {
        return tanks.getOrDefault(key.id(), FluidStack.EMPTY);
    }

    public void setTank(ChannelKey key, FluidStack fluid) {
        tanks.put(key.id(), fluid);
        setDirty();
    }

    public int cell(ChannelKey key) {
        return cells.getOrDefault(key.id(), 0);
    }

    public void setCell(ChannelKey key, int energy) {
        cells.put(key.id(), energy);
        setDirty();
    }

    public List<UUID> team(UUID owner) {
        return teams.computeIfAbsent(owner, k -> new ArrayList<>());
    }
}
