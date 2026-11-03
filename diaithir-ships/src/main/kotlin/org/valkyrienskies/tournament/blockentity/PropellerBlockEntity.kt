package org.valkyrienskies.tournament.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import org.valkyrienskies.tournament.TournamentBlockEntities
import org.valkyrienskies.tournament.TournamentConfig
import org.valkyrienskies.tournament.blocks.PropellerBlock

abstract class PropellerBlockEntity<T: BlockEntity>(
    be: BlockEntityType<T>,
    pos: BlockPos,
    state: BlockState,
    val maxSpeed: Float,
    val accel: Float
): BlockEntity(be, pos, state) {

    var signal: Int = -1
    var rotation: Double = 0.0
    var speed: Double = 0.0

    fun tick(level: Level) {
        if (signal == -1) {
            signal = PropellerBlock.getPropSignal(blockState, level, blockPos)
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

    companion object {
        val ticker = BlockEntityTicker<PropellerBlockEntity<*>> { level, _, _, be ->
            be.tick(level)
        }
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

}

class BigPropellerBlockEntity(
    pos: BlockPos,
    state: BlockState,
): PropellerBlockEntity<BigPropellerBlockEntity>(
    TournamentBlockEntities.PROP_BIG.get(),
    pos,
    state,
    TournamentConfig.SERVER.propellerBigSpeed,
    TournamentConfig.SERVER.propellerBigAccel
)

class SmallPropellerBlockEntity(
    pos: BlockPos,
    state: BlockState,
): PropellerBlockEntity<SmallPropellerBlockEntity>(
    TournamentBlockEntities.PROP_SMALL.get(),
    pos,
    state,
    TournamentConfig.SERVER.propellerSmallSpeed,
    TournamentConfig.SERVER.propellerSmallAccel
)