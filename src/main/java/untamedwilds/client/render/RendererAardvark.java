package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelAardvark;
import untamedwilds.entity.mammal.EntityAardvark;

public class RendererAardvark extends MobRenderer<EntityAardvark, EntityModel<EntityAardvark>> {

    public RendererAardvark(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ModelAardvark(), 0.4F);
    }

    protected void scale(EntityAardvark entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize();
        f *= entity.getScale();
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f * 0.6F;
    }

    public @NotNull Identifier getTextureLocation(EntityAardvark entity) {
        return entity.getTexture();
    }
}
