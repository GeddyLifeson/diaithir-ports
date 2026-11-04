/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import li.cil.oc2.common.block.DiskDriveBlock;
import li.cil.oc2.common.blockentity.DiskDriveBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class DiskDriveRenderer implements BlockEntityRenderer<DiskDriveBlockEntity, DiskDriveRenderer.DiskDriveRenderState> {
    public static final class DiskDriveRenderState extends BlockEntityRenderState {
        public final ItemStackRenderState floppy = new ItemStackRenderState();
        public Direction facing = Direction.NORTH;
        public int neighborLight;
    }

    ///////////////////////////////////////////////////////////////////

    public DiskDriveRenderer(final BlockEntityRendererProvider.Context context) {
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    public DiskDriveRenderState createRenderState() {
        return new DiskDriveRenderState();
    }

    @Override
    public void extractRenderState(final DiskDriveBlockEntity diskDrive, final DiskDriveRenderState state, final float partialTicks, final Vec3 cameraPosition, @Nullable final ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(diskDrive, state, partialTicks, cameraPosition, breakProgress);

        final ItemStack floppy = diskDrive.getFloppy();
        final Direction blockFacing = diskDrive.getBlockState().getValue(DiskDriveBlock.FACING);
        state.facing = blockFacing;
        if (diskDrive.getLevel() != null) {
            state.neighborLight = LevelRenderer.getLightCoords(diskDrive.getLevel(), diskDrive.getBlockPos().relative(blockFacing));
        } else {
            state.neighborLight = state.lightCoords;
        }

        Minecraft.getInstance().getItemModelResolver().updateForTopItem(
            state.floppy, floppy, ItemDisplayContext.FIXED, diskDrive.getLevel(), null, (int) diskDrive.getBlockPos().asLong());
    }

    @Override
    public void submit(final DiskDriveRenderState state, final PoseStack stack, final SubmitNodeCollector collector, final CameraRenderState camera) {
        if (state.floppy.isEmpty()) {
            return;
        }

        stack.pushPose();

        stack.translate(0.5f, 0.5f, 0.5f);
        stack.mulPose(Axis.YN.rotationDegrees(state.facing.toYRot()));
        stack.translate(0.0f, 0.0f, 0.5f);
        stack.mulPose(Axis.XN.rotationDegrees(90));
        stack.translate(0.0f, 0.2375f, 2.5f / 16f);
        stack.scale(0.55f, 0.55f, 0.55f);

        state.floppy.submit(stack, collector, state.neighborLight, OverlayTexture.NO_OVERLAY, 0);

        stack.popPose();
    }
}
