package io.github.tr100000.trutils.api.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.Arrays;
import java.util.List;

public abstract class ExtendedTooltipComponent implements ClientTooltipComponent {
    protected void text(final GuiGraphicsExtractor graphics, Font font, FormattedCharSequence text, int x, int y) {
        graphics.text(font, text, x, y, -1, true);
    }

    protected int longestText(Font font, FormattedCharSequence... text) {
        return longestText(font, 0, text);
    }

    protected int longestText(Font font, int currentLongest, FormattedCharSequence... text) {
        return Math.max(Arrays.stream(text).mapToInt(font::width).max().orElse(0), currentLongest);
    }

    protected int longestText(Font font, List<FormattedCharSequence> text) {
        return longestText(font, 0, text);
    }

    protected int longestText(Font font, int currentLongest, List<FormattedCharSequence> text) {
        return Math.max(text.stream().mapToInt(font::width).max().orElse(0), currentLongest);
    }
}
