package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.layer.LayerFootballFishLure;
import untamedwilds.client.model.ModelFootballFish;
import untamedwilds.entity.fish.EntityFootballFish;

import javax.annotation.Nonnull;

public class RendererFootballFish extends MobRenderer<EntityFootballFish, EntityModel<EntityFootballFish>> {

    public RendererFootballFish(EntityRendererProvider.Context rendermanager) {
        super(rendermanager, new ModelFootballFish(), 0.6F);
        this.addLayer(new LayerFootballFishLure<>(this));
    }

    protected void scale(EntityFootballFish entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize() * 0.8F;
        f *= entity.getScale();
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f * 0.6F;
    }

    public @NotNull Identifier getTextureLocation(EntityFootballFish entity) {
        return entity.getTexture();
    }
}
