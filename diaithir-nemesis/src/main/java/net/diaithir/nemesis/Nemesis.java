package net.diaithir.nemesis;

import java.util.ArrayList;
import java.util.List;

/** One named monster and its grudges. Players and Houses are stored by UUID string and House name. */
public final class Nemesis {
    static final String TAG = "nemesis_";

    public final int id;
    public String name;
    public final String epithet;
    public final String type;
    public String age;
    public int rank;
    public int generation;
    public int kills;
    public final List<String> traits;
    public final List<String> players;
    public final List<String> houses;
    /** Full name of the nemesis this one is heir to, or empty. */
    public String sire;
    /** UUID of the entity carrying this nemesis now, or empty while it is away. */
    public String entity;
    public boolean cheated;
    /** Game time of its last ambush; not saved, so a restart only resets the cooldown. */
    public long lastAmbush;
    public final String warband;
    /** Where it was last seen, as "x, z", or empty. */
    public String seen;

    public Nemesis(int id, String name, String epithet, String type, String age, int rank, int generation, int kills,
                   List<String> traits, List<String> players, List<String> houses, String sire, String entity, boolean cheated, String warband, String seen) {
        this.id = id;
        this.name = name;
        this.epithet = epithet;
        this.type = type;
        this.age = age;
        this.rank = rank;
        this.generation = generation;
        this.kills = kills;
        this.traits = new ArrayList<>(traits);
        this.players = new ArrayList<>(players);
        this.houses = new ArrayList<>(houses);
        this.sire = sire;
        this.entity = entity;
        this.cheated = cheated;
        this.warband = warband;
        this.seen = seen;
    }

    public String fullName() { return name + " " + epithet; }

    public String tag() { return TAG + id; }
}
