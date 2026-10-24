package org.valkyrienskies.mod.common.item

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import org.joml.Vector3d
import org.joml.Vector3dc
import org.valkyrienskies.mod.common.ValkyrienSkiesMod
import org.valkyrienskies.mod.common.dimensionId
import org.valkyrienskies.mod.common.entity.VSPhysicsEntity
import org.valkyrienskies.mod.common.getShipManagingPos
import org.valkyrienskies.mod.common.shipObjectWorld
import org.valkyrienskies.mod.common.util.toJOML
import org.valkyrienskies.mod.common.util.toJOMLD
import org.valkyrienskies.mod.common.vsCore

class PhysicsEntityCreatorItem(
    properties: Properties
) : Item(properties) {
    override fun isFoil(stack: ItemStack): Boolean {
        return true
    }

    override fun useOn(ctx: UseOnContext): InteractionResult {
        val level = ctx.level as? ServerLevel ?: return super.useOn(ctx)

        if (!level.isClientSide) {
            // 26.2: EntityType.create(Level) now requires an EntitySpawnReason; the result is the
            // declared out-Entity type, so cast to the concrete physics entity.
            val entity = ValkyrienSkiesMod.PHYSICS_ENTITY_TYPE.create(level, EntitySpawnReason.MOB_SUMMONED)
                as? VSPhysicsEntity ?: return super.useOn(ctx)
            val shipId = level.shipObjectWorld.allocateShipId(level.dimensionId)
            val sphereRadius = 0.5
            val offsetInLocal: Vector3dc = ctx.clickedFace.normal.toJOMLD().mul(sphereRadius)

            val shipOn = ctx.level.getShipManagingPos(ctx.clickedPos)
            val offsetInGlobal = if (shipOn != null) {
                shipOn.transform.shipToWorldRotation.transform(offsetInLocal, Vector3d())
            } else {
                offsetInLocal
            }

            val entityPos: Vector3dc = if (shipOn != null) {
                // The clicked block lives in shipyard space, so transform its (shipyard) center into
                // world space. NOTE: do NOT feed ctx.clickLocation here - in 26.2 that Vec3 is
                // already in WORLD space, so running it through shipToWorld applied the shipyard
                // offset a second time and flung the entity millions of blocks away (it spawned
                // fine, just nowhere near the player - which looked like "can't spawn on a ship").
                val clickedCenterInShip = Vector3d(
                    ctx.clickedPos.x + 0.5, ctx.clickedPos.y + 0.5, ctx.clickedPos.z + 0.5
                )
                shipOn.transform.shipToWorld.transformPosition(clickedCenterInShip).add(offsetInGlobal)
            } else {
                ctx.clickLocation.toJOML().add(offsetInGlobal)
            }

            val transform = vsCore.newShipTransform(entityPos, Vector3d())
            // Make it weight only 500 so that it floats
            val physicsEntityData = VSPhysicsEntity.createBasicSphereData(shipId, transform, sphereRadius, mass = 500.0)
            entity.setPhysicsEntityData(physicsEntityData)
            entity.setPos(entityPos.x(), entityPos.y(), entityPos.z())
            level.addFreshEntity(entity)

            // Example of adding a constraint to a physics entity
            if (shipOn != null) {
                val attachCompliance = 1e-8
                val attachMaxForce = 1e10
                // Attach the click position of the ship to the surface of the physics entity
                // val attachConstraint = VSAttachmentConstraint(
                //     shipOn.id, physicsEntityData.shipId, attachCompliance, ctx.clickLocation.toJOML(), offsetInGlobal.mul(-1.0, Vector3d()),
                //     attachMaxForce, 0.0
                // )
                // val posDampingConstraint = VSPosDampingConstraint(
                //     shipOn.id, physicsEntityData.shipId, attachCompliance, ctx.clickLocation.toJOML(), offsetInGlobal.mul(-1.0, Vector3d()),
                //     attachMaxForce, 0.1
                // )
                // val rotDampingConstraint = VSRotDampingConstraint(
                //     shipOn.id, physicsEntityData.shipId, attachCompliance, shipOn.transform.shipToWorldRotation.invert(
                //         Quaterniond()
                //     ),
                //     Quaterniond(), 1e10, 0.1, VSRotDampingAxes.ALL_AXES
                // )
                // level.shipObjectWorld.createNewConstraint(attachConstraint)
                // level.shipObjectWorld.createNewConstraint(posDampingConstraint)
                // level.shipObjectWorld.createNewConstraint(rotDampingConstraint)
            }
        }

        return super.useOn(ctx)
    }
}
