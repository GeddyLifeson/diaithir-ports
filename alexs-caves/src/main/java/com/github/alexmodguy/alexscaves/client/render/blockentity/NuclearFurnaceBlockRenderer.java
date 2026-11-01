package com.github.alexmodguy.alexscaves.client.render.blockentity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.NuclearFurnaceModel;
import com.github.alexmodguy.alexscaves.client.render.ColorUtil;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.server.block.NuclearFurnaceBlock;
import com.github.alexmodguy.alexscaves.server.block.blockentity.NuclearFurnaceBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.github.alexthe666.citadel.client.render.legacy.LegacyBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

public class NuclearFurnaceBlockRenderer<T extends NuclearFurnaceBlockEntity> extends LegacyBlockEntityRenderer<T> {

    private static final NuclearFurnaceModel MODEL = new NuclearFurnaceModel();
    private static final Identifier OFF_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/nuclear_furnace/nuclear_furnace_off.png");
    private static final Identifier ON_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/nuclear_furnace/nuclear_furnace_on.png");
    private static final Identifier SUBCRITICAL_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/nuclear_furnace/nuclear_furnace_subcritical.png");
    private static final Identifier CRITICAL_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/nuclear_furnace/nuclear_furnace_critical.png");
    private static final Identifier SUPERCRITICAL_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/nuclear_furnace/nuclear_furnace_supercritical.png");

    private static final Identifier OFF_TEXTURE_GLOW = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/nuclear_furnace/nuclear_furnace_off_glow.png");
    private static final Identifier ON_TEXTURE_GLOW = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/nuclear_furnace/nuclear_furnace_on_glow.png");
    private static final Identifier SUBCRITICAL_TEXTURE_GLOW = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/nuclear_furnace/nuclear_furnace_subcritical_glow.png");
    private static final Identifier CRITICAL_TEXTURE_GLOW = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/nuclear_furnace/nuclear_furnace_critical_glow.png");
    private static final Identifier SUPERCRITICAL_TEXTURE_GLOW = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/nuclear_furnace/nuclear_furnace_supercritical_glow.png");

    public NuclearFurnaceBlockRenderer(BlockEntityRendererProvider.Context rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(T furnace, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        poseStack.pushPose();
        BlockState state = furnace.getBlockState();
        Direction dir = state.getValue(NuclearFurnaceBlock.FACING);
        poseStack.mulPose(Axis.ZN.rotationDegrees(180));
        poseStack.translate(0.0F, -1.5F, 0.0F);
        MODEL.setupAnim(null, dir.toYRot() - 180, furnace.getCriticality(), furnace.age + partialTicks, furnace.getWasteScale(), 0);
        MODEL.renderToBuffer(poseStack, bufferIn.getBuffer(getRenderTypeFor(furnace, false)), combinedLightIn, combinedOverlayIn, ColorUtil.packColor(1.0F, 1.0F, 1.0F, 1.0F));
        MODEL.renderToBuffer(poseStack, bufferIn.getBuffer(getRenderTypeFor(furnace, true)), 240, combinedOverlayIn, ColorUtil.packColor(1.0F, 1.0F, 1.0F, 1.0F));
        poseStack.popPose();
    }

    public int getViewDistance() {
        return 128;
    }

    private RenderType getRenderTypeFor(T furnace, boolean glow) {
        if (!furnace.isUndergoingFission() && furnace.getCriticality() <= 0) {
            return glow ? ACRenderTypes.getEyesAlphaEnabled(OFF_TEXTURE_GLOW) : RenderTypes.entityCutout(OFF_TEXTURE);
        } else {
            if (furnace.getCriticality() == 1) {
                return glow ? ACRenderTypes.getEyesAlphaEnabled(SUBCRITICAL_TEXTURE_GLOW) : RenderTypes.entityCutout(SUBCRITICAL_TEXTURE);
            } else if (furnace.getCriticality() == 2) {
                return glow ? ACRenderTypes.getEyesAlphaEnabled(CRITICAL_TEXTURE_GLOW) : RenderTypes.entityCutout(CRITICAL_TEXTURE);
            } else if (furnace.getCriticality() >= 3) {
                return glow ? ACRenderTypes.getEyesAlphaEnabled(SUPERCRITICAL_TEXTURE_GLOW) : RenderTypes.entityCutout(SUPERCRITICAL_TEXTURE);
            }
            return glow ? ACRenderTypes.getEyesAlphaEnabled(ON_TEXTURE_GLOW) : RenderTypes.entityCutout(ON_TEXTURE);
        }
    }
}
