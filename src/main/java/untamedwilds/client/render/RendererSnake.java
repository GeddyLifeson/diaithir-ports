package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelSnake;
import untamedwilds.entity.reptile.EntitySnake;

import javax.annotation.Nonnull;

public class RendererSnake extends MobRenderer<EntitySnake, EntityModel<EntitySnake>> {

    public RendererSnake(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ModelSnake(), 0F);
    }

    protected void scale(EntitySnake entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize();
        f *= entity.getScale();
        matrixStackIn.scale(f, f, f);
    }

    public @NotNull Identifier getTextureLocation(EntitySnake entity) {
        return entity.getTexture();
    }
}
