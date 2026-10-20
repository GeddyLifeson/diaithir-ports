package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelTortoise;
import untamedwilds.entity.reptile.EntityTortoise;

import javax.annotation.Nonnull;

public class RendererTortoise extends MobRenderer<EntityTortoise, EntityModel<EntityTortoise>> {

    public RendererTortoise(EntityRendererProvider.Context rendererManager) {
        super(rendererManager, new ModelTortoise(), 0.4F);
    }

    @Override
    protected void scale(EntityTortoise entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize() * 0.8F;
        f *= entity.getScale();
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f * 0.4F;
    }

    public @NotNull Identifier getTextureLocation(EntityTortoise entity) {
        return entity.getTexture();
    }

}
