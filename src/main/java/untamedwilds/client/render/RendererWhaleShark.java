package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelWhaleShark;
import untamedwilds.entity.fish.EntityWhaleShark;

public class RendererWhaleShark extends MobRenderer<EntityWhaleShark, EntityModel<EntityWhaleShark>> {

    public RendererWhaleShark(EntityRendererProvider.Context rendermanager) {
        super(rendermanager, new ModelWhaleShark(), 0.2F);
    }
    @Override
    protected void scale(EntityWhaleShark entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize();
        f *= entity.getScale();
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f;
    }

    public @NotNull Identifier getTextureLocation(EntityWhaleShark entity) {
        return entity.getTexture();
    }
}
