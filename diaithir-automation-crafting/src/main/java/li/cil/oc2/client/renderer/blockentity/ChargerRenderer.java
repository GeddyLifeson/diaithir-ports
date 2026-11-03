/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.AABB;
import li.cil.oc2.api.API;
import li.cil.oc2.common.blockentity.ChargerBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

public final class ChargerRenderer implements BlockEntityRenderer<ChargerBlockEntity, BlockEntityRenderState> {
    public static final Identifier EFFECT_LOCATION = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/charger/effect");

    ///////////////////////////////////////////////////////////////////

    public ChargerRenderer(final BlockEntityRendererProvider.Context ignoredContext) {
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    public BlockEntityRenderState createRenderState() {
        return new BlockEntityRenderState();
    }

    @Override
    public void submit(final BlockEntityRenderState state, final PoseStack stack, final SubmitNodeCollector collector, final CameraRenderState camera) {
        // TODO(oc2-restore): The animated charger effect (three scaled, unlit quads using EFFECT_LOCATION
        //  from the block atlas, moving up and down) relied on the old MultiBufferSource/Material/
        //  ModRenderType pipeline. Re-implement using SubmitNodeCollector custom geometry.
    }

    public AABB getRenderBoundingBox(final ChargerBlockEntity block) {
        return block.getRenderBoundingBox();
    }
}
