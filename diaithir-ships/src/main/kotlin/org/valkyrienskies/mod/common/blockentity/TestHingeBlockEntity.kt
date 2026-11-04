package org.valkyrienskies.mod.common.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
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

        val tag = CompoundTag()
        tag.putLong("shipId0", h.shipId0 ?: -1)
        tag.putLong("shipId1", h.shipId1 ?: -1)
        tag.putVector3d("pos0", h.pose0.pos)
        tag.putVector3d("pos1", h.pose1.pos)
        tag.putQuatd("rot0", h.pose0.rot)
        tag.putQuatd("rot1", h.pose1.rot)
        output.store(HINGE_KEY, CompoundTag.CODEC, tag)
    }

    override fun loadAdditional(input: ValueInput) {
        super.loadAdditional(input)
        val tag = input.read(HINGE_KEY, CompoundTag.CODEC).orElse(null) ?: return
        hingeConstraint = VSRevoluteJoint(
            tag.getLongOr("shipId0", 0L), VSJointPose(tag.getVector3d("pos0") ?: return, tag.getQuatd("rot0") ?: return),
            tag.getLongOr("shipId1", 0L), VSJointPose(tag.getVector3d("pos1") ?: return, tag.getQuatd("rot1") ?: return),
            maxForceTorque = null, driveFreeSpin = true
        )
        makeConstraint = true
    }

    override fun preRemoveSideEffects(pos: BlockPos, state: BlockState) {
        super.preRemoveSideEffects(pos, state)
        val level = level as? ServerLevel ?: return
        val id = constraintId ?: return
        ValkyrienSkiesMod.getOrCreateGTPA(level.dimensionId).removeJoint(id)
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

    companion object {
        private const val HINGE_KEY = "hinge_constraint"
    }
}
