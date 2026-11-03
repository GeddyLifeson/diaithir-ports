// Ported from Pick Up Notifier (MPL-2.0) https://github.com/Fuzss/pickupnotifier
// Modifications: config access via ClientConfig statics; rarity style computed locally instead of Puzzles Lib.
package net.diaithir.hudoverlays.pickupnotifier.client.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.diaithir.hudoverlays.pickupnotifier.client.config.ClientConfig;
import net.diaithir.hudoverlays.pickupnotifier.client.util.DisplayEntryRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Rarity;

public abstract class DisplayEntry<T> {
    public static final int ELEMENT_HEIGHT = 18;
    private static final int TEXT_ITEM_MARGIN = 4;

    protected final T item;
    private final int displayAmount;
    private final Component component;
    private int remainingTicks = ClientConfig.DISPLAY_TIME.get();

    public DisplayEntry(T item, int displayAmount, Rarity rarity) {
        this.item = item;
        this.displayAmount = displayAmount;
        this.component = Component.empty().append(this.createTextComponent()).setStyle(this.getComponentStyle(rarity));
    }

    public Object getKey() {
        return this;
    }

    protected abstract Component getEntryName(T item);

    public int getDisplayAmount() {
        return this.displayAmount;
    }

    public boolean mayDiscard() {
        return this.remainingTicks <= 0;
    }

    public void tick() {
        if (this.remainingTicks > 0) {
            this.remainingTicks--;
        }
    }

    protected final Component createTextComponent() {
        boolean reverse = ClientConfig.POSITION.get().isRight();
        List<Component> components = new ArrayList<>();
        this.appendTextComponents(components, reverse);
        if (reverse) {
            Collections.reverse(components);
        }
        return components.stream()
                .reduce((c1, c2) -> Component.empty().append(c1).append(CommonComponents.SPACE).append(c2))
                .orElse(Component.empty());
    }

    protected void appendTextComponents(List<Component> components, boolean reverse) {
        if (ClientConfig.DISPLAY_ITEM_NAME.get()) {
            components.add(this.getEntryName(this.item));
        }
        if (ClientConfig.DISPLAY_AMOUNT.get().isText()) {
            if (this.displayAmount > 1 || this.displayAmount == 1 && ClientConfig.DISPLAY_SINGLE_COUNT.get()) {
                components.add(Component.literal(reverse ? this.displayAmount + "x" : "x" + this.displayAmount));
            }
        }
    }

    private Style getComponentStyle(Rarity rarity) {
        if (!ClientConfig.IGNORE_RARITY.get() && rarity != Rarity.COMMON) {
            return Style.EMPTY.withColor(rarity.color());
        } else {
            return Style.EMPTY.withColor(ClientConfig.TEXT_COLOR.get());
        }
    }

    public static double getRelativeRemainingTicks(Collection<DisplayEntry<?>> values, float partialTick) {
        return values.stream()
                .mapToDouble(entry -> 1.0 - entry.getRelativeRemainingTicks(partialTick))
                .average()
                .orElse(0.0) * values.size();
    }

    public final float getRelativeRemainingTicks(float partialTick) {
        float moveTime = ClientConfig.getMoveTime();
        if (moveTime <= 0.0F) {
            return this.remainingTicks > 0 ? 1.0F : 0.0F;
        }
        return Mth.clamp((this.remainingTicks - partialTick) / moveTime, 0.0F, 1.0F);
    }

    public abstract DisplayEntry<?> mergeWith(DisplayEntry<?> otherDisplayEntry);

    public int getEntryWidth(Font font) {
        int textWidth = font.width(this.component);
        return ClientConfig.DRAW_SPRITE.get() ? textWidth + (textWidth == 0 ? 0 : TEXT_ITEM_MARGIN) + 16 : textWidth;
    }

    public void render(GuiGraphicsExtractor guiGraphics, Font font, int posX, int posY, float alpha) {
        float scale = ClientConfig.getDisplayScale();
        boolean mirrorPosition = ClientConfig.POSITION.get().isRight();
        boolean withSprite = ClientConfig.DRAW_SPRITE.get();
        int textStartX = mirrorPosition || !withSprite ? posX : posX + 16 + TEXT_ITEM_MARGIN;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(scale, scale);

        this.renderBg(guiGraphics, font, posX, posY, alpha);
        guiGraphics.text(font, this.component, textStartX, posY + 4, ARGB.white(alpha), true);
        if (withSprite) {
            int textWidth = font.width(this.component);
            this.renderSprite(guiGraphics, font,
                    mirrorPosition ? posX + textWidth + (textWidth == 0 ? 0 : TEXT_ITEM_MARGIN) : posX,
                    posY, alpha);
        }
        guiGraphics.pose().popMatrix();
    }

    private void renderBg(GuiGraphicsExtractor guiGraphics, Font font, int posX, int posY, float alpha) {
        switch (ClientConfig.ENTRY_BACKGROUND.get()) {
            case CHAT -> {
                int backgroundOpacity = ARGB.color(ARGB.as8BitChannel(Mth.clamp(
                        Minecraft.getInstance().options.textBackgroundOpacity().get().floatValue() * alpha,
                        0.0F, 1.0F)), 0);
                int endY = posY + 16;
                if (ClientConfig.DISPLAY_AMOUNT.get().isSprite()) {
                    endY += 1;
                }
                guiGraphics.fill(posX - 3, posY, posX + this.getEntryWidth(font) + 5, endY, backgroundOpacity);
            }
            case TOOLTIP -> DisplayEntryRenderHelper.extractTooltipBackground(guiGraphics, posX, posY + 3,
                    this.getEntryWidth(font), 9, ARGB.white(alpha));
            default -> {
            }
        }
    }

    protected abstract void renderSprite(GuiGraphicsExtractor guiGraphics, Font font, int posX, int posY, float alpha);

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();
}
