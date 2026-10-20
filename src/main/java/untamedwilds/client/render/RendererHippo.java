package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelHippo;
import untamedwilds.client.model.ModelHippoCalf;
import untamedwilds.entity.mammal.EntityHippo;

import javax.annotation.Nonnull;

public class RendererHippo extends MobRenderer<EntityHippo, EntityModel<EntityHippo>> {

    private final ModelHippo HIPPO_MODEL;
    private final ModelHippoCalf HIPPO_MODEL_CALF;

    public RendererHippo(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ModelHippo(), 1F);
        this.HIPPO_MODEL = (ModelHippo)this.getModel();
        this.HIPPO_MODEL_CALF = new ModelHippoCalf();
    }

    @Override
    public void render(EntityHippo entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        if (entityIn.isBaby()) {
            model = HIPPO_MODEL_CALF;
        } else {
            model = HIPPO_MODEL;
        }
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    protected void scale(EntityHippo entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize();
        //f *= entity.getScale();
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f;
    }

    public @NotNull Identifier getTextureLocation(EntityHippo entity) {
        return entity.getTexture();
    }
}
