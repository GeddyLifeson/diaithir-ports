package org.valkyrienskies.mod.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

/**
 * Shared helper for correcting translucent-geometry resorting on ship render sections.
 *
 * <p>Vanilla sorts a section's translucent faces - and decides whether a resort is even needed -
 * from the camera position expressed relative to the section's block origin (see
 * {@code RenderSection.createVertexSorting}, {@code TranslucencyPointOfView}). Ship render sections
 * store their vertices in shipyard space and are keyed by a shipyard section node millions of
 * blocks from the world camera, so feeding the raw world camera makes every such computation
 * degenerate: the sort order comes out wrong, and the point-of-view gate (both at scheduling time
 * in {@code LevelRenderer.scheduleResort} and in {@code ResortTransparencyTask.doTask}) never
 * changes so a resort is never triggered.
 *
 * <p>This converts the world camera into the owning ship's shipyard space, which is the same space
 * as the shipyard section origin, so the vanilla "camera - sectionOrigin" math becomes correct.
 *
 * <p>Lives in this (non-mixin) package on purpose: classes under {@code org.valkyrienskies.mod.mixin}
 * are owned by the mixin config and cannot be referenced directly from transformed code.
 */
public final class ShipTranslucencyHelper {

    private ShipTranslucencyHelper() {
    }

    /**
     * The world camera position transformed into the shipyard space of the ship owning the given
     * shipyard section node, or {@code null} when that node is not a ship section (callers then keep
     * the vanilla world camera unchanged).
     *
     * <p>Safe to call off the render thread ({@code doTask} runs on chunk-build workers): it only
     * reads the ship object world and the ship's render transform, the same reads VS2 already
     * performs during ship section compilation.
     */
    public static Vec3 shipSpaceCamera(final long sectionNode, final Vec3 worldCamera) {
        final ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        final BlockPos origin = new BlockPos(
            SectionPos.sectionToBlockCoord(SectionPos.x(sectionNode)),
            SectionPos.sectionToBlockCoord(SectionPos.y(sectionNode)),
            SectionPos.sectionToBlockCoord(SectionPos.z(sectionNode)));
        final ClientShip ship = VSGameUtilsKt.getShipObjectManagingPos(level, origin);
        if (ship == null) {
            return null;
        }
        final Vector3d shipCamera = ship.getRenderTransform().getWorldToShip()
            .transformPosition(new Vector3d(worldCamera.x, worldCamera.y, worldCamera.z));
        return new Vec3(shipCamera.x, shipCamera.y, shipCamera.z);
    }
}
