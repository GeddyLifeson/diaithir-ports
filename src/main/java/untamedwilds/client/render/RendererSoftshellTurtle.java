package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelTurtleSoftshell;
import untamedwilds.entity.reptile.EntitySoftshellTurtle;

import javax.annotation.Nonnull;

public class RendererSoftshellTurtle extends MobRenderer<EntitySoftshellTurtle, EntityModel<EntitySoftshellTurtle>> {

    public RendererSoftshellTurtle(EntityRendererProvider.Context rendererManager) {
        super(rendererManager, new ModelTurtleSoftshell(), 0.4F);
    }

    @Override
    protected void scale(EntitySoftshellTurtle entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize() * 0.8F;
        f *= entity.getScale();
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f * 0.4F;
    }

    public @NotNull Identifier getTextureLocation(EntitySoftshellTurtle entity) {
        return entity.getTexture();
    }

}
