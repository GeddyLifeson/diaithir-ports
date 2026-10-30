package com.iafenvoy.jupiter.render.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class TextFieldWithErrorWidget extends EditBox {
    private boolean hasError = false;

    public TextFieldWithErrorWidget(Font font, int x, int y, int width, int height) {
        super(font, x, y, width, height, Component.empty());
    }

    @Override
    public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (this.hasError) {
            this.setTextColorUneditable(0xFFFF0000);
            this.setEditable(false);
        }
        super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
        this.setEditable(true);
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }
}
