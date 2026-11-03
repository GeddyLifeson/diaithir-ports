package rearth.belts.foundry;

import java.util.Locale;

public enum FoundryPriority {
    LEFT,
    RIGHT,
    NONE;
    
    public FoundryPriority next() {
        return values()[(ordinal() + 1) % values().length];
    }
    
    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }
    
    public static FoundryPriority byOrdinal(int ordinal) {
        var all = values();
        return all[Math.floorMod(ordinal, all.length)];
    }
}
