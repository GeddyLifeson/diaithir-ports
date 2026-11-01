package com.iafenvoy.jupiter.render.widget;

import com.iafenvoy.jupiter.config.interfaces.ConfigEntry;
import com.iafenvoy.jupiter.config.interfaces.ConfigMetaProvider;
import com.iafenvoy.jupiter.render.TitleStack;
import com.iafenvoy.jupiter.render.screen.JupiterScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public abstract class WidgetBuilder<T> implements JupiterScreen {
    protected final Minecraft minecraft = Minecraft.getInstance();
    protected final ConfigMetaProvider provider;
    protected final ConfigEntry<T> config;
    protected StringWidget textWidget;
    protected Button resetButton;
    protected boolean canSave = true;

    protected WidgetBuilder(ConfigMetaProvider provider, ConfigEntry<T> config) {
        this.provider = provider;
        this.config = config;
    }

    public void addDialogElements(Context context, String text, int x, int y, int width, int height) {
        Font font = this.minecraft.font;
        this.textWidget = new StringWidget(20, y, font.width(text), height, Component.literal(text), font);
        context.addWidget(this.textWidget);
        this.resetButton = JupiterScreen.createButton(x + width - 50, y, 50, height, Component.translatable("jupiter.screen.button.remove", new Object[]{}), button -> {
            this.config.reset();
            this.refresh();
        });
        this.refreshResetButton(true);
        context.addWidget(this.resetButton);
        this.addCustomElements(context, x, y, width - 55, height);
    }

    public void addElements(Context context, int x, int y, int width, int height) {
        Font font = this.minecraft.font;
        Component component = this.config.getName();
        this.textWidget = new StringWidget(20, y, font.width(component), height, component, font);
        context.addWidget(this.textWidget);
        this.resetButton = JupiterScreen.createButton(x + width - 50, y, 50, height, Component.translatable("jupiter.screen.button.reset", new Object[]{}), button -> {
            this.config.reset();
            this.refresh();
        });
        this.refreshResetButton(false);
        this.config.registerCallback((n, r, d) -> this.refreshResetButton(!d));
        context.addWidget(this.resetButton);
        this.addCustomElements(context, x, y, width - 55, height);
    }

    private void refreshResetButton(boolean dialog) {
        this.setCanReset(dialog || this.config.canReset());
    }

    protected void setCanReset(boolean b) {
        this.resetButton.active = b;
    }

    public abstract void addCustomElements(Context context, int x, int y, int width, int height);

    public void update(boolean visible, int y) {
        if (this.textWidget != null) {
            this.textWidget.visible = this.textWidget.active = visible;
            this.textWidget.setY(y);
        }
        if (this.resetButton != null) {
            this.resetButton.visible = visible;
            this.resetButton.setY(y);
        }
        this.updateCustom(visible, y);
    }

    public abstract void updateCustom(boolean visible, int y);

    public abstract void refresh();

    public boolean isMouseOver(int mouseX, int mouseY) {
        return this.textWidget.visible && this.textWidget.isMouseOver(mouseX, mouseY);
    }

    public ConfigEntry<T> getConfig() {
        return this.config;
    }

    public boolean canSave() {
        return this.canSave;
    }

    public record Context(Screen parent, Consumer<AbstractWidget> appender, TitleStack titleStack) {
        public void addWidget(AbstractWidget widget) {
            this.appender.accept(widget);
        }

        public TitleStack push(Component title) {
            return this.titleStack.push(title);
        }
    }
}
