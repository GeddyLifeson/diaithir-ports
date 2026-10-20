package untamedwilds.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import untamedwilds.UntamedWilds;
import untamedwilds.client.render.compat.AMRenderState;
import untamedwilds.client.render.compat.EntityModel;
import untamedwilds.client.render.compat.RenderLayer;

public class LayerFootballFishLure<T extends Entity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    private final RenderType TEXTURE = net.minecraft.client.renderer.rendertype.RenderTypes.eyes(Identifier.parse(UntamedWilds.MOD_ID + ":textures/entity/football_fish/glint.png"));

    public LayerFootballFishLure(RenderLayerParent<AMRenderState, M> rendererIn) {
        super(rendererIn);
    }

    public RenderType renderType() {
        return TEXTURE;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        this.getParentModel().renderToBuffer(poseStack, bufferSource.getBuffer(this.renderType()), 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
