/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.render.fluid;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.client.PncFluidClient;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.entity.AbstractPneumaticCraftBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.IFluidTank;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractFluidTER<T extends AbstractPneumaticCraftBlockEntity> implements BlockEntityRenderer<T, AbstractFluidTER.State> {
    AbstractFluidTER(@SuppressWarnings("unused") BlockEntityRendererProvider.Context ctx) {
    }

    /** Snapshot of the tanks to render (render state objects must not touch the live block entity). */
    public static class State extends BlockEntityRenderState {
        final List<TankRenderInfo> tanks = new ArrayList<>();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T te, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(te, state, breakProgress);
        state.tanks.clear();
        if (te.nonNullLevel().isLoaded(te.getBlockPos())) {
            for (TankRenderInfo info : getTanksToRender(te)) {
                // copy the tank contents, keeping the face filter
                TankRenderInfo copy = new TankRenderInfo(info.getTank(), info.getBounds());
                copy.getFaces().clear();
                copy.getFaces().or(info.getFaces());
                state.tanks.add(copy);
            }
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.tanks.isEmpty()) return;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), (pose, builder) -> {
            Matrix4f posMat = pose.pose();
            for (TankRenderInfo info : state.tanks) {
                renderFluid(builder, info, posMat, state.lightCoords, OverlayTexture.NO_OVERLAY);
            }
        });
    }

    public static void renderFluid(VertexConsumer builder, TankRenderInfo tankRenderInfo, Matrix4f posMat, int combinedLight, int combinedOverlay) {
        IFluidTank tank = tankRenderInfo.getTank();
        if (tank.getFluidAmount() == 0) return;

        Fluid fluid = tank.getFluid().getFluid();
        Identifier texture = PncFluidClient.stillSprite(fluid);
        if (texture == null) return;
        TextureAtlasSprite still = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(texture);
        int[] cols = RenderUtils.decomposeColor(PncFluidClient.tintColor(tank.getFluid()));

        AABB bounds = getRenderBounds(tank, tankRenderInfo.getBounds());
        float x1 = (float) bounds.minX;
        float x2 = (float) bounds.maxX;
        float y1 = (float) bounds.minY;
        float y2 = (float) bounds.maxY;
        float z1 = (float) bounds.minZ;
        float z2 = (float) bounds.maxZ;
        
        if (tankRenderInfo.shouldRender(Direction.DOWN)) {
            float u1 = still.getU(x1);
            float u2 = still.getU(x2);
            float v1 = still.getV(z1);
            float v2 = still.getV(z2);
            builder.addVertex(posMat, x1, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(u1, v2).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, -1f, 0f);
            builder.addVertex(posMat, x1, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(u1, v1).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, -1f, 0f);
            builder.addVertex(posMat, x2, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(u2, v1).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, -1f, 0f);
            builder.addVertex(posMat, x2, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(u2, v2).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, -1f, 0f);
        }

        if (tankRenderInfo.shouldRender(Direction.UP)) {
            float u1 = still.getU(x1);
            float u2 = still.getU(x2);
            float v1 = still.getV(z1);
            float v2 = still.getV(z2);
            builder.addVertex(posMat, x1, y2, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(u1, v2).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 1f, 0f);
            builder.addVertex(posMat, x2, y2, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(u2, v2).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 1f, 0f);
            builder.addVertex(posMat, x2, y2, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(u2, v1).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 1f, 0f);
            builder.addVertex(posMat, x1, y2, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(u1, v1).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 1f, 0f);
        }

        if (tankRenderInfo.shouldRender(Direction.NORTH)) {
            float u1 = still.getU(x1);
            float u2 = still.getU(x2);
            float v1 = still.getV(y1);
            float v2 = still.getV(y2);
            builder.addVertex(posMat, x1, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(u1, v1).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 0f, -1f);
            builder.addVertex(posMat, x1, y2, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(u1, v2).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 0f, -1f);
            builder.addVertex(posMat, x2, y2, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(u2, v2).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 0f, -1f);
            builder.addVertex(posMat, x2, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(u2, v1).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 0f, -1f);
        }

        if (tankRenderInfo.shouldRender(Direction.SOUTH)) {
            float u1 = still.getU(x1);
            float u2 = still.getU(x2);
            float v1 = still.getV(y1);
            float v2 = still.getV(y2);
            builder.addVertex(posMat, x2, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(u2, v1).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 0f, 1f);
            builder.addVertex(posMat, x2, y2, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(u2, v2).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 0f, 1f);
            builder.addVertex(posMat, x1, y2, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(u1, v2).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 0f, 1f);
            builder.addVertex(posMat, x1, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(u1, v1).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 0f, 1f);
        }

        if (tankRenderInfo.shouldRender(Direction.WEST)) {
            float u1 = still.getU(y1);
            float u2 = still.getU(y2);
            float v1 = still.getV(z1);
            float v2 = still.getV(z2);
            builder.addVertex(posMat, x1, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(u1, v2).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(-1f, 0f, 0f);
            builder.addVertex(posMat, x1, y2, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(u2, v2).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(-1f, 0f, 0f);
            builder.addVertex(posMat, x1, y2, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(u2, v1).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(-1f, 0f, 0f);
            builder.addVertex(posMat, x1, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(u1, v1).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(-1f, 0f, 0f);
        }

        if (tankRenderInfo.shouldRender(Direction.EAST)) {
            float u1 = still.getU(y1);
            float u2 = still.getU(y2);
            float v1 = still.getV(z1);
            float v2 = still.getV(z2);
            builder.addVertex(posMat, x2, y1, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(u1, v1).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(1f, 0f, 0f);
            builder.addVertex(posMat, x2, y2, z1).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(u2, v1).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(1f, 0f, 0f);
            builder.addVertex(posMat, x2, y2, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(u2, v2).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(1f, 0f, 0f);
            builder.addVertex(posMat, x2, y1, z2).setColor(cols[1], cols[2], cols[3], cols[0]).setUv(u1, v2).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(1f, 0f, 0f);
        }
    }

     private static AABB getRenderBounds(IFluidTank tank, AABB tankBounds) {
        float percent = (float) tank.getFluidAmount() / (float) tank.getCapacity();

        double tankHeight = tankBounds.maxY - tankBounds.minY;
        double y1 = tankBounds.minY, y2 = (tankBounds.minY + (tankHeight * percent));
        if (tank.getFluid().getFluid().getFluidType().isLighterThanAir()) {
            double yOff = tankBounds.maxY - y2;  // lighter than air fluids move to the top of the tank
            y1 += yOff; y2 += yOff;
        }
        return new AABB(tankBounds.minX, y1, tankBounds.minZ, tankBounds.maxX, y2, tankBounds.maxZ);
    }

    static AABB rotateY(AABB in, int rot) {
        // clockwise rotation about the Y axis
        return switch (rot) {
            case 90 -> new AABB(1 - in.minZ, in.minY, in.minX, 1 - in.maxZ, in.maxY, in.maxX);
            case 180 -> new AABB(1 - in.minX, in.minY, 1 - in.minZ, 1 - in.maxX, in.maxY, 1 - in.maxZ);
            case 270 -> new AABB(in.minZ, in.minY, 1 - in.minX, in.maxZ, in.maxY, 1 - in.maxX);
            default -> throw new IllegalArgumentException("rot must be 90, 180 or 270");
        };
    }

    abstract Collection<TankRenderInfo> getTanksToRender(T te);
}