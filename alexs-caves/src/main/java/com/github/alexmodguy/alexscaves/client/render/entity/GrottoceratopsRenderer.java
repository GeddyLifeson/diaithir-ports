package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.GrottoceratopsModel;
import com.github.alexmodguy.alexscaves.server.entity.living.GrottoceratopsEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer;
import net.minecraft.resources.Identifier;

public class GrottoceratopsRenderer extends LegacyMobRenderer<GrottoceratopsEntity, GrottoceratopsModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/grottoceratops.png");
    private static final Identifier TEXTURE_BABY = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/grottoceratops_baby.png");
    private static final Identifier TEXTURE_RETRO = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/grottoceratops_retro.png");
    private static final Identifier TEXTURE_RETRO_BABY = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/grottoceratops_retro_baby.png");
    private static final Identifier TEXTURE_TECTONIC = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/grottoceratops_tectonic.png");
    private static final Identifier TEXTURE_TECTONIC_BABY = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/grottoceratops_tectonic_baby.png");

    public GrottoceratopsRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new GrottoceratopsModel(), 1.1F);
    }

    protected void scale(GrottoceratopsEntity mob, PoseStack matrixStackIn, float partialTicks) {
    }

    public Identifier getTextureLocation(GrottoceratopsEntity entity) {
        return entity.getAltSkin() == 1 ? entity.isBaby() ? TEXTURE_RETRO_BABY : TEXTURE_RETRO : entity.getAltSkin() == 2 ? entity.isBaby() ? TEXTURE_TECTONIC_BABY : TEXTURE_TECTONIC : entity.isBaby() ? TEXTURE_BABY : TEXTURE;
    }
}

