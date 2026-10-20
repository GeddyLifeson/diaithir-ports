package untamedwilds.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelGroundSloth;
import untamedwilds.client.render.compat.EntityModel;
import untamedwilds.client.render.compat.MobRenderer;
import untamedwilds.entity.mammal.EntityGroundSloth;

public class RendererGroundSloth extends MobRenderer<EntityGroundSloth, EntityModel<EntityGroundSloth>> {

    public RendererGroundSloth(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ModelGroundSloth(), 1F);
    }

    protected void scale(EntityGroundSloth entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize() * (entity.isBaby() ? 0.55F : 1.0F);
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f * 0.6F;
    }

    public @NotNull Identifier getTextureLocation(EntityGroundSloth entity) {
        return entity.getTexture();
    }
}
