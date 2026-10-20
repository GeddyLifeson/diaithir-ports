package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelSpadefish;
import untamedwilds.entity.fish.EntitySpadefish;

public class RendererSpadefish extends MobRenderer<EntitySpadefish, EntityModel<EntitySpadefish>> {

    public RendererSpadefish(EntityRendererProvider.Context rendermanager) {
        super(rendermanager, new ModelSpadefish(), 0.2F);
    }
    @Override
    protected void scale(EntitySpadefish entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize() * 0.8F;
        f *= entity.getScale();
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f * 0.5F;
    }

    public @NotNull Identifier getTextureLocation(EntitySpadefish entity) {
        return entity.getTexture();
    }
}
