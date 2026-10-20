package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelHyena;
import untamedwilds.client.model.ModelRhinoCalf;
import untamedwilds.entity.mammal.EntityHyena;

import javax.annotation.Nonnull;

public class RendererHyena extends MobRenderer<EntityHyena, EntityModel<EntityHyena>> {

    public RendererHyena(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ModelHyena(), 1F);
    }

    /*@Override
    public void render(EntityHyena entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        if (entityIn.isBaby()) {
            model = HYENA_MODEL;
        } else {
            model = HYENA_MODEL;
        }
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }*/

    protected void scale(EntityHyena entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize();
        f *= entity.getScale();
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f * 0.6F;
    }

    public @NotNull Identifier getTextureLocation(EntityHyena entity) {
        return entity.getTexture();
    }
}
