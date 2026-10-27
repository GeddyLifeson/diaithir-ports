package net.id.paradise_lost.services;

import net.id.paradise_lost.platform.services.IClientHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * PORT NOTE: in 26.1 block render layers come from the block model JSON ("render_type"), so the
 * programmatic render layer calls are no-ops. Control-key polling needs client classes and is
 * resolved reflectively so this class stays loadable on a dedicated server.
 */
public class NeoForgeClientHelper implements IClientHelper {
    @Override
    public void setRenderLayerCutout(Object block) {
    }

    @Override
    public void setRenderLayerCutoutMipped(Object block) {
    }

    @Override
    public void setRenderLayerTranslucent(Object block) {
    }

    @Override
    public boolean isControlKeyDown() {
        if (FMLEnvironment.getDist() != Dist.CLIENT) {
            return false;
        }
        try {
            Class<?> screen = Class.forName("net.minecraft.client.gui.screens.Screen");
            return (boolean) screen.getMethod("hasControlDown").invoke(null);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
}
