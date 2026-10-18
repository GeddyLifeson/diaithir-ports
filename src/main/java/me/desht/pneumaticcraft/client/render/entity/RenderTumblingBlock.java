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

package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.common.entity.projectile.TumblingBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

public class RenderTumblingBlock extends EntityRenderer<TumblingBlockEntity, RenderTumblingBlock.State> {
    public static class State extends FallingBlockRenderState {
        Vector3f tumbleVec;
    }

    public RenderTumblingBlock(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TumblingBlockEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        ItemStack stack = entity.getStack();
        BlockState blockState = stack.getItem() instanceof BlockItem bi ? bi.getBlock().defaultBlockState() : null;
        var mb = state.movingBlockRenderState;
        mb.blockState = blockState == null || entity.level().getBlockState(entity.blockPosition()) == blockState ?
                net.minecraft.world.level.block.Blocks.AIR.defaultBlockState() : blockState;
        BlockPos pos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
        mb.randomSeedPos = entity.getOrigin();
        mb.blockPos = pos;
        if (entity.level() instanceof ClientLevel clientLevel) {
            mb.biome = clientLevel.getBiome(pos);
            mb.cardinalLighting = clientLevel.cardinalLighting();
            mb.lightEngine = clientLevel.getLightEngine();
        }
        state.tumbleVec = entity.tumbleVec;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.movingBlockRenderState.blockState.getRenderShape() == RenderShape.MODEL) {
            poseStack.pushPose();
            if (state.tumbleVec != null) {
                // spin the block on the x & z axes
                poseStack.translate(0, 0.5, 0);
                poseStack.mulPose(Axis.of(state.tumbleVec).rotationDegrees(state.ageInTicks * 18));
                poseStack.translate(-0.5, -0.5, -0.5);
            } else {
                poseStack.translate(-0.5, 0.0, -0.5);
            }
            collector.submitMovingBlock(poseStack, state.movingBlockRenderState);
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }
}
