package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.RelicheirusModel;
import com.github.alexmodguy.alexscaves.client.render.entity.layer.RelicheirusHeldTrilocarisLayer;
import com.github.alexmodguy.alexscaves.server.entity.living.RelicheirusEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer;
import net.minecraft.resources.Identifier;

public class RelicheirusRenderer extends LegacyMobRenderer<RelicheirusEntity, RelicheirusModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/relicheirus.png");
    private static final Identifier TEXTURE_RETRO = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/relicheirus_retro.png");
    private static final Identifier TEXTURE_TECTONIC = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/relicheirus_tectonic.png");

    public RelicheirusRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new RelicheirusModel(), 1.0F);
        this.addLayer(new RelicheirusHeldTrilocarisLayer(this));
    }

    protected void scale(RelicheirusEntity mob, PoseStack matrixStackIn, float partialTicks) {
    }

    public Identifier getTextureLocation(RelicheirusEntity entity) {
        return entity.getAltSkin() == 2 ? TEXTURE_TECTONIC : entity.getAltSkin() == 1 ? TEXTURE_RETRO : TEXTURE;
    }
}

