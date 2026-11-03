package org.valkyrienskies.tournament.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.state.BlockState
import org.valkyrienskies.tournament.TournamentBlockEntities
import org.valkyrienskies.tournament.TournamentConfig
import org.valkyrienskies.tournament.blocks.PropellerBlock

class RotatorBlockEntity(
    pos: BlockPos,
    state: BlockState
): BlockEntity(TournamentBlockEntities.ROTATOR.get(), pos, state) {

    var signal: Int = -1
    var rotation: Double = 0.0
    var speed: Double = 0.0

    fun tick() {
        val maxSpeed = TournamentConfig.SERVER.rotatorSpeed
        val accel = TournamentConfig.SERVER.rotatorAccel

        if (signal == -1) {
            signal = PropellerBlock.getPropSignal(blockState, level!!, blockPos)
        }
        val targetSpeed = signal / 15.0f * maxSpeed
        if (speed < targetSpeed) {
            speed += accel
        } else if (speed > targetSpeed) {
            speed -= accel * 2
        }
        if (speed < 0.0f) {
            speed = 0.0
        }
        rotation -= speed
        rotation %= 360.0
    }

    override fun getUpdateTag(registries: net.minecraft.core.HolderLookup.Provider): CompoundTag =
        saveCustomOnly(registries)

    override fun getUpdatePacket(): ClientboundBlockEntityDataPacket? =
        ClientboundBlockEntityDataPacket.create(this)

    override fun saveAdditional(output: ValueOutput) {
        output.putDouble("speed", speed)
        output.putDouble("rotation", rotation)
        output.putInt("signal", signal)

        super.saveAdditional(output)
    }

    override fun loadAdditional(input: ValueInput) {
        speed = input.getDoubleOr("speed", 0.0)
        rotation = input.getDoubleOr("rotation", 0.0)
        signal = input.getIntOr("signal", 0)

        super.loadAdditional(input)
    }

    fun update() {
        level?.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_ALL_IMMEDIATE)
    }

    companion object {
        val ticker = BlockEntityTicker<RotatorBlockEntity> { level, _, _, be ->
            be.tick()
        }
    }

}