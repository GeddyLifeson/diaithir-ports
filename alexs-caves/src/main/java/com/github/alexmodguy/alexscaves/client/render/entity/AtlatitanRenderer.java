package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.AtlatitanModel;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.client.render.entity.layer.AtlatitanRiderLayer;
import com.github.alexmodguy.alexscaves.server.entity.living.AtlatitanEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.entity.PartEntity;

import javax.annotation.Nullable;

public class AtlatitanRenderer extends LegacyMobRenderer<AtlatitanEntity, AtlatitanModel> implements CustomBookEntityRenderer {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/atlatitan.png");
    private static final Identifier TEXTURE_RETRO = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/atlatitan_retro.png");
    private static final Identifier TEXTURE_TECTONIC = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/atlatitan_tectonic.png");

    private boolean sepia;

    public AtlatitanRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new AtlatitanModel(), 4.0F);
        this.addLayer(new AtlatitanRiderLayer(this));
    }

    protected void scale(AtlatitanEntity mob, PoseStack matrixStackIn, float partialTicks) {
    }


    @Nullable
    protected RenderType getRenderType(AtlatitanEntity mob, boolean normal, boolean translucent, boolean outline) {
        Identifier resourcelocation = this.getTextureLocation(mob);
        if (translucent) {
            return RenderTypes.entityTranslucent(resourcelocation);
        } else if (normal) {
            return sepia ? ACRenderTypes.getBookWidget(resourcelocation, true) : RenderTypes.entityCutout(resourcelocation);
        } else {
            return outline ? RenderTypes.outline(resourcelocation) : null;
        }
    }

    public Identifier getTextureLocation(AtlatitanEntity entity) {
        return entity.getAltSkin() == 2 ? TEXTURE_TECTONIC : entity.getAltSkin() == 1 ? TEXTURE_RETRO : TEXTURE;
    }

    public void render(AtlatitanEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource source, int packedLight) {
        if(sepia){
            this.model.straighten = true;
        }
        super.render(entity, entityYaw, partialTicks, poseStack, source, packedLight);
        if(sepia){
            this.model.straighten = false;
        }
    }

    @Override
    public void setSepiaFlag(boolean sepiaFlag) {
        this.sepia = sepiaFlag;
    }

    public boolean shouldRender(AtlatitanEntity entity, Frustum camera, double x, double y, double z) {
        if (super.shouldRender(entity, camera, x, y, z)) {
            return true;
        } else {
            for (PartEntity part : entity.getParts()) {
                if (camera.isVisible(part.getBoundingBox())) {
                    return true;
                }
            }
            return false;
        }
    }
}

