package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelBear;
import untamedwilds.client.model.ModelBearCub;
import untamedwilds.entity.mammal.EntityBear;

public class RendererBear extends MobRenderer<EntityBear, EntityModel<EntityBear>> {

    private final ModelBear BEAR_MODEL;
    private final ModelBearCub BEAR_MODEL_CUB;

    public RendererBear(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ModelBear(), 1F);
        this.BEAR_MODEL = (ModelBear)this.getModel();
        this.BEAR_MODEL_CUB = new ModelBearCub();
    }

    @Override
    public void render(EntityBear entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        if (entityIn.isBaby()) {
            model = BEAR_MODEL_CUB;
        } else {
            model = BEAR_MODEL;
        }
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    protected void scale(EntityBear entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize();
        //f *= entity.getScale();
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f;
    }

    public @NotNull Identifier getTextureLocation(EntityBear entity) {
        return entity.getTexture();
    }
}
