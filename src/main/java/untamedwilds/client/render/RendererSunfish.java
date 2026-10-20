package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelSunfish;
import untamedwilds.entity.fish.EntitySunfish;

import javax.annotation.Nonnull;

public class RendererSunfish extends MobRenderer<EntitySunfish, EntityModel<EntitySunfish>> {

    public RendererSunfish(EntityRendererProvider.Context rendermanager) {
        super(rendermanager, new ModelSunfish(), 1F);
    }

    protected void scale(EntitySunfish entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize();
        f *= entity.getScale();
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f;
    }

    public @NotNull Identifier getTextureLocation(EntitySunfish entity) {
        return entity.getTexture();
    }
}
