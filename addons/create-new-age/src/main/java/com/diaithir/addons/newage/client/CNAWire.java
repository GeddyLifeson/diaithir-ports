package com.diaithir.addons.newage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import com.diaithir.addons.newage.config.CNAConfig;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class CNAWire {
    public static final float SAG_FACTOR = 0.9f;
    private static final Vector3f GLOBAL_UP = new Vector3f(0.0f, 1.0f, 0.0f);

    private final Vector3f start;
    private final Vector3f direction;
    private final Vector3f up;
    private final float sectionsPerMeter;
    private final float totalLength;
    private final float thickness;
    private final int maxLength;

    public CNAWire(Vector3f start, Vector3f end, float sectionsPerMeter, float thickness, int maxLength) {
        this.start = start;
        Vector3f difference = end.sub(start);
        this.totalLength = difference.length();
        direction = difference.div(totalLength);
        up = calculateUp(direction);
        this.sectionsPerMeter = sectionsPerMeter;
        this.thickness = thickness;
        this.maxLength = maxLength;
    }

    private Vector3f calculateUp(Vector3f direction) {
        Vector3f right = new Vector3f(direction).cross(GLOBAL_UP);
        if (right.equals(new Vector3f(0.0f), 0.01f))
            return new Vector3f(1.0f, 0.0f, 0.0f);
        return right.cross(direction).normalize();
    }

    private float catenary(double x, double length, int sections) {
        double a = length / CNAConfig.getServer().maxWireLength.get() * SAG_FACTOR;
        x = (x / sections * 2 - 1);
        return (float) ((Math.cosh(x) - Math.cosh(1.0f)) * a);
    }

    public Vector3f getDirection() {
        return new Vector3f(direction);
    }

    public Vector3f getUp() {
        return new Vector3f(up);
    }

    public void render(VertexConsumer consumer, PoseStack.Pose basePose, Level level) {
        PoseStack poseStack = new PoseStack();
        poseStack.last().set(basePose);
        poseStack.translate(start.x - Math.floor(start.x), start.y - Math.floor(start.y), start.z - Math.floor(start.z));
        poseStack.mulPose(new Matrix4f().rotateTowards(direction, up));

        int maxSections = Mth.ceil((maxLength * Mth.sqrt(3.0F)) * sectionsPerMeter * 2.0);
        int sectionsAmount = Math.min((int) Math.ceil(totalLength * sectionsPerMeter), maxSections);
        float catenaryScalar = new Vector3f(up).mul(GLOBAL_UP).length();
        float lastCatenary = 0.0f;
        float sectionLength = 1.0F / sectionsPerMeter;

        for (int sectionId = 1; sectionId <= sectionsAmount; sectionId++) {
            float sectionOffset = sectionLength * sectionId;
            Vector3f lightPos = new Vector3f(start)
                    .add(new Vector3f(direction).mul(sectionOffset))
                    .add(new Vector3f(up).mul(lastCatenary));
            BlockPos lightBlockPos = BlockPos.containing(new Vec3(lightPos));
            int block = level.getBrightness(LightLayer.BLOCK, lightBlockPos);
            int sky = level.getBrightness(LightLayer.SKY, lightBlockPos);
            int light = LightCoordsUtil.pack(block, sky);

            float catenary = catenary(sectionId, totalLength, sectionsAmount) * catenaryScalar;
            float ht = thickness / 2;
            float vOffset = (sectionId % 2 == 0) ? 0.5f : 0.0f;
            for (int i = -1; i <= 1; i += 2) {
                addVertex(consumer,  ht,  ht * i + lastCatenary, 0.0f,          0.0f, 0.5f + vOffset, poseStack, light);
                addVertex(consumer, -ht, -ht * i + lastCatenary, 0.0f,          0.0f, 0.0f + vOffset, poseStack, light);
                addVertex(consumer, -ht, -ht * i + catenary,       sectionLength, 1.0f, 0.0f + vOffset, poseStack, light);
                addVertex(consumer,  ht,  ht * i + catenary,       sectionLength, 1.0f, 0.5f + vOffset, poseStack, light);
            }

            lastCatenary = catenary;
            poseStack.translate(0.0f, 0.0f, sectionLength);
        }
    }

    private static void addVertex(VertexConsumer consumer, float x, float y, float z, float u, float v, PoseStack poseStack, int packedLight) {
        consumer.addVertex(poseStack.last().pose(), x, y, z)
                .setColor(1.0f, 1.0f, 1.0f, 1.0f)
                .setUv(u, v)
                .setLight(packedLight)
                .setNormal(0.0f, 1.0f, 0.0f);
    }
}
