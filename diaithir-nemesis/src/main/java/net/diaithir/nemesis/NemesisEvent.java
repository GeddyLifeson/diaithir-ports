package net.diaithir.nemesis;

import net.neoforged.bus.api.Event;
import java.util.List;

/**
 * Posted on the NeoForge bus when a nemesis rises, falls or passes to an heir, so diaithir-ages can keep the same monster
 * as one of its characters. Plain fields only: the engine reads them by reflection and neither mod needs the other.
 */
public final class NemesisEvent extends Event {
    public final String kind; // rise, fall, heir
    public final int id, sire;
    public final String name, warband;
    public final List<String> players;

    NemesisEvent(String kind, Nemesis n, int sire) {
        this.kind = kind;
        this.id = n.id;
        this.sire = sire;
        this.name = n.fullName();
        this.warband = n.warband;
        this.players = List.copyOf(n.players);
    }
}
