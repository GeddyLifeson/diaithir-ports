package net.diaithir.forgeworks;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = DiaithirForgeworks.ID, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {}

    @SubscribeEvent
    static void screens(RegisterMenuScreensEvent e) {
        e.register(Machines.MENU.get(), MachineScreen::new);
    }
}
