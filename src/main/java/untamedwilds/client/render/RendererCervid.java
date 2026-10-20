package untamedwilds.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelCervid;
import untamedwilds.client.render.compat.EntityModel;
import untamedwilds.client.render.compat.MobRenderer;
import untamedwilds.entity.mammal.EntityCervid;

public class RendererCervid extends MobRenderer<EntityCervid, EntityModel<EntityCervid>> {

    public RendererCervid(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ModelCervid(), 1F);
    }

    protected void scale(EntityCervid entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize() * (entity.isBaby() ? 0.55F : 1.0F);
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f * 0.6F;
    }

    public @NotNull Identifier getTextureLocation(EntityCervid entity) {
        return entity.getTexture();
    }
}
