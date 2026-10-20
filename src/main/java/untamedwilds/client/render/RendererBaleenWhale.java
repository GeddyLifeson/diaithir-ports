package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelBaleenWhale;
import untamedwilds.entity.mammal.EntityBaleenWhale;

public class RendererBaleenWhale extends MobRenderer<EntityBaleenWhale, EntityModel<EntityBaleenWhale>> {

    public RendererBaleenWhale(EntityRendererProvider.Context rendermanager) {
        super(rendermanager, new ModelBaleenWhale(), 0.2F);
    }
    @Override
    protected void scale(EntityBaleenWhale entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize();
        f *= entity.getScale();
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f;
    }

    public @NotNull Identifier getTextureLocation(EntityBaleenWhale entity) {
        return entity.getTexture();
    }
}
