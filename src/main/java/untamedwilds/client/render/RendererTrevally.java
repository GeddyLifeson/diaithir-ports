package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelTrevally;
import untamedwilds.entity.fish.EntityTrevally;

import javax.annotation.Nonnull;

public class RendererTrevally extends MobRenderer<EntityTrevally, EntityModel<EntityTrevally>> {

    public RendererTrevally(EntityRendererProvider.Context rendermanager) {
        super(rendermanager, new ModelTrevally(), 0.2F);
    }
    @Override
    protected void scale(EntityTrevally entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize() * 0.8F;
        f *= entity.getScale();
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f * 0.5F;
    }

    public @NotNull Identifier getTextureLocation(EntityTrevally entity) {
        return entity.getTexture();
    }
}
