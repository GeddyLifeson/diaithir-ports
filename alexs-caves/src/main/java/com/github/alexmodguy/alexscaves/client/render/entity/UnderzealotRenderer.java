package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.UnderzealotModel;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.server.entity.living.UnderzealotEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

public class UnderzealotRenderer extends LegacyMobRenderer<UnderzealotEntity, UnderzealotModel> implements CustomBookEntityRenderer {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/underzealot.png");

    private boolean sepia = false;
    public UnderzealotRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new UnderzealotModel(), 0.25F);
    }


    public void render(UnderzealotEntity entity, float f1, float partialTicks, PoseStack poseStack, MultiBufferSource source, int light) {
        this.model.noBurrowing = sepia;
        if(entity.getBuriedProgress(partialTicks) == 1.0F && !sepia){
            return;
        }
        super.render(entity, f1, partialTicks, poseStack, source, light);
    }

    @Nullable
    protected RenderType getRenderType(UnderzealotEntity underzealot, boolean normal, boolean translucent, boolean outline) {
        return sepia ? ACRenderTypes.getBookWidget(TEXTURE, true) : super.getRenderType(underzealot, normal, translucent, outline);
    }

    @Override
    public void setSepiaFlag(boolean sepiaFlag) {
        this.sepia = sepiaFlag;
    }

    public Identifier getTextureLocation(UnderzealotEntity entity) {
        return TEXTURE;
    }
}


