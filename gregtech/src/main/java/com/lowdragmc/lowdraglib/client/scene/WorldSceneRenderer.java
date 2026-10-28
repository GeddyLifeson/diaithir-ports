package com.lowdragmc.lowdraglib.client.scene;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.lowdragmc.lowdraglib.client.shader.management.ShaderManager;
import com.lowdragmc.lowdraglib.client.utils.glu.Project;
import com.lowdragmc.lowdraglib.utils.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.model.data.ModelData;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.minecraft.world.level.block.RenderShape.INVISIBLE;


/**
 * @author KilaBash
 * @date 2022/05/25
 * @implNote render a scene, through VBO compilation scene, greatly optimize rendering performance.
 */
@SuppressWarnings("ALL")
@OnlyIn(Dist.CLIENT)
public abstract class WorldSceneRenderer {
    protected static final FloatBuffer MODELVIEW_MATRIX_BUFFER = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    protected static final FloatBuffer PROJECTION_MATRIX_BUFFER = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    protected static final IntBuffer VIEWPORT_BUFFER = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
    protected static final FloatBuffer PIXEL_DEPTH_BUFFER = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    protected static final FloatBuffer OBJECT_POS_BUFFER = ByteBuffer.allocateDirect(3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    enum CacheState {
        UNUSED,
        NEED,
        COMPILING,
        COMPILED
    }

    public final Level world;
    public final Map<Collection<BlockPos>, ISceneBlockRenderHook> renderedBlocksMap;/* ported: removed */
    protected Set<BlockPos> tileEntities;
    protected boolean useCache;
    @Getter @Setter
    protected boolean endBatchLast = false;// if true, endBatch will be called after all rendering
    protected boolean ortho;
    protected AtomicReference<CacheState> cacheState;
    protected int maxProgress;
    protected int progress;
    protected Thread thread;
    protected ParticleManager particleManager;
    protected Camera camera;
    protected CameraEntity cameraEntity;
    private Consumer<WorldSceneRenderer> beforeRender;
    private Consumer<WorldSceneRenderer> afterRender;
    @Setter @Nullable
    private BiConsumer<MultiBufferSource, Float> beforeBatchEnd;
    private Consumer<BlockHitResult> onLookingAt;
    @Setter @Nullable
    private ISceneEntityRenderHook sceneEntityRenderHook;
    protected int clearColor;
    @Getter
    private Vector3f lastHit;
    @Getter
    private BlockHitResult lastTraceResult;
    private Set<BlockPos> blocked;
    private Vector3f eyePos = new Vector3f(0, 0, 10f);
    private Vector3f lookAt = new Vector3f(0, 0, 0);
    private Vector3f worldUp = new Vector3f(0, 1, 0);
    @Getter
    private float fov = 60f;
    private float minX, maxX, minY, maxY, minZ, maxZ;

    public WorldSceneRenderer(Level world) {
        this.world = world;
        renderedBlocksMap = new LinkedHashMap<>();
        cacheState = new AtomicReference<>(CacheState.UNUSED);
    }

    public WorldSceneRenderer setParticleManager(ParticleManager particleManager) { return null; /* ported stub */ }

    public ParticleManager getParticleManager() {
        return particleManager;
    }

    public WorldSceneRenderer useCacheBuffer(boolean useCache) { return null; /* ported stub */ }

    public WorldSceneRenderer useOrtho(boolean ortho) {
        this.ortho = ortho;
        return this;
    }

    public WorldSceneRenderer deleteCacheBuffer() { return null; /* ported stub */ }

    public WorldSceneRenderer needCompileCache() {
        if (cacheState.get() == CacheState.COMPILING && thread != null) {
            thread.interrupt();
            thread = null;
        }
        cacheState.set(CacheState.NEED);
        return this;
    }

    public WorldSceneRenderer setBeforeWorldRender(Consumer<WorldSceneRenderer> callback) {
        this.beforeRender = callback;
        return this;
    }

    public WorldSceneRenderer setAfterWorldRender(Consumer<WorldSceneRenderer> callback) {
        this.afterRender = callback;
        return this;
    }

    public WorldSceneRenderer addRenderedBlocks(Collection<BlockPos> blocks, ISceneBlockRenderHook renderHook) {
        if (blocks != null) {
            this.renderedBlocksMap.put(blocks, renderHook);
        }
        return this;
    }

    public WorldSceneRenderer setBlocked(Set<BlockPos> blocked) {
        this.blocked = blocked;
        return this;
    }

    public WorldSceneRenderer setOnLookingAt(Consumer<BlockHitResult> onLookingAt) {
        this.onLookingAt = onLookingAt;
        return this;
    }

    public boolean isUseCache() {
        return useCache;
    }

    public void setClearColor(int clearColor) {
        this.clearColor = clearColor;
    }

    public void render(@Nonnull PoseStack poseStack, float x, float y, float width, float height, int mouseX, int mouseY) {
        // do not render if the minecraft is reloading
        if (Minecraft.getInstance().getOverlay() instanceof LoadingOverlay) {
            return;
        }
        // setupCamera
        var pose = poseStack.last().pose();
        Vector4f pos = new Vector4f(x, y, 0, 1.0F);
        pos = pose.transform(pos);
        Vector4f size = new Vector4f(x + width, y + height, 0, 1.0F);
        size = pose.transform(size);
        x = pos.x();
        y = pos.y();
        width = size.x() - x;
        height = size.y() - y;
        PositionedRect viewport = getPositionedRect((int) x, (int) y, (int) width, (int) height);
        var topLeft = poseStack.last().pose().transformPosition(new Vector3f(0.0f, 0.0f, 0.0f));
        PositionedRect mouse = getPositionedRect((int) (mouseX + topLeft.x), (int) (mouseY + topLeft.y), 0, 0);
        mouseX = mouse.position.x;
        mouseY = mouse.position.y;
        setupCamera(viewport);
        // render TrackedDummyWorld
        drawWorld();
        // check lookingAt
        this.lastTraceResult = null;
        this.lastHit = unProject(mouseX, mouseY);
        if (onLookingAt != null && mouseX > viewport.position.x && mouseX < viewport.position.x + viewport.size.width
                && mouseY > viewport.position.y && mouseY < viewport.position.y + viewport.size.height) {
            BlockHitResult result = rayTrace(lastHit);
            if (result != null) {
                this.lastTraceResult = null;
                this.lastTraceResult = result;
                onLookingAt.accept(result);
            }
        }
        // resetCamera
        resetCamera();
    }

    public Vector3f getEyePos() {
        return eyePos;
    }

    public Vector3f getLookAt() {
        return lookAt;
    }

    public Vector3f getWorldUp() {
        return worldUp;
    }

    public void setCameraLookAt(Vector3f eyePos, Vector3f lookAt, Vector3f worldUp) {
        this.eyePos = eyePos;
        this.lookAt = lookAt;
        this.worldUp = worldUp;
        if (camera != null) {
            Vector3f xzProduct = new Vector3f(lookAt.x() - eyePos.x(), 0, lookAt.z() - eyePos.z());
            double angleYaw = Math.toDegrees(xzProduct.angle(new Vector3f(0, 0, 1)));
            if (xzProduct.angle(new Vector3f(1, 0, 0)) < Math.PI / 2) {
                angleYaw = -angleYaw;
            }
            double anglePitch = Math.toDegrees(new Vector3f(lookAt).sub(new Vector3f(eyePos)).angle(new Vector3f(0, 1, 0))) - 90;
            cameraEntity.setPos(eyePos.x(), eyePos.y() - cameraEntity.getEyeHeight(), eyePos.z());
            cameraEntity.xo = cameraEntity.getX();
            cameraEntity.yo = cameraEntity.getY();
            cameraEntity.zo = cameraEntity.getZ();
            cameraEntity.setYRot((float) angleYaw);
            cameraEntity.setXRot((float) anglePitch);
            cameraEntity.yRotO = cameraEntity.getYRot();
            cameraEntity.xRotO = cameraEntity.getXRot();
        }
    }

    public void setCameraLookAt(Vector3f lookAt, double radius, double rotationPitch, double rotationYaw) {
        Vector3f vecX = new Vector3f((float) Math.cos(rotationPitch), (float) 0, (float) Math.sin(rotationPitch));
        Vector3f vecY = new Vector3f(0, (float) (Math.tan(rotationYaw) * vecX.length()), 0);
        Vector3f pos = new Vector3f(vecX).add(vecY).normalize().mul((float) radius);
        setCameraLookAt(pos.add(lookAt.x(), lookAt.y(), lookAt.z()), lookAt, worldUp);
    }

    public void setFov(float fov) {
        this.fov = fov;
    }

    public void setCameraOrtho(float x, float y, float z) {
        this.minX = -x;
        this.maxX = x;
        this.minY = -y;
        this.maxY = y;
        this.minZ = -z;
        this.maxZ = z;
    }

    public void setCameraOrtho(float minX, float maxX, float minY, float maxY, float minZ, float maxZ) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    public PositionedRect getPositionedRect(int x, int y, int width, int height) {
        return new PositionedRect(new Position(x, y), new Size(width, height));
    }

    public PositionedRect getPositionRectRevert(int windowX, int windowY, int windowWidth, int windowHeight) {
        return new PositionedRect(new Position(windowX, windowY), new Size(windowWidth, windowHeight));
    }

    protected void setupCamera(PositionedRect viewport) {  /* ported stub */ }

    protected void clearView(int x, int y, int width, int height) {  /* ported stub */ }

    protected void resetCamera() {  /* ported stub */ }

    protected void drawWorld() {  /* ported stub */ }

    public boolean isCompiling() {
        return cacheState.get() == CacheState.COMPILING;
    }

    public double getCompileProgress() {
        if (maxProgress > 1000) {
            return progress * 1. / maxProgress;
        }
        return 0;
    }

    private void renderCacheBuffer(Minecraft mc, MultiBufferSource.BufferSource buffers, float particleTicks) {  /* ported stub */ }/* ported: removed */

    public static boolean canRenderInLayer(BlockState state, RenderType renderType) { return false; /* ported stub */ }/* ported: removed */

    private void renderTESR(Collection<BlockPos> poses, PoseStack poseStack, MultiBufferSource.BufferSource buffers, @Nullable ISceneBlockRenderHook hook, float partialTicks) {  /* ported stub */ }

    private void renderEntities(TrackedDummyWorld level, PoseStack poseStack, MultiBufferSource buffer, @Nullable ISceneEntityRenderHook hook, float partialTicks) {  /* ported stub */ }


    public static void setDefaultRenderLayerState(RenderType layer) {  /* ported stub */ }

    public BlockHitResult rayTrace(Vector3f hitPos) {
        Vec3 startPos = new Vec3(this.eyePos.x(), this.eyePos.y(), this.eyePos.z());
        if (ortho) {
            startPos = startPos.add(new Vec3(startPos.x - lookAt.x(), startPos.y - lookAt.y(), startPos.z - lookAt.z()).multiply(500, 500, 500));
        }
        hitPos = hitPos.mul(2, new Vector3f()); // Double view range to ensure pos can be seen.
        Vec3 endPos = new Vec3((hitPos.x() - startPos.x), (hitPos.y() - startPos.y), (hitPos.z() - startPos.z));
        try {
            return this.world.clip(new ClipContext(startPos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, cameraEntity));
        } catch (Exception e) {
            return null;
        }
    }

    public Vector3f project(Vector3f pos) { return null; /* ported stub */ }

    public Vector3f unProject(int mouseX, int mouseY) {
        return unProject(mouseX, mouseY, true);
    }

    public Vector3f unProject(int mouseX, int mouseY, boolean checkDepth) { return null; /* ported stub */ }

    /***
     * For better performance, You'd better handle the event {@link #setOnLookingAt(Consumer)} or {@link #getLastTraceResult()}
     * @param mouseX xPos in Texture
     * @param mouseY yPos in Texture
     * @return RayTraceResult Hit
     */
    protected BlockHitResult screenPos2BlockPosFace(int mouseX, int mouseY, int x, int y, int width, int height) { return null; /* ported stub */ }

    /***
     * For better performance, You'd better do project in {@link #setAfterWorldRender(Consumer)}
     * @param pos BlockPos
     * @param depth should pass Depth Test
     * @return x, y, z
     */
    protected Vector3f blockPos2ScreenPos(BlockPos pos, boolean depth, int x, int y, int width, int height) { return null; /* ported stub */ }

    public static class VertexConsumerWrapper implements VertexConsumer {

        final VertexConsumer builder;
        @Setter
        float offsetX, offsetY, offsetZ;
        float r = 1, g = 1, b = 1, a = 1;

        public VertexConsumerWrapper(VertexConsumer builder) {
            this.builder = builder;
        }

        public void addOffset(float offsetX, float offsetY, float offsetZ) {
            this.offsetX += offsetX;
            this.offsetY += offsetY;
            this.offsetZ += offsetZ;
        }

        public void setColorMultiplier(float r, float g, float b, float a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }

        public void clearOffset() {
            this.offsetX = 0;
            this.offsetY = 0;
            this.offsetZ = 0;
        }

        public void clearColor() {
            this.r = 1;
            this.g = 1;
            this.b = 1;
            this.a = 1;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            return builder.addVertex(x + offsetX, y + offsetY, z + offsetZ);
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return builder.setColor((int) (red * r), (int) (green * g), (int) (blue * b), (int) (alpha * a));
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return builder.setUv(u, v);
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return builder.setUv1(u, u);
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return builder.setUv2(u, u);
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            return builder.setNormal(x, y, z);
        }

        @Override
        public VertexConsumer setColor(int color) {
            return builder.setColor(color);
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            return builder.setLineWidth(width);
        }
    }
}
