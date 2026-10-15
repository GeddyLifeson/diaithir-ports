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

package me.desht.pneumaticcraft.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.entity.drone.ProgrammerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import me.desht.pneumaticcraft.client.render.legacy.LegacyBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import me.desht.pneumaticcraft.client.render.ItemSubmit;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;

public class ProgrammerRenderer extends LegacyBlockEntityRenderer<ProgrammerBlockEntity> {
    public ProgrammerRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(ProgrammerBlockEntity te, float pPartialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (!te.nonNullLevel().isLoaded(te.getBlockPos())) return;

        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5, 11/16d, 0.5);
        RenderUtils.rotateMatrixForDirection(matrixStackIn, te.getRotation());

        // not sure why negative X/Z offsets are needed for east/west rotation, but ok...
        if (te.getRotation().getAxis() == Direction.Axis.X) {
            matrixStackIn.translate(-0.345, 0.025, -0.28);
        } else {
            matrixStackIn.translate(0.345, 0.025, 0.28);
        }
        // lie the item flat
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(90));
        matrixStackIn.scale(0.25f, 0.25f, 0.25f);

        ItemSubmit.render(te.displayedStack, ItemDisplayContext.FIXED, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);

        matrixStackIn.popPose();
    }
}
