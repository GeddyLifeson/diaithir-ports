package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelCatfish;
import untamedwilds.entity.fish.EntityCatfish;

public class RendererCatfish extends MobRenderer<EntityCatfish, EntityModel<EntityCatfish>> {

    public RendererCatfish(EntityRendererProvider.Context rendermanager) {
        super(rendermanager, new ModelCatfish(), 0.2F);
    }

    @Override
    protected void scale(EntityCatfish entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize() * 0.8F;
        f *= entity.getScale();
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f * 0.5F;
    }

    public @NotNull Identifier getTextureLocation(EntityCatfish entity) {
        return entity.getTexture();
    }
}
