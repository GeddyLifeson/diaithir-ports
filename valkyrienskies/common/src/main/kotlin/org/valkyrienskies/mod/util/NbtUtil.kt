package org.valkyrienskies.mod.util

import com.mojang.serialization.Codec
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.joml.Quaterniond
import org.joml.Quaterniondc
import org.joml.Vector3d
import org.joml.Vector3dc

fun CompoundTag.putVector3d(prefix: String, vector3d: Vector3dc) =
    with(vector3d) {
        putDouble(prefix + "x", x())
        putDouble(prefix + "y", y())
        putDouble(prefix + "z", z())
    }

fun CompoundTag.getVector3d(prefix: String): Vector3d? {
    return if (
        !this.contains(prefix + "x") ||
        !this.contains(prefix + "y") ||
        !this.contains(prefix + "z")
    ) {
        null
    } else {
        Vector3d(
            this.getDouble(prefix + "x").orElse(0.0),
            this.getDouble(prefix + "y").orElse(0.0),
            this.getDouble(prefix + "z").orElse(0.0)
        )
    }
}

fun CompoundTag.putQuatd(prefix: String, quat: Quaterniondc) =
    with(quat) {
        putDouble(prefix + "x", x())
        putDouble(prefix + "y", y())
        putDouble(prefix + "z", z())
        putDouble(prefix + "w", w())
    }

fun CompoundTag.getQuatd(prefix: String): Quaterniond? =
    if (
        this.contains(prefix + "x") &&
        this.contains(prefix + "y") &&
        this.contains(prefix + "z") &&
        this.contains(prefix + "w")
    ) {
        Quaterniond(
            getDouble(prefix + "x").orElse(0.0),
            getDouble(prefix + "y").orElse(0.0),
            getDouble(prefix + "z").orElse(0.0),
            getDouble(prefix + "w").orElse(0.0),
        )
    } else null

// 26.2: BlockEntity#saveAdditional/loadAdditional now take ValueOutput/ValueInput instead of a raw
// CompoundTag. ValueOutput's put* methods share CompoundTag's names, but ValueInput reads use
// getXOr(key, default) instead of a Optional-returning getX(key), so mirror the helpers above.
fun ValueOutput.putVector3d(prefix: String, vector3d: Vector3dc) =
    with(vector3d) {
        putDouble(prefix + "x", x())
        putDouble(prefix + "y", y())
        putDouble(prefix + "z", z())
    }

// ValueInput has no Optional-returning getDouble(key) (only getDoubleOr with a default), so use
// the generic Codec-based read() to check presence.
private fun ValueInput.hasDouble(key: String): Boolean = read(key, Codec.DOUBLE).isPresent

fun ValueInput.getVector3d(prefix: String): Vector3d? {
    return if (
        !hasDouble(prefix + "x") ||
        !hasDouble(prefix + "y") ||
        !hasDouble(prefix + "z")
    ) {
        null
    } else {
        Vector3d(
            getDoubleOr(prefix + "x", 0.0),
            getDoubleOr(prefix + "y", 0.0),
            getDoubleOr(prefix + "z", 0.0)
        )
    }
}

fun ValueOutput.putQuatd(prefix: String, quat: Quaterniondc) =
    with(quat) {
        putDouble(prefix + "x", x())
        putDouble(prefix + "y", y())
        putDouble(prefix + "z", z())
        putDouble(prefix + "w", w())
    }

fun ValueInput.getQuatd(prefix: String): Quaterniond? =
    if (
        hasDouble(prefix + "x") &&
        hasDouble(prefix + "y") &&
        hasDouble(prefix + "z") &&
        hasDouble(prefix + "w")
    ) {
        Quaterniond(
            getDoubleOr(prefix + "x", 0.0),
            getDoubleOr(prefix + "y", 0.0),
            getDoubleOr(prefix + "z", 0.0),
            getDoubleOr(prefix + "w", 0.0),
        )
    } else null
