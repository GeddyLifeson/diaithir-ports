package org.valkyrienskies.mod.mixin.client.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3d;
import org.joml.primitives.AABBic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.mixinducks.client.render.IVSViewAreaMethods;
import org.valkyrienskies.mod.mixinducks.mod_compat.vanilla_renderer.LevelRendererDuck;

/**
 * Draws VS2 ship chunk sections with the 26.1 level renderer. Ship sections live in {@link MixinViewArea}; they are
 * compiled by the normal SectionRenderDispatcher and added to the visible list while the chunk draws are prepared. Each
 * ship section gets its own ChunkSection uniform (the shader is {@code ProjMat * ModelViewMat * (Position + ChunkPosition
 * - CameraBlockPos + CameraOffset)}): ChunkPosition is moved next to the camera and ModelViewMat carries the ship's
 * render transform, so no precision is lost at the shipyard's large coordinates.
 */
@Mixin(LevelRenderer.class)
public abstract class MixinLevelRendererShips implements LevelRendererDuck {
    @Unique private static final Logger VS_LOG = LoggerFactory.getLogger("vs_render");

    @Shadow @Final private ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections;
    @Shadow private ViewArea viewArea;
    @Shadow private ClientLevel level;
    @Shadow @Final private net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    @Shadow private SectionRenderDispatcher sectionRenderDispatcher;

    /** Ship sections added to visibleSections for this frame, with the ship that draws them. */
    @Unique private final IdentityHashMap<SectionRenderDispatcher.RenderSection, ClientShip> vs$frameShips = new IdentityHashMap<>();
    @Unique private SectionRenderDispatcher.RenderSection vs$current;
    @Unique private int vs$added;
    @Unique private int vs$dbg;

    @Override
    public void vs$setNeedsFrustumUpdate() {
    }

    @Unique
    private void vs$collect(final boolean compile) {
        vs$frameShips.clear();
        if (level == null || viewArea == null || sectionRenderDispatcher == null) {
            return;
        }
        final IVSViewAreaMethods area = (IVSViewAreaMethods) viewArea;
        final RenderRegionCache cache = compile ? new RenderRegionCache() : null;
        int dirty = 0;
        for (final ClientShip ship : VSGameUtilsKt.getShipObjectWorld(level).getLoadedShips()) {
            final AABBic bb = ship.getShipAABB();
            if (bb == null) {
                continue;
            }
            for (int cx = (bb.minX() >> 4); cx <= (bb.maxX() >> 4); cx++) {
                for (int cz = (bb.minZ() >> 4); cz <= (bb.maxZ() >> 4); cz++) {
                    final LevelChunk chunk = level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
                    if (chunk == null) {
                        continue;
                    }
                    final LevelChunkSection[] sections = chunk.getSections();
                    for (int i = 0; i < sections.length; i++) {
                        if (sections[i] == null || sections[i].hasOnlyAir()) {
                            continue;
                        }
                        final int sy = chunk.getSectionYFromSectionIndex(i);
                        final SectionRenderDispatcher.RenderSection section = area.vs$getOrCreateShipRenderSection(cx, sy, cz);
                        if (section == null) {
                            continue;
                        }
                        vs$frameShips.put(section, ship);
                        if (compile && section.isDirty()) {
                            section.rebuildSectionAsync(cache);
                            section.setNotDirty();
                            dirty++;
                        }
                    }
                }
            }
        }
        if (compile && (vs$dbg++ % 300 == 0)) {
            VS_LOG.info("ship render: ships={} sections={} compiledThisFrame={}", VSGameUtilsKt.getShipObjectWorld(level).getLoadedShips().size(), vs$frameShips.size(), dirty);
        }
    }

    @Inject(method = "compileSections", at = @At("HEAD"))
    private void vs$drainShipChunks(final net.minecraft.client.Camera camera, final CallbackInfo ci) {
        final var mgr = org.valkyrienskies.mod.common.assembly.SeamlessChunksManager.get();
        if (mgr != null) {
            mgr.drainDeferredBatch();
        }
    }

    @Inject(method = "compileSections", at = @At("TAIL"))
    private void vs$compileShipSections(final net.minecraft.client.Camera camera, final CallbackInfo ci) {
        vs$collect(true);
    }

    @Inject(method = "prepareChunkRenders", at = @At("HEAD"))
    private void vs$addShipSections(final Matrix4fc modelViewMatrix, final CallbackInfoReturnable<ChunkSectionsToRender> cir) {
        vs$collect(false);
        vs$added = 0;
        for (final SectionRenderDispatcher.RenderSection s : vs$frameShips.keySet()) {
            visibleSections.add(s);
            vs$added++;
        }
    }

    @Inject(method = "prepareChunkRenders", at = @At("RETURN"))
    private void vs$removeShipSections(final Matrix4fc modelViewMatrix, final CallbackInfoReturnable<ChunkSectionsToRender> cir) {
        if (vs$added > 0) {
            visibleSections.removeElements(visibleSections.size() - vs$added, visibleSections.size());
            vs$added = 0;
        }
    }

    @WrapOperation(method = "prepareChunkRenders", at = @At(value = "INVOKE",
        target = "Lit/unimi/dsi/fastutil/objects/ObjectListIterator;next()Ljava/lang/Object;"))
    private Object vs$trackSection(final it.unimi.dsi.fastutil.objects.ObjectListIterator<?> it, final Operation<Object> original) {
        final Object o = original.call(it);
        vs$current = (SectionRenderDispatcher.RenderSection) o;
        return o;
    }

    @WrapOperation(method = "prepareChunkRenders", at = @At(value = "NEW",
        target = "(Lorg/joml/Matrix4fc;IIIFII)Lnet/minecraft/client/renderer/DynamicUniforms$ChunkSectionInfo;"))
    private DynamicUniforms.ChunkSectionInfo vs$shipSectionInfo(final Matrix4fc modelView, final int x, final int y, final int z,
        final float visibility, final int w, final int h, final Operation<DynamicUniforms.ChunkSectionInfo> original) {
        final ClientShip ship = vs$current == null ? null : vs$frameShips.get(vs$current);
        if (ship == null) {
            return original.call(modelView, x, y, z, visibility, w, h);
        }
        final Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        final ShipTransform t = ship.getRenderTransform();
        final double cbx = Math.floor(cam.x), cby = Math.floor(cam.y), cbz = Math.floor(cam.z);
        final BlockPos p = vs$current.getRenderOrigin();
        // reference block R in ship space (keeps all numbers small), its world position W_R
        final int rx = (int) Math.floor(t.getPositionInShip().x()), ry = (int) Math.floor(t.getPositionInShip().y()), rz = (int) Math.floor(t.getPositionInShip().z());
        final Matrix4d s2w = new Matrix4d(t.getShipToWorld());
        final Vector3d wr = s2w.transformPosition(new Vector3d(rx, ry, rz));
        s2w.m30(0).m31(0).m32(0); // linear part S
        final Vector3d camOff = new Vector3d(cbx - cam.x, cby - cam.y, cbz - cam.z);
        final Vector3d a = new Vector3d(wr.x - cam.x, wr.y - cam.y, wr.z - cam.z).sub(s2w.transformDirection(new Vector3d(camOff)));
        final Matrix4d af = new Matrix4d(s2w).setTranslation(a);
        final Matrix4d m = new Matrix4d().set(modelView).mul(af);
        return original.call(new Matrix4f().set(m), (int) cbx + (p.getX() - rx), (int) cby + (p.getY() - ry), (int) cbz + (p.getZ() - rz),
            visibility, w, h);
    }

    /** Extracts the block entities of ship sections; the dispatcher is told the camera in ship space for its range check. */
    @Inject(method = "extractVisibleBlockEntities(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/renderer/culling/Frustum;)V",
        at = @At("RETURN"))
    private void vs$extractShipBlockEntities(final net.minecraft.client.Camera camera, final float partialTick,
        final net.minecraft.client.renderer.state.level.LevelRenderState state, final net.minecraft.client.renderer.culling.Frustum frustum,
        final CallbackInfo ci) {
        if (level == null || viewArea == null) {
            return;
        }
        vs$collect(false);
        if (vs$frameShips.isEmpty()) {
            return;
        }
        final Vec3 cam = camera.position();
        final IdentityHashMap<ClientShip, Vec3> shipCams = new IdentityHashMap<>();
        for (final var e : vs$frameShips.entrySet()) {
            final var section = e.getKey();
            final java.util.List<net.minecraft.world.level.block.entity.BlockEntity> bes = section.getSectionMesh().getRenderableBlockEntities();
            if (bes.isEmpty()) {
                continue;
            }
            final ClientShip ship = e.getValue();
            final Vec3 sc = shipCams.computeIfAbsent(ship, sh -> {
                final Vector3d v = sh.getRenderTransform().getWorldToShip().transformPosition(new Vector3d(cam.x, cam.y, cam.z));
                return new Vec3(v.x, v.y, v.z);
            });
            blockEntityRenderDispatcher.prepare(sc);
            for (final var be : bes) {
                final var rs = blockEntityRenderDispatcher.tryExtractRenderState(be, partialTick, null, null);
                if (rs != null) {
                    state.blockEntityRenderStates.add(rs);
                }
            }
        }
        blockEntityRenderDispatcher.prepare(cam);
    }

    /** Puts the pose of a ship block entity at the block's world position with the ship's rotation and scale. */
    @WrapOperation(method = "submitBlockEntities", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;submit(Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"))
    private void vs$submitShipBlockEntity(final net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher d,
        final net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState rs, final com.mojang.blaze3d.vertex.PoseStack ps,
        final net.minecraft.client.renderer.SubmitNodeCollector c, final net.minecraft.client.renderer.state.level.CameraRenderState camState,
        final Operation<Void> original) {
        final BlockPos bp = rs.blockPos;
        final var ship = level == null ? null : VSGameUtilsKt.getShipObjectManagingPos(level, bp.getX() >> 4, bp.getZ() >> 4);
        if (!(ship instanceof ClientShip cs)) {
            original.call(d, rs, ps, c, camState);
            return;
        }
        final Matrix4d s2w = new Matrix4d(cs.getRenderTransform().getShipToWorld());
        final Vector3d w = s2w.transformPosition(new Vector3d(bp.getX(), bp.getY(), bp.getZ()));
        final Vec3 cam = camState.pos;
        final Vector3d sc = s2w.getScale(new Vector3d());
        final org.joml.Quaterniond qd = s2w.getNormalizedRotation(new org.joml.Quaterniond());
        ps.popPose(); // drop the caller's unmodified translation, then push our own and restore the balance afterwards
        ps.pushPose();
        ps.translate(w.x - cam.x, w.y - cam.y, w.z - cam.z);
        ps.mulPose(new org.joml.Quaternionf((float) qd.x, (float) qd.y, (float) qd.z, (float) qd.w));
        ps.scale((float) sc.x, (float) sc.y, (float) sc.z);
        original.call(d, rs, ps, c, camState);
        ps.popPose();
        ps.pushPose();
    }

    /** Draws the loads carried by cargo ships (diaithir-carts containers API) in each ship's render transform. */
    @Inject(method = "submitBlockEntities", at = @At("RETURN"))
    private void vs$submitCargo(final com.mojang.blaze3d.vertex.PoseStack ps, final net.minecraft.client.renderer.state.level.LevelRenderState state,
        final net.minecraft.client.renderer.SubmitNodeStorage storage, final CallbackInfo ci) {
        net.diaithir.ships.cargo.ClientCargo.submit(ps, state.cameraRenderState.pos, storage);
    }
}
