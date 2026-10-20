package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelManatee;
import untamedwilds.entity.mammal.EntityManatee;

public class RendererManatee extends MobRenderer<EntityManatee, EntityModel<EntityManatee>> {

    private final ModelManatee MANATEE_MODEL;
    private final ModelManatee MANATEE_MODEL_CALF;

    public RendererManatee(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ModelManatee(), 1F);
        this.MANATEE_MODEL = (ModelManatee)this.getModel();
        this.MANATEE_MODEL_CALF = new ModelManatee();
    }

    /*@Override
    public void render(EntityManatee entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        if (entityIn.isBaby()) {
            model = MANATEE_MODEL_CALF;
        } else {
            model = MANATEE_MODEL;
        }
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }*/

    protected void scale(EntityManatee entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize();
        f *= entity.getScale();
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f;
    }

    public @NotNull Identifier getTextureLocation(EntityManatee entity) {
        return entity.getTexture();
    }
}
