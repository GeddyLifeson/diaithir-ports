package io.github.tr100000.trutils.api.gui;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;

public class BlankIcon implements Icon {
    public static final BlankIcon INSTANCE = new BlankIcon();
    public static final IconType<BlankIcon> TYPE = new IconType<>("blank", MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

    @Override
    public IconType<?> getType() {
        return TYPE;
    }
}
