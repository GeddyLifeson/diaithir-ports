/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.manual;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;

// TODO(oc2-restore): The Markdown Manual (li.cil.manual) is not available for 26.1.2.
// The original registered a ManualModel, a path provider, a document provider and the
// "home", "blocks" and "modules" tabs. Restore once the library is available again.
@OnlyIn(Dist.CLIENT)
public final class Manuals {
    ///////////////////////////////////////////////////////////////////

    public static void initialize(IEventBus modBus) {
        // TODO(oc2-restore): register manual, path providers, document providers and tabs.
    }
}
