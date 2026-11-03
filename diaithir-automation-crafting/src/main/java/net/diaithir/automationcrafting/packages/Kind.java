package net.diaithir.automationcrafting.packages;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/** The three benches of The Package Mod. */
public enum Kind implements StringRepresentable {
    RESEARCHER, PACKAGER, UNPACKAGER;

    public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
