package mcjty.rftoolsutility.modules.environmental.client;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Collections;
import java.util.List;

public class ClientSetup {
    public static void initClient() {
        NeoForge.EVENT_BUS.addListener(EnvironmentalRenderer::renderEnvironmentals);
    }

    public static List<Identifier> onTextureStitch() {
        return Collections.singletonList(EnvironmentalRenderer.HALO);
    }
}
