package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelGiantClam;
import untamedwilds.entity.mollusk.EntityGiantClam;

import javax.annotation.Nonnull;

public class RendererGiantClam extends MobRenderer<EntityGiantClam, EntityModel<EntityGiantClam>> {

    public RendererGiantClam(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ModelGiantClam(), 1F);
    }

    @Override
    protected void scale(EntityGiantClam entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize();
        f *= entity.getScale();
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f;
    }

    public @NotNull Identifier getTextureLocation(EntityGiantClam entity) {
        return entity.getTexture();
    }
}
