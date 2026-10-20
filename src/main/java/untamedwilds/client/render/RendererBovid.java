package untamedwilds.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelBovid;
import untamedwilds.client.render.compat.EntityModel;
import untamedwilds.client.render.compat.MobRenderer;
import untamedwilds.entity.mammal.EntityBovid;

public class RendererBovid extends MobRenderer<EntityBovid, EntityModel<EntityBovid>> {

    public RendererBovid(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ModelBovid(), 1F);
    }

    protected void scale(EntityBovid entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize() * (entity.isBaby() ? 0.55F : 1.0F);
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f * 0.6F;
    }

    public @NotNull Identifier getTextureLocation(EntityBovid entity) {
        return entity.getTexture();
    }
}
