package net.id.paradise_lost.clientstub;

import net.id.paradise_lost.ModConstants;
import net.id.paradise_lost.registry.EntityRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/**
 * PORT NOTE (stub): minimal client entrypoint. The original Paradise Lost client rendering stack was written
 * against the pre-26.1 render pipeline and is excluded from the build (see neoforge/build.gradle.kts).
 * Until it is ported, entities are registered with an invisible renderer so the client can load worlds.
 */
@Mod(value = ModConstants.MOD_ID, dist = Dist.CLIENT)
public class ParadiseLostClientStub {
    public ParadiseLostClientStub(IEventBus modBus, ModContainer modContainer) {
        modBus.addListener(ClientStubEvents::registerRenderers);
    }
}
