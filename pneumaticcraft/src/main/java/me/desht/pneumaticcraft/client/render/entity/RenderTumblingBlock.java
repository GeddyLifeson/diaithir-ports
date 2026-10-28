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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class RenderTumblingBlock extends me.desht.pneumaticcraft.client.render.LegacyEntityRenderer<TumblingBlockEntity> {
    public RenderTumblingBlock(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    protected void render(TumblingBlockEntity entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        ItemStack stack = entity.getStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) {
            return;
        }
        Block block = ((BlockItem) stack.getItem()).getBlock();
        BlockState state = block.defaultBlockState();
        if (state.getRenderShape() == RenderShape.MODEL) {
            Level world = entity.level();
            if (state != world.getBlockState(entity.blockPosition()) && state.getRenderShape() != RenderShape.INVISIBLE) {
                matrixStackIn.pushPose();
                if (entity.tumbleVec != null) {
                    // spin the block on the x & z axes
                    matrixStackIn.translate(0, 0.5, 0);
                    float angle = ((entity.tickCount + partialTicks) * 18);
                    matrixStackIn.mulPose(Axis.of(entity.tumbleVec).rotationDegrees(angle));
                    matrixStackIn.translate(-0.5, -0.5, -0.5);
                }

                matrixStackIn.translate(0.5, 0.5, 0.5);
                matrixStackIn.scale(2f, 2f, 2f);
                me.desht.pneumaticcraft.client.render.LegacyBER.renderItem(stack, net.minecraft.world.item.ItemDisplayContext.FIXED, matrixStackIn, bufferIn, packedLightIn);
                matrixStackIn.popPose();
            }
        }
    }

    public Identifier getTextureLocation(TumblingBlockEntity entity) {
        return null;
    }
}
