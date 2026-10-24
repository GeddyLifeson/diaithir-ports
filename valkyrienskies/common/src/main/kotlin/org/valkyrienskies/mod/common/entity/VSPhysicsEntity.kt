package org.valkyrienskies.mod.common.entity

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerEntity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.level.Level
import net.minecraft.world.level.entity.EntityInLevelCallback
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.Vec3
import org.joml.Matrix3d
import org.joml.Vector3d
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.ships.properties.ShipInertiaData
import org.valkyrienskies.core.api.ships.properties.ShipTransform
import org.valkyrienskies.core.internal.physics.PhysicsEntityData
import org.valkyrienskies.core.internal.physics.PhysicsEntityServer
import org.valkyrienskies.core.internal.physics.VSSphereCollisionShapeData
import org.valkyrienskies.core.internal.world.VsiClientShipWorld
import org.valkyrienskies.core.internal.world.VsiServerShipWorld
import org.valkyrienskies.mod.api.vsApi
import org.valkyrienskies.mod.common.dimensionId
import org.valkyrienskies.mod.common.shipObjectWorld
import org.valkyrienskies.mod.common.util.toMinecraft
import org.valkyrienskies.mod.common.vsCore

open class VSPhysicsEntity(type: EntityType<VSPhysicsEntity>, level: Level) : Mob(type, level) {
    // Physics data, persistent
    protected var physicsEntityData: PhysicsEntityData? = null
        private set

    // The physics entity, transient, only exists server side after this entity has been added to a world
    protected var physicsEntityServer: PhysicsEntityServer? = null
        private set

    fun setPhysicsEntityData(physicsEntityData: PhysicsEntityData) {
        if (this.physicsEntityData != null) {
            throw IllegalStateException("Cannot define physicsEntityData, its already defined!")
        }
        this.physicsEntityData = physicsEntityData
        if (!this.level().isClientSide) {
            this.entityData.set(SHIP_ID_DATA, physicsEntityData.shipId.toString())
        }
    }

    override fun tick() {
        if (!this.level().isClientSide) {
            val physicsEntityServerCopy = physicsEntityServer
            if (physicsEntityServerCopy != null) {
                val transform = physicsEntityServerCopy.shipTransform
                this.setPos(transform.positionInWorld.toMinecraft())
                this.physicsEntityData!!.transform = transform
            }
        }
        // 26.2: client-side position interpolation is handled by the base entity. Server position
        // updates arrive as vanilla move/teleport packets that feed Entity.getInterpolation(), and
        // super.tick() -> LivingEntity.aiStep() advances it via InterpolationHandler.interpolate().
        // The old manual lerpTo/tickLerp (Entity.lerpTo was removed in 26.2) is no longer needed.
        super.tick()
    }

    fun getRenderTransform(shipObjectClientWorld: VsiClientShipWorld): ShipTransform? {
        val shipIdString = entityData.get(SHIP_ID_DATA)
        if (shipIdString == "") {
            return null
        }
        val shipIdLong = shipIdString.toLong()
        return shipObjectClientWorld.getPhysEntityClientRenderTransform(shipIdLong)
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(SHIP_ID_DATA, "")
    }

    override fun travel(movementInput: Vec3) {
        // disable AI movement
    }

    override fun getAddEntityPacket(serverEntity: ServerEntity): Packet<ClientGamePacketListener> {
        return ClientboundAddEntityPacket(this, serverEntity)
    }

    override fun addAdditionalSaveData(output: ValueOutput) {
        val data = physicsEntityData ?: return
        // 26.2: ValueOutput has no putByteArray; store the Jackson-serialised physics blob as an
        // int[] (each byte widened to int; toByte() truncates back losslessly on read).
        val bytes = getMapper().writeValueAsBytes(data)
        output.putIntArray(PHYS_DATA_NBT_KEY, IntArray(bytes.size) { bytes[it].toInt() })
    }

    override fun readAdditionalSaveData(input: ValueInput) {
        if (!this.level().isClientSide && physicsEntityData != null) {
            throw IllegalStateException("This entity is already loaded!")
        }
        val ints = input.getIntArray(PHYS_DATA_NBT_KEY).orElse(null) ?: return
        val bytes = ByteArray(ints.size) { ints[it].toByte() }
        setPhysicsEntityData(getMapper().readValue<PhysicsEntityData>(bytes))
    }

    override fun setLevelCallback(callback: EntityInLevelCallback) {
        super.setLevelCallback(callback)
        if (!this.level().isClientSide) {
            val isNull = callback == EntityInLevelCallback.NULL
            if (!isNull) {
                // Try adding the rigid body of this entity from the world
                if (physicsEntityServer != null) {
                    throw IllegalStateException("Rigid body is already in the world!")
                }
                physicsEntityServer = (level().shipObjectWorld as VsiServerShipWorld).createPhysicsEntity(
                    physicsEntityData!!, level().dimensionId
                )
            } else {
                // Try removing the rigid body of this entity from the world
                if (physicsEntityServer == null) {
                    return
                    // throw IllegalStateException("Rigid body does not exist in the world!")
                }
                (level().shipObjectWorld as VsiServerShipWorld).deletePhysicsEntity(physicsEntityData!!.shipId)
                physicsEntityServer = null
            }
        }
    }

    override fun shouldRenderAtSqrDistance(d: Double): Boolean {
        var e = this.boundingBox.size
        if (java.lang.Double.isNaN(e)) {
            e = 1.0
        }
        e *= 1024.0
        return d < e * e
    }

    // 26.2: Entity.moveTo(x,y,z,yRot,xRot) was renamed to snapTo(x,y,z,yRot,xRot).
    override fun snapTo(d: Double, e: Double, f: Double, g: Float, h: Float) {
        super.snapTo(d, e, f, g, h)
        if (!this.level().isClientSide) {
            val physicsEntityServerCopy = physicsEntityServer
            if (physicsEntityServerCopy != null) {
                val newPos = Vector3d(d, e, f)
                val teleportData = vsCore.newShipTeleportData(newPos = newPos)
                (this.level().shipObjectWorld as VsiServerShipWorld)
                    .teleportPhysicsEntity(this.physicsEntityServer!!, teleportData)
            } else {
                physicsEntityData!!.transform = vsApi.newBodyTransform(
                    Vector3d(d, e, f),
                    physicsEntityData!!.transform.shipToWorldRotation,
                    Vector3d(1.0),
                    Vector3d()
                ) as ShipTransform
            }
        }
    }

    companion object {
        private const val PHYS_DATA_NBT_KEY = "phys_entity_data"

        // Use string because there is no LONG serializer by default SMH my head!
        val SHIP_ID_DATA: EntityDataAccessor<String> =
            SynchedEntityData.defineId(VSPhysicsEntity::class.java, EntityDataSerializers.STRING)

        private fun getMapper(): ObjectMapper {
            return vsCore.defaultMapper
        }

        // Without these attributes LivingEntity complains
        fun createAttributes(): AttributeSupplier.Builder {
            return createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
        }

        fun createBasicSphereData(
            shipId: ShipId, transform: ShipTransform, radius: Double = 0.5, mass: Double = 10000.0
        ): PhysicsEntityData {
            val inertia = 0.4 * mass * radius * radius
            val inertiaData: ShipInertiaData = vsCore.newShipInertiaData(Vector3d(), mass, Matrix3d().scale(inertia))
            val collisionShapeData = VSSphereCollisionShapeData(radius)
            return PhysicsEntityData(
                shipId = shipId,
                transform = transform,
                inertiaData = inertiaData,
                linearVelocity = Vector3d(),
                angularVelocity = Vector3d(),
                collisionShapeData = collisionShapeData,
                isStatic = false
            )
        }
    }
}
