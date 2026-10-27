package com.diaithir.addons.newage.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = "diaithir_create_new_age", dist = Dist.CLIENT)
public class CNAClientMod {
    public CNAClientMod(IEventBus bus) {
        bus.addListener((FMLClientSetupEvent e) -> e.enqueueWork(CNAClient::setup));
    }
}
