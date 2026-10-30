package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.CaniacModel;
import com.github.alexmodguy.alexscaves.client.render.entity.layer.LicowitchPossessionLayer;
import com.github.alexmodguy.alexscaves.server.entity.living.CaniacEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer;
import net.minecraft.resources.Identifier;

public class CaniacRenderer extends LegacyMobRenderer<CaniacEntity, CaniacModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/caniac.png");

    public CaniacRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new CaniacModel(), 0.65F);
        this.addLayer(new LicowitchPossessionLayer(this));
    }

    public Identifier getTextureLocation(CaniacEntity entity) {
        return TEXTURE;
    }
}


