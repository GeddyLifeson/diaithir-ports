package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelBison;
import untamedwilds.client.model.ModelBisonCalf;
import untamedwilds.entity.mammal.EntityBison;

import javax.annotation.Nonnull;

public class RendererBison extends MobRenderer<EntityBison, EntityModel<EntityBison>> {

    private final ModelBison BISON_MODEL;
    private final ModelBisonCalf BISON_CALF_MODEL;

    public RendererBison(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ModelBison(), 1F);
        this.BISON_MODEL = (ModelBison)this.getModel();
        this.BISON_CALF_MODEL = new ModelBisonCalf();
    }

    @Override
    public void render(EntityBison entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        model = !entityIn.isBaby() ? BISON_MODEL : BISON_CALF_MODEL;
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    protected void scale(EntityBison entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize();
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f * 0.6F;
    }

    public @NotNull Identifier getTextureLocation(EntityBison entity) {
        return entity.getTexture();
    }
}
