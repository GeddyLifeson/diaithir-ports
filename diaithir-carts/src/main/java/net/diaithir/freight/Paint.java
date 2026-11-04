package net.diaithir.freight;

import net.minecraft.util.StringRepresentable;

/** Wall colour: 20 ft boxes are red, 40 ft blue, reefers and tanks white. */
public enum Paint implements StringRepresentable {
    RED, BLUE, WHITE;

    @Override public String getSerializedName() { return name().toLowerCase(java.util.Locale.ROOT); }

    public static Paint of(LoadType t) {
        return t.cold() || t.kind() == LoadType.Kind.TANK ? WHITE : t.l() > 8 ? BLUE : RED;
    }
}
