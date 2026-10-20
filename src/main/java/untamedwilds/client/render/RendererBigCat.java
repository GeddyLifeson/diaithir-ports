package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelBigCat;
import untamedwilds.client.model.ModelBigCatCub;
import untamedwilds.entity.mammal.EntityBigCat;

public class RendererBigCat extends MobRenderer<EntityBigCat, EntityModel<EntityBigCat>> {

    private final ModelBigCat BIG_CAT_MODEL;
    private final ModelBigCatCub BIG_CAT_MODEL_CUB;

    public RendererBigCat(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ModelBigCat(), 1F);
        this.BIG_CAT_MODEL = (ModelBigCat)this.getModel();
        this.BIG_CAT_MODEL_CUB = new ModelBigCatCub();
    }

    @Override
    public void render(EntityBigCat entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        if (entityIn.isBaby()) {
            model = BIG_CAT_MODEL_CUB;
        } else {
            model = BIG_CAT_MODEL;
        }
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    protected void scale(EntityBigCat entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize();
        //f *= entity.getScale();
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f;
    }

    @Override
    public @NotNull Identifier getTextureLocation(EntityBigCat entity) {
        return entity.getTexture();
    }
}
