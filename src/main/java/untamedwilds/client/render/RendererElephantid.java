package untamedwilds.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelElephantid;
import untamedwilds.client.render.compat.EntityModel;
import untamedwilds.client.render.compat.MobRenderer;
import untamedwilds.entity.mammal.EntityElephantid;

public class RendererElephantid extends MobRenderer<EntityElephantid, EntityModel<EntityElephantid>> {

    public RendererElephantid(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ModelElephantid(), 1F);
    }

    protected void scale(EntityElephantid entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize() * (entity.isBaby() ? 0.55F : 1.0F);
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f * 0.6F;
    }

    public @NotNull Identifier getTextureLocation(EntityElephantid entity) {
        return entity.getTexture();
    }
}
