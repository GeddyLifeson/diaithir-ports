package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.VesperModel;
import com.github.alexmodguy.alexscaves.server.entity.living.VesperEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer;
import net.minecraft.resources.Identifier;

public class VesperRenderer extends LegacyMobRenderer<VesperEntity, VesperModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/vesper.png");

    public VesperRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new VesperModel(), 0.35F);
    }

    public Identifier getTextureLocation(VesperEntity entity) {
        return TEXTURE;
    }
}


