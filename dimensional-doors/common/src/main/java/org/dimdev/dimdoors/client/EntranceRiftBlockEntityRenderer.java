package org.dimdev.dimdoors.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.Vec3;
import org.dimdev.dimdoors.api.client.DefaultTransformation;
import org.dimdev.dimdoors.api.client.DimensionalPortalRenderer;
import org.dimdev.dimdoors.api.client.Transformer;
import org.dimdev.dimdoors.block.door.DimensionalTrapDoorBlock;
import org.dimdev.dimdoors.block.entity.EntranceRiftBlockEntity;
import org.jspecify.annotations.Nullable;

/**
 * The door model itself is rendered by the normal block model; this only adds the portal surface.
 */
public class EntranceRiftBlockEntityRenderer extends RiftBlockEntityRenderer<EntranceRiftBlockEntity, EntranceRiftRenderState> {
    public EntranceRiftBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void extractRenderState(EntranceRiftBlockEntity blockEntity, EntranceRiftRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.renderBlockState = blockEntity.getRenderBlockState();
        state.transformer = getTransformer(blockEntity);
        state.tall = blockEntity.isTall();
    }

    @Override
    public void submit(EntranceRiftRenderState state, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, matrices, collector, camera);
        DimensionalPortalRenderer.renderDimensionalPortal(collector, matrices, state.transformer, state.tall);
    }

    @Override
    public EntranceRiftRenderState createRenderState() {
        return new EntranceRiftRenderState();
    }

    public static Transformer getTransformer(EntranceRiftBlockEntity blockEntity) {
        if (blockEntity.getBlockState().getBlock() instanceof DimensionalTrapDoorBlock) {
            return blockEntity.getBlockState().getValue(TrapDoorBlock.HALF) == Half.TOP ? DefaultTransformation.TOP_TRAPDOOR : DefaultTransformation.BOTTOMM_TRAPDOOR;
        }

        return DefaultTransformation.fromDirection(blockEntity.getOrientation());
    }
}
