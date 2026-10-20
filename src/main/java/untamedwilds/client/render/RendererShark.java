package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelShark;
import untamedwilds.entity.fish.EntityShark;

import javax.annotation.Nonnull;

public class RendererShark extends MobRenderer<EntityShark, EntityModel<EntityShark>> {

    public RendererShark(EntityRendererProvider.Context rendermanager) {
        super(rendermanager, new ModelShark(), 0.2F);
    }
    @Override
    protected void scale(EntityShark entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize();
        f *= entity.getScale();
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f;
    }

    public @NotNull Identifier getTextureLocation(EntityShark entity) {
        return entity.getTexture();
    }
}
