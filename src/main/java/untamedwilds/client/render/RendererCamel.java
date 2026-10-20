package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelCamel;
import untamedwilds.client.model.ModelCamelCalf;
import untamedwilds.entity.mammal.EntityCamel;

public class RendererCamel extends MobRenderer<EntityCamel, EntityModel<EntityCamel>> {

    private final ModelCamel CAMEL_MODEL;
    private final ModelCamelCalf CAMEL_CALF_MODEL;

    public RendererCamel(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ModelCamel(), 1F);
        this.CAMEL_MODEL = (ModelCamel)this.getModel();
        this.CAMEL_CALF_MODEL = new ModelCamelCalf();
    }

    @Override
    public void render(EntityCamel entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        model = !entityIn.isBaby() ? CAMEL_MODEL : CAMEL_CALF_MODEL;
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    protected void scale(EntityCamel entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize() * (entity.isBaby() ? 0.7F : 1F);
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f * 0.9F;
    }

    public @NotNull Identifier getTextureLocation(EntityCamel entity) {
        return entity.getTexture();
    }
}
