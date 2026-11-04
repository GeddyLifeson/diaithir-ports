/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.renderer;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Vector3f;
import li.cil.oc2.api.API;
import li.cil.oc2.common.blockentity.NetworkConnectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import javax.annotation.Nullable;
import java.util.*;

// Note on cable rendering and Fabulous rendering mode: sadly at the time of writing,
// there is no hook to render before the Fabulous shaders. And those sadly appear to
// completely flatten / break the depth buffer. So using the RenderWorldLastEvent will
// not work anymore for rendering cables in one nice efficient batch. So instead we
// fall back to letting the TESRs trigger the cable rendering. We still use the data
// structures with precomputed data and such, it's just that they need much larger
// render bounds and require an addition hash map look-up.
// TODO(oc2-restore): The actual cable rendering (RenderLevelStageEvent based, using
// ModRenderType.getNetworkCable() and a PoseStack/VertexConsumer pipeline) relied on
// the old GlStateManager/RenderType/LightTexture APIs and has been removed for 26.1.2.
// The connection tracking and geometry helpers are kept so it can be restored.
@EventBusSubscriber(value = Dist.CLIENT, modid = API.MOD_ID)
public final class NetworkCableRenderer {
    private static final int MAX_RENDER_DISTANCE = 100;
    private static final int CABLE_VERTEX_COUNT = 9;
    private static final float CABLE_THICKNESS = 0.025f;
    private static final float CABLE_LENGTH_FOR_MAX_SWING = 6f;
    private static final float CABLE_MAX_SWING_AMOUNT = 0.05f;
    private static final int CABLE_SWING_INTERVAL = 8000;
    private static final float CABLE_HANG_MIN = 0.1f;
    private static final float CABLE_HANG_MAX = 0.5f;
    private static final float CABLE_MAX_LENGTH = 8f;
    private static final Vector3f CABLE_COLOR = new Vector3f(0.0f, 0.33f, 0.4f);

    private static final Set<NetworkConnectorBlockEntity> connectors = Collections.newSetFromMap(new WeakHashMap<>());
    private static int lastKnownConnectorCount;
    private static boolean isDirty;

    private static final ArrayList<Connection> connections = new ArrayList<>();
    private static final WeakHashMap<NetworkConnectorBlockEntity, ArrayList<Connection>> connectionsByConnector = new WeakHashMap<>();

    ///////////////////////////////////////////////////////////////////

    public static void addNetworkConnector(final NetworkConnectorBlockEntity connector) {
        connectors.add(connector);
        invalidateConnections();
    }

    public static void invalidateConnections() {
        isDirty = true;
    }

    ///////////////////////////////////////////////////////////////////

    @SubscribeEvent
    public static void handleChunkUnloadEvent(final ChunkEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            final ChunkPos chunkPos = event.getChunk().getPos();

            final ArrayList<NetworkConnectorBlockEntity> list = new ArrayList<>(NetworkCableRenderer.connectors);
            for (final NetworkConnectorBlockEntity connector : list) {
                final ChunkPos connectorChunkPos = ChunkPos.containing(connector.getBlockPos());
                if (Objects.equals(connectorChunkPos, chunkPos)) {
                    connectors.remove(connector);
                }
            }

            invalidateConnections();
        }
    }

    @SubscribeEvent
    public static void handleWorldUnloadEvent(final LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            final LevelAccessor level = event.getLevel();

            final ArrayList<NetworkConnectorBlockEntity> list = new ArrayList<>(NetworkCableRenderer.connectors);
            for (final NetworkConnectorBlockEntity connector : list) {
                if (connector.getLevel() == level) {
                    connectors.remove(connector);
                }
            }

            invalidateConnections();
        }
    }

    // TODO(oc2-restore): render cables, see class comment.

    private static Vec3 lerp(final Vec3 a, final Vec3 b, final float t) {
        return a.add(b.subtract(a).scale(t)); // a + (b - a)*t = a*(1-t) + b*t
    }

    private static Vec3 quadraticBezier(final Vec3 a, final Vec3 b, final Vec3 c, final float t) {
        final Vec3 a1 = lerp(a, c, t);
        final Vec3 b1 = lerp(c, b, t);
        return lerp(a1, b1, t);
    }

    private static Vec3 getExtrusionVector(final Vec3 eye, final Vec3 v, final Vec3 forward) {
        return forward.cross(eye.subtract(v)).normalize().scale(CABLE_THICKNESS);
    }

    private static float computeCableHang(final Vec3 a, final Vec3 b) {
        final double length = a.distanceTo(b);
        final double hangFactor = Mth.clamp(length / CABLE_MAX_LENGTH, 0, 1);
        return (float) (CABLE_HANG_MIN + (CABLE_HANG_MAX - CABLE_HANG_MIN) * hangFactor);
    }

    private static float computeCableSwingAmount(final Vec3 p0, final Vec3 p1) {
        return Mth.clamp((float) p0.distanceTo(p1) / CABLE_LENGTH_FOR_MAX_SWING, 0.1f, 1f) * CABLE_MAX_SWING_AMOUNT;
    }

    private static Vec3 animateCableSwing(final Vec3 c, @Nullable final Vec3 right, final float swingAmount, final int seed) {
        final float relTime = ((System.currentTimeMillis() + seed) % CABLE_SWING_INTERVAL) / (float) CABLE_SWING_INTERVAL;
        final float relRadialTime = relTime * 2 * (float) Math.PI;

        if (right == null) {
            return c.add(swingAmount * Mth.sin(relRadialTime),
                0,
                swingAmount * Mth.cos(relRadialTime));
        } else {
            return c.add(swingAmount * Mth.cos(relRadialTime) * right.x,
                0.5f * swingAmount * Mth.sin(relRadialTime * 2 - (float) Math.PI) - swingAmount,
                swingAmount * Mth.cos(relRadialTime) * right.z);
        }
    }

    private static void validateConnectors() {
        final ArrayList<NetworkConnectorBlockEntity> list = new ArrayList<>(connectors);
        for (final NetworkConnectorBlockEntity connector : list) {
            if (!connector.isValid()) {
                connectors.remove(connector);
                connectionsByConnector.remove(connector);
                invalidateConnections();
            }
        }

        // We track the size because the WeakHasMap may expunge dead entries without
        // us knowing otherwise.
        if (list.size() != lastKnownConnectorCount) {
            invalidateConnections();
        }
        lastKnownConnectorCount = list.size();
    }

    private static void validatePairs() {
        if (!isDirty) {
            return;
        }

        isDirty = false;
        connections.clear();
        connectionsByConnector.clear();

        final HashSet<Connection> seen = new HashSet<>();
        for (final NetworkConnectorBlockEntity connector : connectors) {
            final BlockPos position = connector.getBlockPos();
            for (final BlockPos connectedPosition : connector.getConnectedPositions()) {
                final Connection connection = new Connection(position, connectedPosition);
                if (seen.add(connection)) {
                    connections.add(connection);
                    connectionsByConnector.computeIfAbsent(connector, unused -> new ArrayList<>()).add(connection);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////

    private static final class Connection {
        private static final Vec3 POS_Y = new Vec3(0, 1, 0);

        public final BlockPos fromPos, toPos;
        public final Vec3 from, to, forward, right;
        public final AABB bounds;

        private Connection(final BlockPos fromPos, final BlockPos toPos) {
            if (fromPos.compareTo(toPos) > 0) {
                this.fromPos = toPos;
                this.toPos = fromPos;
            } else {
                this.fromPos = fromPos;
                this.toPos = toPos;
            }

            from = Vec3.atCenterOf(fromPos);
            to = Vec3.atCenterOf(toPos);
            forward = to.subtract(from).normalize();
            right = fromPos.getX() == toPos.getX() && fromPos.getZ() == toPos.getZ()
                ? null : forward.cross(POS_Y);
            bounds = new AABB(from, to).inflate(0, CABLE_HANG_MAX, 0);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Connection that = (Connection) o;
            return fromPos.equals(that.fromPos) && toPos.equals(that.toPos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fromPos, toPos);
        }
    }
}
