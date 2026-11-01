package org.valkyrienskies.eureka.gui.shiphelm

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.util.FormattedCharSequence

// 26.2: AbstractButton.extractWidgetRenderState is FINAL (buttons draw through the shared sprite
// path), so a custom-textured button can no longer extend Button - extend AbstractWidget directly
// and implement the press callback ourselves. GuiGraphics -> GuiGraphicsExtractor, drawString ->
// text, blit takes RenderPipeline + texture size, RenderSystem shader/blend state calls are gone,
// and onClick/onRelease take MouseButtonEvent.
class ShipHelmButton(
    x: Int,
    y: Int,
    text: Component,
    private val font: Font,
    private val onPress: () -> Unit,
) : AbstractWidget(x, y, 156, 23, text) {

    var isPressed = false

    init {
        active = true
    }

    override fun extractWidgetRenderState(extractor: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!isHovered) isPressed = false

        if (this.isPressed || !this.active) {
            extractor.blit(
                RenderPipelines.GUI_TEXTURED, ShipHelmScreen.TEXTURE,
                x, y, BUTTON_P_X.toFloat(), BUTTON_P_Y.toFloat(), width, height, 256, 256
            )
        } else if (this.isHovered) {
            extractor.blit(
                RenderPipelines.GUI_TEXTURED, ShipHelmScreen.TEXTURE,
                x, y, BUTTON_H_X.toFloat(), BUTTON_H_Y.toFloat(), width, height, 256, 256
            )
        }

        // 26.2 colors are full ARGB - zero alpha renders invisible.
        val color = 0xFF404040.toInt()
        val formattedCharSequence: FormattedCharSequence = message.visualOrderText
        extractor.text(
            font,
            formattedCharSequence,
            ((x + width / 2) - font.width(formattedCharSequence) / 2),
            (y + (height - 8) / 2),
            color,
            false
        )
    }

    override fun onClick(event: MouseButtonEvent, isDoubleClick: Boolean) {
        isPressed = true
        onPress()
    }

    override fun onRelease(event: MouseButtonEvent) {
        isPressed = false
    }

    override fun updateWidgetNarration(output: NarrationElementOutput) {
        defaultButtonNarrationText(output)
    }

    companion object {
        private const val BUTTON_H_X = 0
        private const val BUTTON_H_Y = 166
        private const val BUTTON_P_X = 0
        private const val BUTTON_P_Y = 189
    }
}
