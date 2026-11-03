package li.cil.oc2.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import li.cil.oc2.common.blockentity.ChargerBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

import li.cil.oc2.client.renderer.ModRenderType;
import li.cil.oc2.common.blockentity.InternetGateWayBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class InternetGateWayRenderer implements BlockEntityRenderer<InternetGateWayBlockEntity, InternetGateWayRenderer.GateWayRenderState> {
    private static final float BLOCK_HEIGHT = 14f/16f;
    private static final float PORTAL_POSITION = 0.4f;
    private static final float EMITTER_POSITION = -3f/16f;
    private static final float EMITTER_SIZE = 6f/16f;
    private static final float EMITTER_PIXEL_SIZE = EMITTER_SIZE / 9f;
    private static final int[] SCRAMBLER = {1, 2, 0, 3, 13, 5, 15, 12, 6, 14, 10, 11, 7, 8, 9, 4};

    public static class GateWayRenderState extends BlockEntityRenderState {
        public InternetGateWayBlockEntity gateWay;
    }

    public InternetGateWayRenderer(final BlockEntityRendererProvider.Context context) {
        //this.renderer = context.getBlockEntityRenderDispatcher();
    }

    @Override
    public GateWayRenderState createRenderState() {
        return new GateWayRenderState();
    }

    @Override
    public void extractRenderState(final InternetGateWayBlockEntity blockEntity, final GateWayRenderState state, final float partialTicks, final Vec3 cameraPosition, @Nullable final ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.gateWay = blockEntity;
    }

    @Override
    public void submit(final GateWayRenderState state, final PoseStack stack, final SubmitNodeCollector collector, final CameraRenderState camera) {
        final InternetGateWayBlockEntity gateWay = state.gateWay;
        if (gateWay == null) {
            return;
        }
        stack.pushPose();
        stack.translate(0.5f, BLOCK_HEIGHT, 0.5f);
        stack.pushPose();
        long time = System.currentTimeMillis();
        long dt = time - gateWay.lastRender;
        gateWay.lastRender = time;
        if (dt > 1000) {
            //Catch up if rendering stopped
            gateWay.handledInboundCount = gateWay.inboundCount;
            gateWay.handledOutboundCount = gateWay.outboundCount;
        }
        double phase = ((double)time)/1000d;
        stack.translate(0f, PORTAL_POSITION+Math.sin(phase/2)*0.03f, 0f);
        //stack.mulPose(Vector3f.XN.rotationDegrees((float)Math.sin(phase)*5));
        //stack.mulPose(Vector3f.ZN.rotationDegrees((float)Math.sin(phase+2)*5));
        float halfSide = 0.2f;

        collector.submitCustomGeometry(stack, RenderTypes.endPortal(), (pose, portal) -> {
            Matrix4f portalMatrix = pose.pose();
            renderCube(portal, portalMatrix, halfSide, 0, 0, 0, false);
        });
        stack.popPose();
        stack.translate(0, EMITTER_POSITION, 0);
        int pendingPackets = Math.max(0, gateWay.inboundCount - gateWay.handledInboundCount) + Math.max(0, gateWay.outboundCount - gateWay.handledOutboundCount);
        float speedMod = Math.max(1f, Math.min(1.5f, pendingPackets * 0.025f));
        final List<float[]> cubes = new ArrayList<>();
        for (int x=0;x<InternetGateWayBlockEntity.EMITTER_SIDE_PIXELS;x++) {
            for (int z=0;z<InternetGateWayBlockEntity.EMITTER_SIDE_PIXELS;z++) {
                int flatPos = x * InternetGateWayBlockEntity.EMITTER_SIDE_PIXELS + z;
                if (gateWay.animProgress[flatPos] < 1f) {
                    float animPos = gateWay.animProgress[flatPos];
                    gateWay.animProgress[flatPos] += dt/1000f * speedMod;
                    if (gateWay.animReversed[flatPos]) {
                        animPos = 1 - animPos;
                    }
                    cubes.add(new float[]{(x*2-3f)*EMITTER_PIXEL_SIZE, animPos*(PORTAL_POSITION-EMITTER_POSITION), (z*2-3f)*EMITTER_PIXEL_SIZE});
                }
            }
        }
        if (!cubes.isEmpty()) {
            collector.submitCustomGeometry(stack, ModRenderType.getGateWayParticle(), (pose, packet) -> {
                Matrix4f matrix = pose.pose();
                for (final float[] cube : cubes) {
                    renderCube(packet, matrix, EMITTER_PIXEL_SIZE/2, cube[0], cube[1], cube[2], true);
                }
            });
        }
        while (gateWay.handledInboundCount<gateWay.inboundCount || gateWay.handledOutboundCount<gateWay.outboundCount) {
            int scrambledPointer = SCRAMBLER[gateWay.pointer];
            if (gateWay.animProgress[scrambledPointer]>=1f) {
                gateWay.pointer += 1;
                if (gateWay.pointer>=InternetGateWayBlockEntity.EMITTER_SIDE_PIXELS*InternetGateWayBlockEntity.EMITTER_SIDE_PIXELS) {
                    gateWay.pointer = 0;
                }
                gateWay.animProgress[scrambledPointer] = 0f - (float)Math.random() * 0.1f;
                if (gateWay.handledInboundCount<gateWay.inboundCount) {
                    gateWay.animReversed[scrambledPointer] = true;
                    gateWay.handledInboundCount += 1;
                } else {
                    gateWay.animReversed[scrambledPointer] = false;
                    gateWay.handledOutboundCount += 1;
                }
            } else {
                break;
            }
        }

        stack.popPose();
    }

    private void renderCube(VertexConsumer vertices, Matrix4f matrix, float halfSide, float x, float y, float z, boolean enColor) {
        //X axis
        renderCubeFace(vertices, matrix, +halfSide+x, 0+y, 0+z, halfSide, 5, 1, 0, enColor);
        renderCubeFace(vertices, matrix, -halfSide+x, 0+y, 0+z, halfSide, 5, 0, 1, enColor);
        //Y axis
        renderCubeFace(vertices, matrix, 0+x, +halfSide+y, 0+z, halfSide, 0, 5, 1, enColor);
        renderCubeFace(vertices, matrix, 0+x, -halfSide+y, 0+z, halfSide, 1, 5, 0, enColor);
        //Z axis
        renderCubeFace(vertices, matrix, 0+x, 0+y, -halfSide+z, halfSide, 0, 1, 5, enColor);
        renderCubeFace(vertices, matrix, 0+x, 0+y, +halfSide+z, halfSide, 1, 0, 5, enColor);
    }

    private void renderCubeFace(VertexConsumer vertices, Matrix4f matrix, float x, float y, float z, float halfSideSize, int signShiftX, int signShiftY, int signShiftZ, boolean enColor) {
        float signs[] = {-1f, -1f, 1f, 1f, -1f, 0f, 0f, 0f, 0f};
        for (int i=0;i<4;i++) {
            vertices.addVertex(matrix, x+halfSideSize*signs[i+signShiftX], y+halfSideSize*signs[i+signShiftY], z+halfSideSize*signs[i+signShiftZ]);
            if (enColor) {
                vertices.setColor(1f, 1f, 1f, 1f);
            }
        }

    }

    @Override
    public AABB getRenderBoundingBox(InternetGateWayBlockEntity block) {
        return block.getRenderBoundingBox();
    }
}
