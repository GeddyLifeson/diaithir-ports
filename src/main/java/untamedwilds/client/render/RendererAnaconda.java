package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.ModelAnaconda;
import untamedwilds.entity.reptile.EntityAnaconda;

public class RendererAnaconda extends MobRenderer<EntityAnaconda, EntityModel<EntityAnaconda>> {

    public RendererAnaconda(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ModelAnaconda(), 0F);
    }

    protected void scale(EntityAnaconda entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize();
        f *= entity.getScale();
        matrixStackIn.scale(f, f, f);
    }

    public @NotNull Identifier getTextureLocation(EntityAnaconda entity) {
        return entity.getTexture();
    }
}
