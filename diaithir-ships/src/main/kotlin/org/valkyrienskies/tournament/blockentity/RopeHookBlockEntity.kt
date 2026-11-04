package org.valkyrienskies.tournament.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import org.joml.Vector3d
import org.valkyrienskies.core.internal.joints.VSJointId
import org.valkyrienskies.tournament.TournamentBlockEntities
import org.valkyrienskies.tournament.TournamentConfig
import org.valkyrienskies.tournament.TournamentDebugHelper
import org.valkyrienskies.tournament.util.debug.DebugLine
import org.valkyrienskies.tournament.util.helper.convertShipToWorldSpace
import java.awt.Color

class RopeHookBlockEntity(pos: BlockPos, state: BlockState):
        BlockEntity(TournamentBlockEntities.ROPE_HOOK.get(), pos, state)
{

    var ropeId: VSJointId? = null
    var mainPos: Vector3d? = null
    var otherPos: Vector3d? = null

    var maxLen: Double = 0.0

    var debugID: Long = -1

    var isSecondary = false
    var conPos: BlockPos? = null

    fun setRopeID(rope: VSJointId, mainIn: Vector3d?, otherIn: Vector3d?, level: Level) {
        println("Block>> $rope")

        val main = level.convertShipToWorldSpace(mainIn!!.add(0.5, 0.5, 0.5, Vector3d()))
        val other = level.convertShipToWorldSpace(otherIn!!.add(0.5, 0.5, 0.5, Vector3d()))

        ropeId = rope
        otherPos = other
        mainPos = main
        maxLen = 0.0

        debugID = TournamentDebugHelper.addObject(DebugLine(main, other, Color.RED, !TournamentConfig.CLIENT.particleRopeRenderer))

        level.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_ALL_IMMEDIATE)
    }

    fun setSecondary(main: BlockPos) {
        println("set secondary $main")

        isSecondary = true
        conPos = main

        level!!.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_ALL_IMMEDIATE)
    }

    override fun getUpdateTag(registries: net.minecraft.core.HolderLookup.Provider): CompoundTag =
        saveCustomOnly(registries)

    override fun getUpdatePacket(): ClientboundBlockEntityDataPacket? {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    override fun saveAdditional(output: ValueOutput) {
        if(mainPos == null || otherPos == null)
            return

        output.putDouble("mainX", mainPos!!.x)
        output.putDouble("mainY", mainPos!!.y)
        output.putDouble("mainZ", mainPos!!.z)

        output.putDouble("otherX", otherPos!!.x)
        output.putDouble("otherY", otherPos!!.y)
        output.putDouble("otherZ", otherPos!!.z)

        output.putDouble("maxLen", maxLen)
        output.putLong("debugID", debugID)
        println("saved debugID: $debugID")
        output.putInt("ropeId", ropeId!!)

        output.putBoolean("secondary", isSecondary)

        if(isSecondary) {
            println("saved: secondary")

            output.putInt("conX", conPos!!.x)
            output.putInt("conY", conPos!!.y)
            output.putInt("conZ", conPos!!.z)
        }
    }

    override fun loadAdditional(input: ValueInput) {
        mainPos = Vector3d(
            input.getDoubleOr("mainX", 0.0),
            input.getDoubleOr("mainY", 0.0),
            input.getDoubleOr("mainZ", 0.0),
        )

        otherPos = Vector3d(
            input.getDoubleOr("otherX", 0.0),
            input.getDoubleOr("otherY", 0.0),
            input.getDoubleOr("otherZ", 0.0),
        )

        maxLen = input.getDoubleOr("maxLen", 0.0)
        debugID = input.getLongOr("debugID", 0L)
        ropeId = input.getIntOr("ropeId", 0)

        isSecondary = input.getBooleanOr("secondary", false)
        if(isSecondary) {
            println("loaded secondary")
            conPos = BlockPos(
                input.getIntOr("conX", 0),
                input.getIntOr("conY", 0),
                input.getIntOr("conZ", 0),
            )
        }
    }

}
