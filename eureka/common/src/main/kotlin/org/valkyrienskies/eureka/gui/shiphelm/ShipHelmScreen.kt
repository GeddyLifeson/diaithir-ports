package org.valkyrienskies.eureka.gui.shiphelm

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.phys.BlockHitResult
import org.valkyrienskies.core.api.ships.Ship
import org.valkyrienskies.eureka.EurekaConfig
import org.valkyrienskies.eureka.EurekaMod
import org.valkyrienskies.mod.common.getShipManagingPos

// 26.2 GUI overhaul: GuiGraphics -> GuiGraphicsExtractor (extract/render split), renderBg ->
// extractBackground(extractor, mouseX, mouseY, partialTick), renderLabels -> extractLabels,
// drawString -> text, blit now takes a RenderPipeline + explicit texture size, the
// RenderSystem.setShader*/setShaderColor/setShaderTexture calls are gone entirely, and mouse
// callbacks take MouseButtonEvent records instead of (mouseX, mouseY, button).
class ShipHelmScreen(handler: ShipHelmScreenMenu, playerInventory: Inventory, text: Component) :
    AbstractContainerScreen<ShipHelmScreenMenu>(handler, playerInventory, text) {

    private lateinit var assembleButton: ShipHelmButton
    private lateinit var alignButton: ShipHelmButton
    private lateinit var disassembleButton: ShipHelmButton

    private var pos = (Minecraft.getInstance().hitResult as? BlockHitResult)?.blockPos
    private var ship: Ship? = pos?.let { Minecraft.getInstance().level?.getShipManagingPos(it) }

    init {
        titleLabelX = 6
        titleLabelY = 6
    }

    override fun init() {
        super.init()
        val x = (width - imageWidth) / 2
        val y = (height - imageHeight) / 2

        assembleButton = addRenderableWidget(
            ShipHelmButton(x + BUTTON_1_X, y + BUTTON_1_Y, ASSEMBLE_TEXT, font) {
                minecraft?.gameMode?.handleInventoryButtonClick(menu.containerId, 0)
            }
        )

        alignButton = addRenderableWidget(
            ShipHelmButton(x + BUTTON_2_X, y + BUTTON_2_Y, ALIGN_TEXT, font) {
                minecraft?.gameMode?.handleInventoryButtonClick(menu.containerId, 1)
            }
        )

        disassembleButton = addRenderableWidget(
            ShipHelmButton(x + BUTTON_3_X, y + BUTTON_3_Y, DISSEMBLE_TEXT, font) {
                minecraft?.gameMode?.handleInventoryButtonClick(menu.containerId, 3)
            }
        )

        disassembleButton.active = EurekaConfig.SERVER.allowDisassembly
        updateButtons()
    }

    private fun updateButtons() {
        val newPos = (Minecraft.getInstance().hitResult as? BlockHitResult)?.blockPos
        if (newPos != null) {
            pos = newPos
        }
        val newShip = pos?.let { Minecraft.getInstance().level?.getShipManagingPos(it) }
        if (newShip != null) {
            ship = newShip
        }

        val isLookingAtShip = ship != null

        assembleButton.active = !isLookingAtShip
        disassembleButton.active = EurekaConfig.SERVER.allowDisassembly && isLookingAtShip
        alignButton.active = disassembleButton.active
    }

    override fun extractBackground(extractor: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.extractBackground(extractor, mouseX, mouseY, partialTick)
        updateButtons()

        val x = (width - imageWidth) / 2
        val y = (height - imageHeight) / 2
        extractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0f, 0f, imageWidth, imageHeight, 256, 256)
    }

    override fun extractLabels(extractor: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        if (this.menu.aligning) {
            alignButton.message = ALIGNING_TEXT
            alignButton.active = false
        } else {
            alignButton.message = ALIGN_TEXT
            alignButton.active = true
        }

        // TODO render stats
        if (ship == null) return
        // 26.2 colors are full ARGB - a 0x00-alpha color like the old 0x404040 renders invisible.
        ship!!.slug?.let { extractor.text(font, it, titleLabelX, titleLabelY, LABEL_COLOR, false) }
        extractor.text(font, String.format("%.2f", ship!!.velocity.length()) + "m/s", 8, 25, LABEL_COLOR, false)
    }

    // mojank doesn't check mouse release for their widgets for some reason
    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        isDragging = false
        if (getChildAt(event.x(), event.y()).filter { it.mouseReleased(event) }.isPresent) return true

        return super.mouseReleased(event)
    }

    companion object { // TEXTURE DATA
        internal val TEXTURE = Identifier.fromNamespaceAndPath(EurekaMod.MOD_ID, "textures/gui/ship_helm.png")

        private const val LABEL_COLOR = 0xFF404040.toInt()

        private const val BUTTON_1_X = 10
        private const val BUTTON_1_Y = 73
        private const val BUTTON_2_X = 10
        private const val BUTTON_2_Y = 103
        private const val BUTTON_3_X = 10
        private const val BUTTON_3_Y = 133

        private val ASSEMBLE_TEXT = Component.translatable("gui.vs_eureka.assemble")
        private val DISSEMBLE_TEXT = Component.translatable("gui.vs_eureka.disassemble")
        private val ALIGN_TEXT = Component.translatable("gui.vs_eureka.align")
        private val ALIGNING_TEXT = Component.translatable("gui.vs_eureka.aligning")
    }
}
