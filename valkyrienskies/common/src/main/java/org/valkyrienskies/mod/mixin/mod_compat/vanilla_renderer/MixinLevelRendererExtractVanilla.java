package org.valkyrienskies.mod.mixin.mod_compat.vanilla_renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.mixin.accessors.client.render.LevelRendererAccessor;
import org.valkyrienskies.mod.mixinducks.client.render.IVSViewAreaMethods;
import org.valkyrienskies.mod.mixinducks.client.render.LevelRendererVanillaDuck;

/**
 * Rebuilds ship render sections when their blocks change.
 *
 * <p>Ship sections don't live in vanilla's {@link ViewArea} grid — they're kept in a separate
 * shipyard-space keyed map on {@link IVSViewAreaMethods}, because shipyard coordinates are millions
 * of blocks away from the camera and would fall outside the view grid entirely. So when a block on a
 * ship changes, vanilla's {@code setSectionDirty} resolves nothing and the stale mesh is rendered
 * forever (placing or breaking a block on an assembled ship had no visible effect).
 *
 * <p>26.2 removed {@code RenderSection.setDirty}; the equivalent is {@link
 * SectionRenderDispatcher.RenderSection#reset()}, which cancels pending compile tasks and swaps the
 * mesh back to {@code CompiledSectionMesh.UNCOMPILED}. That's all we need to do here: {@code
 * MixinLevelRendererVanilla} already recompiles any ship section whose mesh has no renderable
 * layers, so resetting hands it straight back to the existing compile path.
 */
@Mixin(LevelRenderer.class)
public abstract class MixinLevelRendererExtractVanilla {

    /**
     * Makes sure ship render sections are in {@code visibleSections} before block entities are
     * collected from them.
     *
     * <p>26.2 splits a frame into extract() then render(). Ship sections are appended to
     * {@code visibleSections} from {@code prepareChunkRenders}, which is in the render() half - but
     * {@code extractVisibleBlockEntities} walks {@code visibleSections} in the extract() half, one
     * phase earlier. Vanilla rebuilds {@code visibleSections} often enough that ship sections
     * appended during render() had usually been dropped again by the next extract(); measurements
     * showed them surviving on roughly one frame in four. Chunk geometry still drew (that reads
     * visibleSections during render(), after we re-append), but ship block entities were skipped on
     * most frames - which is why a chest or a helm's wheel vanished as soon as its ship assembled.
     */
    @Inject(
        method = "extractVisibleBlockEntities(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/renderer/culling/Frustum;)V",
        at = @At("HEAD")
    )
    private void vs$addShipSectionsBeforeExtract(final Camera camera, final float partialTick,
        final LevelRenderState state, final Frustum frustum, final CallbackInfo ci) {
        final LevelRenderer levelRenderer = Minecraft.getInstance().levelRenderer;
        if (levelRenderer instanceof final LevelRendererVanillaDuck duck) {
            duck.vs$ensureShipSectionsVisible();
        }
    }

    /**
     * Keeps ship sections above the block entity fade-in cutoff.
     *
     * <p>{@code extractVisibleBlockEntities} skips a whole section's block entities when
     * {@code renderSection.getVisibility(millis) < 0.3f} - that's the fade-in used to hide sections
     * that were just compiled. Ship sections are created on demand and reset whenever a ship block
     * changes, so their fade timer keeps restarting and the visibility never climbs past the cutoff;
     * their block entities were dropped on every single frame.
     *
     * <p>The ship's chunk geometry didn't suffer from this because {@code MixinLevelRendererVanilla}
     * already substitutes {@code visibility = 1.0f} in the per-section shader uniform - but that
     * uniform is written during render(), while this cutoff is read during extract(). Force the same
     * "always fully faded in" answer here so both halves of the frame agree.
     */
    @WrapOperation(
        method = "extractVisibleBlockEntities(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/renderer/culling/Frustum;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection;getVisibility(J)F"
        )
    )
    private float vs$shipSectionsAreFullyVisible(final SectionRenderDispatcher.RenderSection instance,
        final long millis, final Operation<Float> original) {
        final ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            final BlockPos origin = instance.getRenderOrigin();
            if (VSGameUtilsKt.isBlockInShipyard(level, origin.getX(), origin.getY(), origin.getZ())) {
                return 1.0f;
            }
        }
        return original.call(instance, millis);
    }

    @Inject(method = "setSectionDirty(III)V", at = @At("HEAD"))
    private void vs$resetShipSection(final int sectionX, final int sectionY, final int sectionZ,
        final CallbackInfo ci) {
        final Minecraft minecraft = Minecraft.getInstance();
        final ClientLevel level = minecraft.level;
        if (level == null || !VSGameUtilsKt.isChunkInShipyard(level, sectionX, sectionZ)) {
            return;
        }
        final LevelRenderer levelRenderer = minecraft.levelRenderer;
        if (levelRenderer == null) {
            return;
        }
        final ViewArea viewArea = ((LevelRendererAccessor) levelRenderer).getViewArea();
        if (viewArea == null) {
            return;
        }
        final SectionRenderDispatcher.RenderSection section =
            ((IVSViewAreaMethods) viewArea).vs$getShipRenderSection(sectionX, sectionY, sectionZ);
        if (section != null) {
            section.reset();
        }
    }

    /**
     * Lets a ship block's break progress survive the extract-side distance cull, so its crack
     * overlay is actually generated.
     *
     * <p>{@code extractBlockDestroyAnimation} drops any break whose block is more than 32 blocks
     * ({@code distToCenterSqr > 1024}) from the camera before it ever builds a
     * {@code BlockBreakingRenderState}. A ship block's {@link BlockPos} is its raw shipyard
     * coordinate - millions of blocks from the camera - so every ship block break was culled here
     * and no crack was ever created (the {@code translate} fix in {@code MixinLevelRendererVanilla}
     * never even got a state to draw). Substitute the distance to the block's ship-transformed world
     * position, which while mining is right in front of the camera.
     */
    @WrapOperation(
        method = "extractBlockDestroyAnimation(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/BlockPos;distToCenterSqr(DDD)D"
        )
    )
    private double vs$shipBlockBreakingDistance(final BlockPos pos, final double x, final double y,
        final double z, final Operation<Double> original) {
        final ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            final ClientShip ship = VSGameUtilsKt.getShipObjectManagingPos(level, pos);
            if (ship != null) {
                final Vector3d world = ship.getRenderTransform().getShipToWorld()
                    .transformPosition(new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                final double dx = world.x - x;
                final double dy = world.y - y;
                final double dz = world.z - z;
                return dx * dx + dy * dy + dz * dz;
            }
        }
        return original.call(pos, x, y, z);
    }

    /**
     * Captures the view frustum vanilla uses to cull its own render sections, so ship sections can be
     * culled with the same frustum when {@code MixinLevelRendererVanilla} appends them. Stored on the
     * level renderer (a different object than this extractor) via its duck interface.
     */
    @Inject(method = "applyFrustum(Lnet/minecraft/client/renderer/culling/Frustum;)V", at = @At("HEAD"))
    private void vs$captureShipCullingFrustum(final Frustum frustum, final CallbackInfo ci) {
        final LevelRenderer levelRenderer = Minecraft.getInstance().levelRenderer;
        if (levelRenderer instanceof final LevelRendererVanillaDuck duck) {
            duck.vs$setShipCullingFrustum(frustum);
        }
    }

}
