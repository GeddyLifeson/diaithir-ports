package com.github.alexmodguy.alexscaves.client.render.entity.layer;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.NucleeperModel;
import com.github.alexmodguy.alexscaves.server.entity.living.NucleeperEntity;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import com.github.alexthe666.citadel.client.render.legacy.LegacyLayerParent;
import com.github.alexthe666.citadel.client.render.legacy.LegacyEnergySwirlLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.Creeper;

public class NucleeperEnergySwirlLayer  extends LegacyEnergySwirlLayer<NucleeperEntity, NucleeperModel> {
    private static final Identifier POWER_LOCATION = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/nucleeper/nucleeper_charged.png");
    private final NucleeperModel model = new NucleeperModel(1.0F);

    public NucleeperEnergySwirlLayer(LegacyLayerParent<NucleeperEntity, NucleeperModel> renderer) {
        super(renderer);
    }

    protected boolean isPowered(NucleeperEntity entity) {
        return entity.isPowered();
    }

    protected float xOffset(float f) {
        return f * 0.01F;
    }

    protected Identifier getTextureLocation() {
        return POWER_LOCATION;
    }

    protected NucleeperModel model() {
        return this.model;
    }
}
