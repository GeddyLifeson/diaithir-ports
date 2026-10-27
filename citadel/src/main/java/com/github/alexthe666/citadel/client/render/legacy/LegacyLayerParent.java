package com.github.alexthe666.citadel.client.render.legacy;

import com.github.alexthe666.citadel.client.model.basic.BasicEntityModel;
import net.minecraft.world.entity.Entity;

public interface LegacyLayerParent<T extends Entity, M extends BasicEntityModel<T>> {
    M getModel();
}
