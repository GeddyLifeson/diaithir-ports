package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelOpossum;
import untamedwilds.entity.mammal.EntityOpossum;

public class RendererOpossum extends MobRenderer<EntityOpossum, EntityModel<EntityOpossum>> {

    public RendererOpossum(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ModelOpossum(), 0.4F);
    }

    @Override
    public void render(EntityOpossum entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    protected void scale(EntityOpossum entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize();
        f *= entity.getScale();
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f * 0.4F;
    }

    public @NotNull Identifier getTextureLocation(EntityOpossum entity) {
        return entity.getTexture();
    }
}
