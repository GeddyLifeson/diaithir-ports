package rearth.belts.foundry;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum FoundryLaneMode implements StringRepresentable {
    LEFT,
    RIGHT,
    AUTO;
    
    public FoundryLaneMode next() {
        return values()[(ordinal() + 1) % values().length];
    }
    
    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
