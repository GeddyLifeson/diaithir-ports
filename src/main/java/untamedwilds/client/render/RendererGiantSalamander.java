package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelGiantSalamander;
import untamedwilds.entity.amphibian.EntityGiantSalamander;

import javax.annotation.Nonnull;

public class RendererGiantSalamander extends MobRenderer<EntityGiantSalamander, EntityModel<EntityGiantSalamander>> {

    public RendererGiantSalamander(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ModelGiantSalamander(), 0.2F);
    }

    @Override
    protected void scale(EntityGiantSalamander entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize();
        f *= entity.getScale();
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f * 0.4F;
    }

    public @NotNull Identifier getTextureLocation(EntityGiantSalamander entity) {
        return entity.getTexture();
    }
}
