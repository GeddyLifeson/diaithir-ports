package org.valkyrienskies.eureka.gui.engine

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Inventory
import org.valkyrienskies.eureka.EurekaMod

// 26.2 GUI overhaul: renderBg -> extractBackground, renderLabels -> extractLabels,
// GuiGraphics -> GuiGraphicsExtractor, RenderSystem shader state calls gone.
//
// The texture is 512x512 while classic MC GUI blits assume 256x256, so the old code drew at
// half-size constants under a pose().scale(2x) transform. 26.2's blit takes the texture size
// explicitly, so the pose-stack trick is replaced by a helper that doubles coords/UVs and
// samples the true 512x512 space directly.
class EngineScreen(handler: EngineScreenMenu, playerInventory: Inventory, text: Component) :
    AbstractContainerScreen<EngineScreenMenu>(handler, playerInventory, text) {

    override fun extractBackground(extractor: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.extractBackground(extractor, mouseX, mouseY, partialTick)
        val xP = (width - imageWidth) / 2
        val yP = (height - imageHeight) / 2

        // Draws in the old half-scale coordinate space: screen coords/sizes and texel UVs are all
        // doubled, sampling the full 512x512 texture (replaces the pose translate+scale(2x)).
        fun blit2(x: Int, y: Int, u: Int, v: Int, w: Int, h: Int) {
            extractor.blit(
                RenderPipelines.GUI_TEXTURED, TEXTURE,
                xP + x * 2, yP + y * 2, (u * 2).toFloat(), (v * 2).toFloat(), w * 2, h * 2, 512, 512
            )
        }

        // Draw the container background
        val (containerX, containerY) = if (menu.heatLevel > 1)
            Pair(HEATED_CONTAINER_X, HEATED_CONTAINER_Y)
        else
            Pair(CONTAINER_X, CONTAINER_Y)

        blit2(FIRE_HOLE_X, FIRE_HOLE_Y, containerX, containerY, FIRE_HOLE_WIDTH, FIRE_HOLE_HEIGHT)

        // region COALS
        // Draw the coal
        if (menu.fuelLeft != 0) {
            val t = COAL_MULTI_MAX - ((menu.fuelLeft.toFloat() / menu.fuelTotal.toFloat()) * COAL_MULTI_MAX)
            fun coal(xC: Int, yC: Int, heightC: Int, mult: Float) {
                val drop = (t * mult).toInt()
                val calcY = FIRE_HOLE_HEIGHT - heightC + drop
                blit2(FIRE_HOLE_X, FIRE_HOLE_Y + calcY, xC, yC, COAL_WIDTH, heightC)
            }

            coal(COAL_4_X, COAL_4_Y, COAL_4_HEIGHT, COAL_4_MULT)
            coal(COAL_3_X, COAL_3_Y, COAL_3_HEIGHT, COAL_3_MULT)
            coal(COAL_2_X, COAL_2_Y, COAL_2_HEIGHT, COAL_2_MULT)
            coal(COAL_1_X, COAL_1_Y, COAL_1_HEIGHT, COAL_1_MULT)
        }
        // endregion

        // Draw the glass background
        val (glassX, glassY) = if (menu.heatLevel > 3)
            Pair(HEATED_GLASS_X, HEATED_GLASS_Y)
        else
            Pair(GLASS_X, GLASS_Y)

        blit2(FIRE_HOLE_X, FIRE_HOLE_Y, glassX, glassY, FIRE_HOLE_WIDTH, FIRE_HOLE_HEIGHT)

        // Draw the inventory
        blit2(0, 0, 0, 0, imageWidth / 2, imageHeight / 2)
    }

    override fun extractLabels(extractor: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        // Intentionally empty: suppress the default title/inventory labels.
    }

    companion object { // TEXTURE DATA
        internal val TEXTURE = Identifier.fromNamespaceAndPath(EurekaMod.MOD_ID, "textures/gui/engine.png")

        private const val FIRE_HOLE_X = 10 / 2
        private const val FIRE_HOLE_Y = 8 / 2

        private const val FIRE_HOLE_WIDTH = 156 / 2
        private const val FIRE_HOLE_HEIGHT = 68 / 2

        private const val HEATED_GLASS_X = 10 / 2
        private const val HEATED_GLASS_Y = 172 / 2
        private const val GLASS_X = 10 / 2
        private const val GLASS_Y = 244 / 2

        private const val HEATED_CONTAINER_X = 10 / 2
        private const val HEATED_CONTAINER_Y = 390 / 2
        private const val CONTAINER_X = 10 / 2
        private const val CONTAINER_Y = 318 / 2

        // TODO fill in actual pixel coords
        private const val COAL_4_X = 184 / 2
        private const val COAL_4_Y = 18 / 2
        private const val COAL_3_X = 184 / 2
        private const val COAL_3_Y = 80 / 2
        private const val COAL_2_X = 184 / 2
        private const val COAL_2_Y = 128 / 2
        private const val COAL_1_X = 184 / 2
        private const val COAL_1_Y = 166 / 2
        private const val COAL_WIDTH = 158 / 2
        private const val COAL_4_HEIGHT = 60 / 2
        private const val COAL_3_HEIGHT = 44 / 2
        private const val COAL_2_HEIGHT = 34 / 2
        private const val COAL_1_HEIGHT = 26 / 2

        private const val COAL_MULTI_MAX = COAL_1_HEIGHT.toFloat()
        private const val COAL_4_MULT = COAL_4_HEIGHT.toFloat() / COAL_MULTI_MAX
        private const val COAL_3_MULT = COAL_3_HEIGHT.toFloat() / COAL_MULTI_MAX
        private const val COAL_2_MULT = COAL_2_HEIGHT.toFloat() / COAL_MULTI_MAX
        private const val COAL_1_MULT = 1f
    }
}
