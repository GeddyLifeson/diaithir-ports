package org.valkyrienskies.mod.mixinducks.client.render;

import net.minecraft.client.renderer.culling.Frustum;

public interface LevelRendererVanillaDuck {

    void vs$addShipVisibleChunks(final Frustum frustum);

    /**
     * Appends this frame's ship render sections to {@code visibleSections}. Must be called before
     * anything that reads {@code visibleSections} - both the chunk draw batching in render() and the
     * block entity collection in extract().
     */
    void vs$ensureShipSectionsVisible();

    /**
     * Stores the current view frustum so ship sections can be frustum-culled when they are appended
     * to {@code visibleSections}. Set from {@code LevelExtractor.applyFrustum}, which is where
     * vanilla culls its own sections. May be null on the very first frame (then no ship culling).
     */
    void vs$setShipCullingFrustum(final Frustum frustum);

}
