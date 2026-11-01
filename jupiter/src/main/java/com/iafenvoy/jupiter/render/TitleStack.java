package com.iafenvoy.jupiter.render;

import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public final class TitleStack {
    private static final Component OMIT = Component.literal("...");
    private static final Component SEPARATOR = Component.literal(" -> ").withStyle(ChatFormatting.GRAY);
    private final Font font = Minecraft.getInstance().font;
    private final List<Component> titles;
    private MutableComponent cachedTitle = Component.empty();

    private TitleStack(List<Component> titles) {
        this.titles = titles;
    }

    public TitleStack push(Component title) {
        ImmutableList.Builder<Component> builder = ImmutableList.builder();
        builder.addAll(this.titles);
        builder.add(title);
        return new TitleStack(builder.build());
    }

    public static TitleStack create(Component title) {
        return new TitleStack(List.of(title));
    }

    public void cacheTitle(int width) {
        if (this.titles.isEmpty()) return;
        MutableComponent part = Component.empty();
        this.cachedTitle = Component.empty();
        for (int i = this.titles.size() - 1; i >= 0; i--) {
            boolean first = i == this.titles.size() - 1;
            if (first) {
                part.append(this.titles.get(i));
                if (i > 0) this.cachedTitle.append(OMIT).append(SEPARATOR);
                this.cachedTitle.append(part);
            } else {
                part = Component.empty().append(this.titles.get(i)).append(SEPARATOR).append(part);
                MutableComponent component = Component.empty();
                if (i > 0) component.append(OMIT).append(SEPARATOR);
                component.append(part);
                if (this.font.width(component) > width) break;
                this.cachedTitle = component;
            }
        }
    }

    public Component getTitle() {
        return this.cachedTitle;
    }
}
