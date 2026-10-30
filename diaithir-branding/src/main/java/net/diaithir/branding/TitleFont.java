package net.diaithir.branding;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

/** Screen titles are set in Uncial Antiqua (UI schema section 1). Text that already picks a font keeps it. */
public final class TitleFont {
    private static final FontDescription UNCIAL = new FontDescription.Resource(Identifier.fromNamespaceAndPath("diaithirbranding", "uncial"));

    private TitleFont() {
    }

    public static Component apply(Component title) {
        if (title == null || title.getString().isEmpty()) return title;
        return title.copy().withStyle(style -> style.getFont() == FontDescription.DEFAULT ? style.withFont(UNCIAL) : style);
    }
}
