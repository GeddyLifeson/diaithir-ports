package org.valkyrienskies.mod.common.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.valkyrienskies.core.api.util.PhysTickOnly
import org.valkyrienskies.core.internal.joints.VSJointId
import org.valkyrienskies.core.internal.joints.VSJointPose
import org.valkyrienskies.core.internal.joints.VSRevoluteJoint
import org.valkyrienskies.mod.common.ValkyrienSkiesMod
import org.valkyrienskies.mod.common.dimensionId
import org.valkyrienskies.mod.util.getQuatd
import org.valkyrienskies.mod.util.getVector3d
import org.valkyrienskies.mod.util.putQuatd
import org.valkyrienskies.mod.util.putVector3d

@OptIn(PhysTickOnly::class)
class TestHingeBlockEntity(blockPos: BlockPos, blockState: BlockState) : BlockEntity(
    ValkyrienSkiesMod.TEST_HINGE_BLOCK_ENTITY_TYPE, blockPos, blockState
) {
    var otherHingePos: BlockPos? = null
    @Volatile
    var constraintId: VSJointId? = null

    var hingeConstraint: VSRevoluteJoint? = null

    private var makeConstraint = false

    override fun saveAdditional(output: ValueOutput) {
        super.saveAdditional(output)
        val h = hingeConstraint ?: return

        output.putLong("shipId0", h.shipId0 ?: -1)
        output.putLong("shipId1", h.shipId1 ?: -1)
        output.putVector3d("pos0", h.pose0.pos)
        output.putVector3d("pos1", h.pose1.pos)
        output.putQuatd("rot0", h.pose0.rot)
        output.putQuatd("rot1", h.pose1.rot)
    }

    override fun loadAdditional(input: ValueInput) {
        super.loadAdditional(input)
        hingeConstraint = VSRevoluteJoint(
            input.getLongOr("shipId0", -1), VSJointPose(input.getVector3d("pos0") ?: return, input.getQuatd("rot0") ?: return),
            input.getLongOr("shipId1", -1), VSJointPose(input.getVector3d("pos1") ?: return, input.getQuatd("rot1") ?: return),
            maxForceTorque = null, driveFreeSpin = true
        )
        makeConstraint = true
    }

    fun tick() {
        if (!makeConstraint) return
        if (level !is ServerLevel) return
        makeConstraint = false

        val level = level as ServerLevel
        ValkyrienSkiesMod.getOrCreateGTPA(level.dimensionId).addJoint(hingeConstraint!!, 4) {
            constraintId = it
        }
    }
}

