package net.diaithir.bestiary;

import net.minecraft.util.StringRepresentable;

public enum Preset implements StringRepresentable {
    BEAST, FLYER, MECH, AVATAR, STALKER;

    public static final StringRepresentable.EnumCodec<Preset> CODEC = StringRepresentable.fromEnum(Preset::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
