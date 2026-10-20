package untamedwilds.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import untamedwilds.client.render.compat.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import untamedwilds.client.render.compat.MobRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import untamedwilds.client.model.MonsterSpitter;
import untamedwilds.client.model.MonsterSpitterLarva;
import untamedwilds.entity.relict.EntitySpitter;

public class RendererSpitter extends MobRenderer<EntitySpitter, EntityModel<EntitySpitter>> {

    private final MonsterSpitter SPITTER_MODEL;
    private final MonsterSpitterLarva SPITTER_MODEL_LARVA;

    public RendererSpitter(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MonsterSpitter(), 1F);
        this.SPITTER_MODEL = (MonsterSpitter)this.getModel();
        this.SPITTER_MODEL_LARVA = new MonsterSpitterLarva();
    }

    @Override
    public void render(EntitySpitter entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        if (entityIn.isBaby()) {
            model = SPITTER_MODEL_LARVA;
        } else {
            model = SPITTER_MODEL;
        }
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    protected void scale(EntitySpitter entity, PoseStack matrixStackIn, float partialTickTime) {
        float f = entity.getMobSize();
        //f *= entity.getScale();
        matrixStackIn.scale(f, f, f);
        this.shadowRadius = f;
    }

    @Override
    public @NotNull Identifier getTextureLocation(EntitySpitter entity) {
        return entity.getTexture();
    }
}
