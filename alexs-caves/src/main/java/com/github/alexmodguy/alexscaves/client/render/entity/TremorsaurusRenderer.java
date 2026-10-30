package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.TremorsaurusModel;
import com.github.alexmodguy.alexscaves.client.render.entity.layer.TremorsaurusHeldMobLayer;
import com.github.alexmodguy.alexscaves.client.render.entity.layer.TremorsaurusRiderLayer;
import com.github.alexmodguy.alexscaves.server.entity.living.TremorsaurusEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer;
import net.minecraft.resources.Identifier;

public class TremorsaurusRenderer extends LegacyMobRenderer<TremorsaurusEntity, TremorsaurusModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/tremorsaurus.png");
    private static final Identifier TEXTURE_PRINCESS = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/tremorsaurus_princess.png");
    private static final Identifier TEXTURE_RETRO = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/tremorsaurus_retro.png");
    private static final Identifier TEXTURE_TECTONIC = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/tremorsaurus_tectonic.png");

    public TremorsaurusRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new TremorsaurusModel(), 1.1F);
        this.addLayer(new TremorsaurusRiderLayer(this));
        this.addLayer(new TremorsaurusHeldMobLayer(this));
    }

    protected void scale(TremorsaurusEntity mob, PoseStack matrixStackIn, float partialTicks) {
    }

    public Identifier getTextureLocation(TremorsaurusEntity entity) {
        return entity.hasCustomName() && "princess".equalsIgnoreCase(entity.getName().getString()) ? TEXTURE_PRINCESS : entity.getAltSkin() == 1 ? TEXTURE_RETRO : entity.getAltSkin() == 2 ? TEXTURE_TECTONIC : TEXTURE;
    }


}

