/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common;

import li.cil.oc2.common.bus.device.rpc.RPCMethodParameterTypeAdapters;
import li.cil.oc2.common.inet.InternetManagerImpl;
import li.cil.oc2.common.util.ServerScheduler;
import li.cil.oc2.common.vxlan.TunnelManager;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.bus.api.SubscribeEvent;

public final class CommonSetup {
    @SubscribeEvent
    public static void handleSetupEvent(final FMLCommonSetupEvent event) {
        // TODO(oc2-restore): Integrations.initialize(); (li.cil.oc2.common.integration excluded from compile stage)
        InternetManagerImpl.initialize();
        RPCMethodParameterTypeAdapters.initialize();
        ServerScheduler.initialize();
        TunnelManager.initialize();
    }
}
