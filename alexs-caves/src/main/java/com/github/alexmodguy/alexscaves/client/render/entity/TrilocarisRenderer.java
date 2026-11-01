package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.TrilocarisModel;
import com.github.alexmodguy.alexscaves.server.entity.living.TrilocarisEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer;
import net.minecraft.resources.Identifier;

public class TrilocarisRenderer extends LegacyMobRenderer<TrilocarisEntity, TrilocarisModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/trilocaris.png");

    public TrilocarisRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new TrilocarisModel(), 0.3F);
    }

    protected float getFlipDegrees(TrilocarisEntity centipede) {
        return 180.0F;
    }


    protected void scale(TrilocarisEntity mob, PoseStack matrixStackIn, float partialTicks) {
    }

    public Identifier getTextureLocation(TrilocarisEntity entity) {
        return TEXTURE;
    }
}

