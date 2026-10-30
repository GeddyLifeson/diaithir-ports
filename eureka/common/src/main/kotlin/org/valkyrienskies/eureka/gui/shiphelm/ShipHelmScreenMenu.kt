package org.valkyrienskies.eureka.gui.shiphelm

import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.DataSlot
import net.minecraft.world.item.ItemStack
import org.valkyrienskies.eureka.EurekaConfig
import org.valkyrienskies.eureka.EurekaScreens
import org.valkyrienskies.eureka.blockentity.ShipHelmBlockEntity

class ShipHelmScreenMenu(syncId: Int, playerInv: Inventory, private val blockEntity: ShipHelmBlockEntity?) :
    AbstractContainerMenu(EurekaScreens.SHIP_HELM.get(), syncId) {

    constructor(syncId: Int, playerInv: Inventory) : this(syncId, playerInv, null)

    // Server-side these read live state off the block entity; client-side (blockEntity == null) they
    // are standalone slots that the vanilla menu sync writes into. They used to be plain vals captured
    // once at construction, so the GUI showed permanently stale state - which is what hid an "aligning"
    // flag left enabled by an aborted disassemble and made steering look broken.
    private val aligningData = addDataSlot(
        if (blockEntity != null) object : DataSlot() {
            override fun get(): Int = if (blockEntity.aligning) 1 else 0
            override fun set(value: Int) {}
        } else DataSlot.standalone()
    )

    private val assembledData = addDataSlot(
        if (blockEntity != null) object : DataSlot() {
            override fun get(): Int = if (blockEntity.assembled) 1 else 0
            override fun set(value: Int) {}
        } else DataSlot.standalone()
    )

    val aligning: Boolean get() = aligningData.get() != 0
    val assembled: Boolean get() = assembledData.get() != 0

    override fun stillValid(player: Player): Boolean = true

    override fun clickMenuButton(player: Player, id: Int): Boolean {
        if (blockEntity == null) return false

        if (id == 0 && !assembled && !player.level().isClientSide) {
            blockEntity.assemble(player)
            return true
        }

        if (id == 1 && assembled && !player.level().isClientSide) {
            blockEntity.align()
            return true
        }

        if (id == 3 && assembled && !player.level().isClientSide && EurekaConfig.SERVER.allowDisassembly) {
            blockEntity.disassemble()
            return true
        }

        return super.clickMenuButton(player, id)
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        // Do nothing
        return ItemStack.EMPTY
    }

    companion object {
        val factory: (syncId: Int, playerInv: Inventory) -> ShipHelmScreenMenu = ::ShipHelmScreenMenu
    }
}
