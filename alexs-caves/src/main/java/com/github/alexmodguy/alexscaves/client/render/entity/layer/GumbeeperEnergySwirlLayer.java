package com.github.alexmodguy.alexscaves.client.render.entity.layer;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.GumbeeperModel;
import com.github.alexmodguy.alexscaves.server.entity.living.GumbeeperEntity;
import com.github.alexmodguy.alexscaves.server.entity.living.TremorsaurusEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import com.github.alexthe666.citadel.client.render.legacy.LegacyLayerParent;
import com.github.alexthe666.citadel.client.render.legacy.LegacyEnergySwirlLayer;
import net.minecraft.resources.Identifier;

public class GumbeeperEnergySwirlLayer extends LegacyEnergySwirlLayer<GumbeeperEntity, GumbeeperModel> {
    private static final Identifier POWER_LOCATION = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/gumbeeper_charged.png");
    private final GumbeeperModel model = new GumbeeperModel(1.0F);

    public GumbeeperEnergySwirlLayer(LegacyLayerParent<GumbeeperEntity, GumbeeperModel> renderer) {
        super(renderer);
    }

    protected boolean isPowered(GumbeeperEntity entity) {
        return entity.isPowered();
    }

    protected float xOffset(float f) {
        return f * 0.01F;
    }

    protected Identifier getTextureLocation() {
        return POWER_LOCATION;
    }

    protected GumbeeperModel model() {
        return this.model;
    }
}
