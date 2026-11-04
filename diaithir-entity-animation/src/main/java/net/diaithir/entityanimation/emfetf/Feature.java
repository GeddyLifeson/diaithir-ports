// Ported from Entity Texture Features and Entity Model Features by Traben (LGPL-3.0-only)
// https://github.com/Traben-0/Entity_Texture_Features , https://github.com/Traben-0/Entity_Model_Features (26.1 sources, preprocessor evaluated for 26.1.2 NeoForge)
// Modifications: @Mod entry points replaced by this Feature; upstream packages kept for their mixin configs and plugins; Iris/Sodium/3D-skin-layer/Player-Animation-Library compat mixins disabled.
package net.diaithir.entityanimation.emfetf;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        new traben.entity_texture_features.ETFInit();
        new traben.entity_model_features.EMFInit();
    }
}
