package net.diaithir.claims;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

/**
 * Teams: a named group whose members may use each other's claims. A player is in at most one team.
 * Held in memory and saved as one NBT file next to the world (teams.dat); no dependency on other mods.
 */
public final class Teams {
    private static final Map<String, Set<UUID>> MEMBERS = new HashMap<>();
    private static final Map<UUID, String> OF = new HashMap<>();
    private static final Map<UUID, String> INVITES = new HashMap<>();
    private static java.nio.file.Path file;

    private Teams() {}

    static void load(java.nio.file.Path world) {
        MEMBERS.clear();
        OF.clear();
        INVITES.clear();
        file = world.resolve("diaithir_teams.dat");
        try {
            if (!java.nio.file.Files.exists(file)) return;
            CompoundTag root = net.minecraft.nbt.NbtIo.readCompressed(file, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
            CompoundTag teams = root.getCompoundOrEmpty("teams");
            for (String name : teams.keySet()) {
                for (Tag t : teams.getListOrEmpty(name)) {
                    t.asString().ifPresent(s -> add(name, UUID.fromString(s)));
                }
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger("DiaithirClaims").warn("teams.dat unreadable: {}", e.toString());
        }
    }

    static void save() {
        if (file == null) return;
        CompoundTag root = new CompoundTag(), teams = new CompoundTag();
        MEMBERS.forEach((n, set) -> {
            ListTag l = new ListTag();
            set.forEach(u -> l.add(StringTag.valueOf(u.toString())));
            teams.put(n, l);
        });
        root.put("teams", teams);
        try {
            net.minecraft.nbt.NbtIo.writeCompressed(root, file);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger("DiaithirClaims").warn("teams.dat not saved: {}", e.toString());
        }
    }

    private static void add(String name, UUID who) {
        MEMBERS.computeIfAbsent(name, k -> new HashSet<>()).add(who);
        OF.put(who, name);
    }

    public static String teamOf(UUID who) {
        return OF.get(who);
    }

    public static boolean sameTeam(UUID a, UUID b) {
        String t = OF.get(a);
        return t != null && t.equals(OF.get(b));
    }

    public static List<UUID> members(String team) {
        return new ArrayList<>(MEMBERS.getOrDefault(team, Set.of()));
    }

    /** Returns an error text, or null on success. */
    public static String create(UUID who, String name) {
        if (OF.containsKey(who)) return "Leave your team first.";
        if (!name.matches("[A-Za-z0-9_]{2,24}")) return "Team names are 2 to 24 letters, digits or underscores.";
        if (MEMBERS.containsKey(name)) return "That name is taken.";
        add(name, who);
        save();
        return null;
    }

    public static String invite(UUID from, UUID to) {
        String t = OF.get(from);
        if (t == null) return "You are not in a team.";
        if (OF.containsKey(to)) return "They are already in a team.";
        INVITES.put(to, t);
        return null;
    }

    public static String join(UUID who) {
        String t = INVITES.remove(who);
        if (t == null) return "No invitation pending.";
        if (OF.containsKey(who)) return "Leave your team first.";
        if (!MEMBERS.containsKey(t)) return "That team no longer exists.";
        add(t, who);
        save();
        return null;
    }

    public static String leave(UUID who) {
        String t = OF.remove(who);
        if (t == null) return "You are not in a team.";
        Set<UUID> set = MEMBERS.get(t);
        set.remove(who);
        if (set.isEmpty()) MEMBERS.remove(t);
        save();
        return null;
    }
}
