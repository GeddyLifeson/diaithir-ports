package net.diaithir.ships.cargo

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.block.MovingBlockRenderState
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4d
import org.joml.Quaterniond
import org.joml.Quaternionf
import org.joml.Vector3d
import org.valkyrienskies.mod.common.shipObjectWorld
import java.util.concurrent.ConcurrentHashMap

/** Client side of the ship cargo: what each carrier ship holds (as block states to draw) and the drawing of it in the ship's render transform. */
object ClientCargo {
    class Draw(val state: BlockState, val x: Int, val y: Int, val z: Int, val sx: Int, val sy: Int, val sz: Int)
    class Manifest(val ox: Int, val oy: Int, val oz: Int, val w: Int, val h: Int, val l: Int, val loads: List<Draw>)

    val ships = ConcurrentHashMap<Long, Manifest>()

    fun isCarrier(shipId: Long) = ships.containsKey(shipId)

    fun clear() = ships.clear()

    /** Called from the level renderer after the block entities are submitted; the pose stack is camera relative. */
    @JvmStatic
    fun submit(pose: PoseStack, cam: Vec3, collector: SubmitNodeCollector) {
        if (ships.isEmpty()) return
        val level = Minecraft.getInstance().level ?: return
        val world = level.shipObjectWorld
        val mbs = MovingBlockRenderState()
        mbs.lightEngine = level.lightEngine
        mbs.cardinalLighting = level.cardinalLighting()
        val tmp = Vector3d()
        for ((id, m) in ships) {
            if (m.loads.isEmpty()) continue
            val ship = world.loadedShips.getById(id) ?: continue
            val s2w = Matrix4d(ship.renderTransform.shipToWorld)
            val sc = s2w.getScale(Vector3d())
            val q = s2w.getNormalizedRotation(Quaterniond())
            val rot = Quaternionf(q.x.toFloat(), q.y.toFloat(), q.z.toFloat(), q.w.toFloat())
            for (d in m.loads) {
                s2w.transformPosition(tmp.set((m.ox + d.x).toDouble(), (m.oy + d.y).toDouble(), (m.oz + d.z).toDouble()))
                pose.pushPose()
                pose.translate(tmp.x - cam.x, tmp.y - cam.y, tmp.z - cam.z)
                pose.mulPose(rot)
                pose.scale(sc.x.toFloat() * d.sx, sc.y.toFloat() * d.sy, sc.z.toFloat() * d.sz)
                mbs.blockState = d.state
                mbs.blockPos = BlockPos.containing(tmp.x + d.sx / 2.0, tmp.y + d.sy / 2.0, tmp.z + d.sz / 2.0)
                mbs.randomSeedPos = mbs.blockPos
                collector.submitMovingBlock(pose, mbs)
                pose.popPose()
            }
        }
    }
}
