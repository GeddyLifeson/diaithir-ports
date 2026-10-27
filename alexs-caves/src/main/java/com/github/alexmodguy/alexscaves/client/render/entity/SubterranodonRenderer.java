package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.SubterranodonModel;
import com.github.alexmodguy.alexscaves.client.render.entity.layer.SubterranodonRiderLayer;
import com.github.alexmodguy.alexscaves.server.entity.living.SubterranodonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer;
import net.minecraft.resources.Identifier;

public class SubterranodonRenderer extends LegacyMobRenderer<SubterranodonEntity, SubterranodonModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/subterranodon.png");
    private static final Identifier TEXTURE_RETRO = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/subterranodon_retro.png");
    private static final Identifier TEXTURE_TECTONIC = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/subterranodon_tectonic.png");

    public SubterranodonRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new SubterranodonModel(), 0.5F);
        this.addLayer(new SubterranodonRiderLayer(this));

    }

    public Identifier getTextureLocation(SubterranodonEntity entity) {
        return entity.getAltSkin() == 1 ? TEXTURE_RETRO : entity.getAltSkin() == 2 ? TEXTURE_TECTONIC : TEXTURE;
    }
}

