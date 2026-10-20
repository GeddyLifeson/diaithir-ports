package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelTarantula;
import untamedwilds.entity.arthropod.EntityTarantula;

import javax.annotation.Nonnull;

public class RendererTarantula extends MobRenderer<EntityTarantula, ModelTarantula> {

    public RendererTarantula(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ModelTarantula(), 0.2F);
    }

    @Override
    protected void scale(EntityTarantula entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize() * 0.7F;
        f *= entity.getScale();
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f * 0.6F;
    }

    public @NotNull Identifier getTextureLocation(EntityTarantula entity) {
        return entity.getTexture();
    }
}
