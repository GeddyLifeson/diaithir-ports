package org.valkyrienskies.mod.mixin.mod_compat.vanilla_renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.chunk.TranslucencyPointOfView;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.assembly.SeamlessChunksManager;
import org.valkyrienskies.mod.common.VSClientGameUtils;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.config.ShipRenderer;
import org.valkyrienskies.mod.common.config.ShipRendererKt;
import org.valkyrienskies.mod.mixinducks.client.render.IVSViewAreaMethods;
import org.valkyrienskies.mod.mixinducks.client.render.LevelRendererVanillaDuck;
import org.valkyrienskies.mod.mixinducks.mod_compat.vanilla_renderer.LevelRendererDuck;
import org.valkyrienskies.mod.util.ShipTranslucencyHelper;

/**
 * Renders ship chunks through the vanilla 26.2 GPU-batched section pipeline.
 *
 * <p>26.2 deleted everything the old implementation was built on (renderLevel, renderSectionLayer,
 * ShaderInstance/CHUNK_OFFSET uniforms, VertexBuffer.bind/draw). The new pipeline batches ALL
 * visible sections into one {@link ChunkSectionsToRender} via {@code prepareChunkRenders}, where
 * each section gets its own {@code DynamicUniforms.ChunkSectionInfo(mat4 modelView, ivec3 origin,
 * ...)} std140 entry, and the terrain vertex shader computes
 * {@code pos = Position + (ChunkPosition - CameraBlockPos) + CameraOffset;
 * gl_Position = ProjMat * ModelViewMat * pos}.
 *
 * <p>Because the matrix is PER-SECTION, ship sections can ride the same batch as vanilla terrain:
 * <ol>
 *   <li>{@code prepareChunkRenders} HEAD: for every loaded ship, get/create its shipyard-space
 *       {@link SectionRenderDispatcher.RenderSection}s (via {@link IVSViewAreaMethods}), lazily
 *       compile them ({@link RenderRegionCache#createRegion} + {@code compileSync} — worldgen is
 *       skipped for shipyard chunks so this is cheap), and append them to {@code visibleSections}.
 *   <li>Wrap the {@code ChunkSectionInfo} constructor: for ship sections substitute
 *       {@code ModelViewMat = viewRot * T(-camPos) * shipToWorld * T(sectionOrigin) * T(-camOffset)}
 *       (double precision, cast to float at the end) and {@code ChunkPosition = CameraBlockPos}
 *       (which zeroes the shader's {@code ChunkPosition - CameraBlockPos} term, leaving
 *       {@code + CameraOffset} — cancelled by the trailing {@code T(-camOffset)}).
 * </ol>
 *
 * <p>Block updates on ships are rebuilt via MixinLevelExtractorVanilla, which resets the ship
 * RenderSection (26.2's replacement for the removed RenderSection.setDirty) so the compile path
 * below picks it up again on the next frame.
 *
 * <p>TODO Phase 2b polish: frustum-cull ship AABBs, translucent resorting, VSGameEvents ship render
 * events, block-entities/block-damage on ships.
 */
@Mixin(value = LevelRenderer.class, priority = 999)
public abstract class MixinLevelRendererVanilla implements LevelRendererDuck, LevelRendererVanillaDuck {

    // 26.2: LevelRenderer no longer holds a ClientLevel field (level data moved into the
    // extract-side LevelExtractor/LevelRenderState); read the live level off Minecraft instead.
    @Shadow
    @Final
    private ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections;

    @Shadow
    private @Nullable ViewArea viewArea;

    // Ship sections appended for the current frame -> the ship they belong to. RenderSection does
    // not override equals/hashCode, so a plain HashMap gives identity semantics.
    @Unique
    private final Map<SectionRenderDispatcher.RenderSection, ClientShip> vs$shipSections = new HashMap<>();

    // Set from LevelExtractor.applyFrustum via the duck each frame; used to frustum-cull ship
    // sections before appending them. Null until the first applyFrustum of the session.
    @Unique
    private Frustum vs$shipCullingFrustum;


    @Override
    public void vs$setShipCullingFrustum(final Frustum frustum) {
        this.vs$shipCullingFrustum = frustum;
    }

    /**
     * Feed {@code scheduleResort}'s translucency point-of-view test the ship-space camera for ship
     * sections. This is the gate that decides whether a translucent resort is even scheduled; with
     * the raw world camera vs a distant shipyard section it never changes, so ship translucent
     * geometry would never be resorted. See {@link ShipTranslucencyHelper}.
     */
    @WrapOperation(
        method = "scheduleResort",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/chunk/TranslucencyPointOfView;"
                + "set(Lnet/minecraft/world/phys/Vec3;J)"
                + "Lnet/minecraft/client/renderer/chunk/TranslucencyPointOfView;")
    )
    private TranslucencyPointOfView vs$shipScheduleResortPov(final TranslucencyPointOfView pov,
        final Vec3 camera, final long node, final Operation<TranslucencyPointOfView> original) {
        final Vec3 shipCamera = ShipTranslucencyHelper.shipSpaceCamera(node, camera);
        return original.call(pov, shipCamera != null ? shipCamera : camera, node);
    }

    @Override
    public void vs$setNeedsFrustumUpdate() {
        // Ship sections are re-discovered every frame in vs$addShipSections; nothing to invalidate.
    }

    @Override
    public void vs$addShipVisibleChunks(final Frustum frustum) {
        // Only called by the immersive-portals compat mixin (excluded from the 26.2 port).
        // Ship section discovery happens unconditionally in vs$addShipSections.
    }

    @Inject(method = "prepareChunkRenders", at = @At("HEAD"))
    private void vs$addShipSections(final Matrix4fc baseModelView,
        final CallbackInfoReturnable<ChunkSectionsToRender> cir) {
        vs$ensureShipSectionsVisible();
    }

    /**
     * Discovers ship render sections and appends them to {@code visibleSections}.
     *
     * <p>This has to run both here (before {@code prepareChunkRenders} builds the draw batches) and
     * from {@code LevelExtractor.extractVisibleBlockEntities}. 26.2 splits rendering into extract()
     * then render(), and {@code visibleSections} is rebuilt often enough that ship sections appended
     * during render() are usually gone again by the next frame's extract() - measured at roughly one
     * frame in four surviving. Block entities are collected during extract(), so on the other three
     * frames ship block entities were silently skipped and never rendered at all.
     */
    @Override
    public void vs$ensureShipSectionsVisible() {
        // Flush seamless-assembly deferred chunk packets once per frame on the render thread
        // (this hook replaced the old setupRender HEAD injection, which no longer exists).
        final SeamlessChunksManager manager = SeamlessChunksManager.get();
        if (manager != null) {
            manager.drainDeferredBatch();
        }

        // Drop last frame's ship sections: visibleSections is only rebuilt when the occlusion
        // graph updates, so our appended entries would otherwise duplicate frame over frame.
        if (!vs$shipSections.isEmpty()) {
            visibleSections.removeIf(vs$shipSections::containsKey);
            vs$shipSections.clear();
        }

        final ClientLevel level = Minecraft.getInstance().level;
        if (level == null || viewArea == null) {
            return;
        }
        final IVSViewAreaMethods shipViewArea = (IVSViewAreaMethods) viewArea;
        final int minSectionY = level.getMinSectionY();
        final int maxSectionY = level.getMaxSectionY();

        // Collect this frame's ship sections, then append them to visibleSections sorted near->far by
        // camera distance. 26.2 draws the TRANSLUCENT layer per-section (it deliberately does NOT
        // batch translucent draws by texture like the opaque layers), and the draw order follows
        // visibleSections order. Ship sections are keyed by shipyard coordinates, so appending them in
        // raw chunk/sectionY order gave a fixed, camera-independent draw order: one ship glass
        // structure would always occlude another behind it regardless of viewing angle. Sorting by
        // real world distance - the same near->far order vanilla builds visibleSections in - makes
        // ship translucent geometry draw back-to-front like terrain.
        // Camera lookup must never abort this method: it also runs from the extract half of the frame
        // (LevelExtractor.extractVisibleBlockEntities), and if it throws there, no ship section makes
        // it into visibleSections and every ship BLOCK ENTITY silently disappears - chests vanished
        // while plain ship blocks kept rendering, because the render half re-adds the sections.
        Vec3 cameraPos = null;
        try {
            cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        } catch (final Throwable ignored) {
            // No camera this phase - fall through and skip the distance sort below.
        }
        final List<SectionRenderDispatcher.RenderSection> collected = new ArrayList<>();

        for (final ClientShip ship : VSGameUtilsKt.getShipObjectWorld(level).getLoadedShips()) {
            if (ShipRendererKt.getShipRenderer(ship) != ShipRenderer.VANILLA) {
                continue;
            }
            ship.getActiveChunksSet().forEach((x, z) -> {
                final LevelChunk chunk = level.getChunk(x, z);
                for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                    final LevelChunkSection chunkSection =
                        chunk.getSection(level.getSectionIndexFromSectionY(sectionY));
                    if (chunkSection.hasOnlyAir()) {
                        continue;
                    }
                    final SectionRenderDispatcher.RenderSection renderSection =
                        shipViewArea.vs$getOrCreateShipRenderSection(x, sectionY, z);
                    if (renderSection == null) {
                        continue;
                    }
                    if (!renderSection.getSectionMesh().hasRenderableLayers()) {
                        // Not compiled yet (or previous compile produced nothing) - compile now.
                        // Shipyard chunk content is client-loaded (MixinClientChunkCache), and all
                        // worldgen stages are skipped for shipyard chunks, so this is cheap.
                        try {
                            renderSection.compileSync(new RenderRegionCache());
                        } catch (final Throwable ignored) {
                            // Chunk data not available client-side yet; retry next frame.
                        }
                    }
                    if (!renderSection.getSectionMesh().hasRenderableLayers()) {
                        continue; // Still nothing uploaded to draw this frame.
                    }
                    // Frustum-cull: an off-screen or behind-camera ship section is otherwise still
                    // appended and drawn. Skip it when its ship-transformed world AABB misses the view.
                    if (vs$shipCullingFrustum != null && !vs$isShipSectionVisible(ship, x, sectionY, z)) {
                        continue;
                    }
                    vs$shipSections.put(renderSection, ship);
                    collected.add(renderSection);
                }
            });
        }

        // Quantise distance into buckets and rely on the STABLE sort keeping same-bucket sections in
        // their (frame-stable) discovery order. Sorting by raw continuous distance made sections at
        // near-equal depth swap order on sub-block camera jitter, which flickered the translucent
        // walls of a box straddling a section boundary. Vanilla avoids this by ordering sections in
        // discrete BFS steps; bucketing reproduces that discreteness while still drawing clearly
        // different depths back-to-front (which is what fixes cross-structure occlusion).
        if (cameraPos != null) {
            final Vec3 sortFrom = cameraPos;
            collected.sort(Comparator.comparingDouble(
                section -> Math.floor(
                    Math.sqrt(vs$shipSectionCameraDistSq(section, sortFrom)) / VS$TRANSLUCENT_SORT_BUCKET)));
        }
        visibleSections.addAll(collected);
    }

    // Distance bucket (blocks) for ship-section translucent ordering. Sections whose camera distance
    // falls in the same bucket keep a stable order; only a >1-bucket difference reorders them.
    @Unique
    private static final double VS$TRANSLUCENT_SORT_BUCKET = 6.0;

    /**
     * World-space squared distance from the camera to a ship render section's ship-transformed
     * center, used to order ship sections near-&gt;far in {@code visibleSections} for correct
     * translucent draw order.
     */
    @Unique
    private double vs$shipSectionCameraDistSq(final SectionRenderDispatcher.RenderSection section,
        final Vec3 cameraPos) {
        final ClientShip ship = vs$shipSections.get(section);
        if (ship == null) {
            return Double.MAX_VALUE;
        }
        final long node = section.getSectionNode();
        final org.joml.Vector3d center = ship.getRenderTransform().getShipToWorld().transformPosition(
            new org.joml.Vector3d(
                SectionPos.sectionToBlockCoord(SectionPos.x(node)) + 8.0,
                SectionPos.sectionToBlockCoord(SectionPos.y(node)) + 8.0,
                SectionPos.sectionToBlockCoord(SectionPos.z(node)) + 8.0));
        final double dx = center.x - cameraPos.x;
        final double dy = center.y - cameraPos.y;
        final double dz = center.z - cameraPos.z;
        return dx * dx + dy * dy + dz * dz;
    }

    @WrapOperation(
        method = "prepareChunkRenders",
        at = @At(value = "NEW", target = "net/minecraft/client/renderer/DynamicUniforms$ChunkSectionInfo")
    )
    private DynamicUniforms.ChunkSectionInfo vs$shipSectionInfo(
        final Matrix4fc modelView, final int x, final int y, final int z, final float visibility,
        final int atlasWidth, final int atlasHeight,
        final Operation<DynamicUniforms.ChunkSectionInfo> original,
        @Local(ordinal = 0) final SectionRenderDispatcher.RenderSection renderSection
    ) {
        final ClientShip ship = vs$shipSections.get(renderSection);
        if (ship == null) {
            return original.call(modelView, x, y, z, visibility, atlasWidth, atlasHeight);
        }

        final Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        final Vec3 camPos = camera.position();
        final BlockPos camBlock = camera.blockPosition();
        final BlockPos origin = renderSection.getRenderOrigin(); // shipyard-space section origin

        // MV_ship = viewRot * T(-camPos) * shipToWorld * T(origin) * T(camPos - camBlock).
        // Composed in doubles (shipyard coords are ~1e7) and cast to float only at the end.
        final Matrix4d mv = new Matrix4d(modelView)
            .translate(-camPos.x, -camPos.y, -camPos.z)
            .mul(ship.getRenderTransform().getShipToWorld())
            .translate(origin.getX(), origin.getY(), origin.getZ())
            .translate(camPos.x - camBlock.getX(), camPos.y - camBlock.getY(), camPos.z - camBlock.getZ());

        // ChunkPosition = CameraBlockPos zeroes the shader's translation term; force visibility to
        // 1 so freshly-compiled ship sections don't fade in from invisible.
        return original.call(
            (Matrix4fc) new Matrix4f().set(mv),
            camBlock.getX(), camBlock.getY(), camBlock.getZ(),
            1.0f, atlasWidth, atlasHeight
        );
    }

    /**
     * Draws the block selection outline for a targeted ship block at the ship's transform.
     *
     * <p>{@code renderBlockOutline} hands {@code renderHitOutline} the camera position, and the
     * outline is drawn at {@code outlineState.pos() - camera}, which puts a targeted ship block's
     * outline out in shipyard space instead of on the ship. Move the pose onto the ship block and
     * pass the block position as the camera, so the outline is drawn at the pose origin.
     */
    @WrapOperation(
        method = "renderBlockOutline",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;renderHitOutline(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;DDDLnet/minecraft/client/renderer/state/level/BlockOutlineRenderState;IF)V"
        )
    )
    private void vs$transformShipBlockOutline(final LevelRenderer instance, final PoseStack poseStack,
        final VertexConsumer consumer, final double camX, final double camY, final double camZ,
        final BlockOutlineRenderState outline, final int color, final float width, final Operation<Void> original) {
        final ClientLevel level = Minecraft.getInstance().level;
        final BlockPos pos = outline.pos();
        final ClientShip ship = level == null ? null : VSGameUtilsKt.getShipObjectManagingPos(level, pos);
        if (ship == null) {
            original.call(instance, poseStack, consumer, camX, camY, camZ, outline, color, width);
            return;
        }
        poseStack.pushPose();
        VSClientGameUtils.transformRenderWithShip(
            ship.getRenderTransform(), poseStack, pos.getX(), pos.getY(), pos.getZ(), camX, camY, camZ);
        original.call(instance, poseStack, consumer, (double) pos.getX(), (double) pos.getY(), (double) pos.getZ(),
            outline, color, width);
        poseStack.popPose();
    }

    /**
     * Draws block entities that sit on a ship at the ship's transform instead of at their raw
     * shipyard coordinates.
     *
     * <p>{@code submitBlockEntities} walks {@code levelRenderState.blockEntityRenderStates} and does
     * {@code poseStack.translate(state.blockPos - cameraPos)} before handing the pose to the block
     * entity render dispatcher. Ship sections are in {@code visibleSections} (we put them there), so
     * ship block entities were already being extracted and submitted - just translated out to
     * shipyard space millions of blocks from the camera, i.e. never actually visible. That's why a
     * chest or a ship helm's wheel vanished the moment its ship was assembled.
     *
     * <p>Substitute the full ship render transform (rotation included), the same treatment the
     * ship's chunk geometry gets above. The camera position is recovered from the translate
     * arguments, which are {@code blockPos - camera}, so no extra locals are needed.
     */
    @WrapOperation(
        method = "submitBlockEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/renderer/SubmitNodeStorage;)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V")
    )
    private void vs$transformShipBlockEntity(final PoseStack poseStack, final double x, final double y,
        final double z, final Operation<Void> original,
        @Local final BlockEntityRenderState blockEntityRenderState) {
        final ClientLevel level = Minecraft.getInstance().level;
        final BlockPos pos = blockEntityRenderState.blockPos;
        final ClientShip ship = level == null || pos == null
            ? null : VSGameUtilsKt.getShipObjectManagingPos(level, pos);
        if (ship == null) {
            original.call(poseStack, x, y, z);
            return;
        }
        VSClientGameUtils.transformRenderWithShip(
            ship.getRenderTransform(), poseStack,
            pos.getX(), pos.getY(), pos.getZ(),
            pos.getX() - x, pos.getY() - y, pos.getZ() - z
        );
    }

    /**
     * Draws the block-breaking crack overlay on a ship block at the ship's transform.
     *
     * <p>Same shape as the two cases above: {@code submitBlockDestroyAnimation} walks
     * {@code levelRenderState.blockBreakingRenderStates} and does
     * {@code poseStack.translate(state.blockPos() - cameraPos)} before submitting the crack model, so
     * the cracks on a mined ship block appeared out in shipyard space instead of on the block. The
     * pick already resolves ship blocks correctly (they can be broken), so only the draw transform
     * was wrong.
     */
    @WrapOperation(
        method = "submitBlockDestroyAnimation(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V")
    )
    private void vs$transformShipBlockBreaking(final PoseStack poseStack, final double x, final double y,
        final double z, final Operation<Void> original,
        @Local final BlockBreakingRenderState blockBreakingRenderState) {
        final ClientLevel level = Minecraft.getInstance().level;
        final BlockPos pos = blockBreakingRenderState == null ? null : blockBreakingRenderState.blockPos();
        final ClientShip ship = level == null || pos == null
            ? null : VSGameUtilsKt.getShipObjectManagingPos(level, pos);
        if (ship == null) {
            original.call(poseStack, x, y, z);
            return;
        }
        VSClientGameUtils.transformRenderWithShip(
            ship.getRenderTransform(), poseStack,
            pos.getX(), pos.getY(), pos.getZ(),
            pos.getX() - x, pos.getY() - y, pos.getZ() - z
        );
    }

    /**
     * Whether a ship render section at the given shipyard section coords is inside the view frustum,
     * tested against its ship-transformed world AABB. Eight corners because the ship transform
     * includes rotation, so a min/max-only box would be wrong.
     */
    @Unique
    private boolean vs$isShipSectionVisible(final ClientShip ship, final int sectionX,
        final int sectionY, final int sectionZ) {
        final org.joml.Matrix4dc s2w = ship.getRenderTransform().getShipToWorld();
        final double x0 = sectionX << 4;
        final double y0 = sectionY << 4;
        final double z0 = sectionZ << 4;
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        for (int c = 0; c < 8; c++) {
            final org.joml.Vector3d p = s2w.transformPosition(new org.joml.Vector3d(
                x0 + ((c & 1) == 0 ? 0.0 : 16.0),
                y0 + ((c & 2) == 0 ? 0.0 : 16.0),
                z0 + ((c & 4) == 0 ? 0.0 : 16.0)));
            minX = Math.min(minX, p.x);
            minY = Math.min(minY, p.y);
            minZ = Math.min(minZ, p.z);
            maxX = Math.max(maxX, p.x);
            maxY = Math.max(maxY, p.y);
            maxZ = Math.max(maxZ, p.z);
        }
        return vs$shipCullingFrustum.isVisible(
            new net.minecraft.world.phys.AABB(minX, minY, minZ, maxX, maxY, maxZ));
    }
}
