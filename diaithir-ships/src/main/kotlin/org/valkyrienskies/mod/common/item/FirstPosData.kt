package org.valkyrienskies.mod.common.item

import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData

/**
 * Helpers to store/read the "first selected position" of selection-type items in the
 * [DataComponents.CUSTOM_DATA] component.
 */
internal object FirstPosData {
    private const val X = "firstPosX"
    private const val Y = "firstPosY"
    private const val Z = "firstPosZ"

    fun get(stack: ItemStack): BlockPos? {
        val customData = stack.get(DataComponents.CUSTOM_DATA) ?: return null
        if (!customData.contains(X)) return null
        val tag = customData.copyTag()
        return BlockPos(tag.getIntOr(X, 0), tag.getIntOr(Y, 0), tag.getIntOr(Z, 0))
    }

    fun set(stack: ItemStack, pos: BlockPos) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack) { tag ->
            tag.putInt(X, pos.x)
            tag.putInt(Y, pos.y)
            tag.putInt(Z, pos.z)
        }
    }

    fun clear(stack: ItemStack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack) { tag ->
            tag.remove(X)
            tag.remove(Y)
            tag.remove(Z)
        }
    }
}
