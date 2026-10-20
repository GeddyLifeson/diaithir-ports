package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelArowana;
import untamedwilds.entity.fish.EntityArowana;

public class RendererArowana extends MobRenderer<EntityArowana, EntityModel<EntityArowana>> {

    public RendererArowana(EntityRendererProvider.Context rendermanager) {
        super(rendermanager, new ModelArowana(), 0.2F);
    }

    @Override
    protected void scale(EntityArowana entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize() * 0.8F;
        f *= entity.getScale();
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f * 0.5F;
    }

    public @NotNull Identifier getTextureLocation(EntityArowana entity) {
        return entity.getTexture();
    }
}
