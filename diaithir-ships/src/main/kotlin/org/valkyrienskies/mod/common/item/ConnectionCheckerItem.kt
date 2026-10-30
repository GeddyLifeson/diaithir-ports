package org.valkyrienskies.mod.common.item

import net.minecraft.ChatFormatting
import net.minecraft.core.particles.DustParticleOptions
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.ARGB
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.level.block.state.BlockState
import org.joml.Vector3f
import org.valkyrienskies.core.api.world.connectivity.ConnectionStatus
import org.valkyrienskies.mod.common.dimensionId
import org.valkyrienskies.mod.common.getShipManagingPos
import org.valkyrienskies.mod.common.shipObjectWorld
import java.util.function.DoubleSupplier

class ConnectionCheckerItem(
    properties: Properties, private val scale: DoubleSupplier, private val minScaling: DoubleSupplier
) : Item(properties) {

    override fun isFoil(stack: ItemStack): Boolean {
        return true
    }

    // In 26.1 Item.inventoryTick only runs on the server, so the selection particles are sent
    // to the holding player with a particle packet instead of being spawned client side.
    override fun inventoryTick(item: ItemStack, level: ServerLevel, entity: Entity, slot: EquipmentSlot?) {
        super.inventoryTick(item, level, entity, slot)
        val firstPos = FirstPosData.get(item) ?: return
        val player = entity as? ServerPlayer ?: return

        //spawn grey particles surrounding originally selected position
        for (x in -1..1) for (y in -1..1) for (z in -1..1) {
            level.sendParticles(
                player,
                ParticleTypes.SMOKE,
                true,
                true,
                firstPos.x + 0.5 + x * 0.5,
                firstPos.y + 0.5 + y * 0.5,
                firstPos.z + 0.5 + z * 0.5,
                1,
                0.0,
                0.0,
                0.0,
                0.0
            )
        }
    }

    override fun useOn(ctx: UseOnContext): InteractionResult {
        val level = ctx.level ?: return super.useOn(ctx)
        val blockPos = ctx.clickedPos
        val blockState: BlockState = level.getBlockState(blockPos)
        val item = ctx.itemInHand

        if (item.item !is ConnectionCheckerItem) {
            return InteractionResult.FAIL
        }

        val parentShip = ctx.level.getShipManagingPos(blockPos)
        if (!blockState.isAir) {
            // Make a ship
            val dimensionId = level.dimensionId

            if (parentShip != null) {
                val firstPos = FirstPosData.get(item)
                if (firstPos != null) {
                    val firstPosX = firstPos.x
                    val firstPosY = firstPos.y
                    val firstPosZ = firstPos.z
                    val connected = level.shipObjectWorld.isConnectedBySolid(blockPos.x, blockPos.y, blockPos.z, firstPosX, firstPosY, firstPosZ, dimensionId)
                    val chatColor = when (connected) {
                        ConnectionStatus.CONNECTED -> ChatFormatting.GREEN
                        ConnectionStatus.DISCONNECTED -> ChatFormatting.RED
                        ConnectionStatus.UNKNOWN -> ChatFormatting.YELLOW
                    }
                    if (!level.isClientSide) {
                        ctx.player?.sendSystemMessage(Component.literal("[SERVERSIDE] ").withStyle(ChatFormatting.LIGHT_PURPLE).append(Component.literal("Connected:").withStyle(
                            ChatFormatting.RESET)).append(Component.literal(" $connected").withStyle(chatColor)))
                    } else {
                        ctx.player?.sendSystemMessage(Component.literal("[CLIENTSIDE] ").withStyle(ChatFormatting.GOLD).append(Component.literal("Connected:").withStyle(
                            ChatFormatting.RESET)).append(Component.literal(" $connected").withStyle(chatColor)))
                        val color = when (connected) {
                            ConnectionStatus.CONNECTED -> Vector3f(0.0f, 1.0f, 0.0f) // Green
                            ConnectionStatus.DISCONNECTED -> Vector3f(1.0f, 0.0f, 0.0f) // Red
                            ConnectionStatus.UNKNOWN -> Vector3f(1.0f, 1.0f, 0.0f) // Yellow
                        }
                        for (x in -1..1) for (y in -1..1) for (z in -1..1) {
                            repeat (1) {
                                level.addParticle(
                                    DustParticleOptions(ARGB.colorFromFloat(1f, color.x, color.y, color.z), 1f),
                                    true,
                                    true,
                                    blockPos.x.toDouble() + 0.5 + x * 0.5,
                                    blockPos.y.toDouble() + 0.5 + y * 0.5,
                                    blockPos.z.toDouble() + 0.5 + z * 0.5,
                                    0.0,
                                    0.0,
                                    0.0
                                )
                            }
                        }
                    }
                    FirstPosData.clear(item)
                } else {
                    FirstPosData.set(item, blockPos)
                    if (!level.isClientSide) {
                        ctx.player?.sendSystemMessage(
                            Component.literal("First block selected: (${blockPos.x}, ${blockPos.y}, ${blockPos.z})").withStyle(
                                ChatFormatting.ITALIC))
                    }
                }
            }
        }

        return super.useOn(ctx)
    }
}
