// Ported from Disable Accessibility Screen (MIT) https://github.com/DaFuqs/DisableAccessibilityScreen
// Modifications: the mixin is the whole feature; this class only marks it for the integration list.
package net.diaithir.client.disableaccessibility;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {}
}
