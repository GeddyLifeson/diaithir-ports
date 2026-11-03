package net.diaithir.upgrades.client;

import net.diaithir.upgrades.DiaithirUpgrades;
import net.diaithir.upgrades.Upgrades;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = DiaithirUpgrades.ID, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {}

    @SubscribeEvent
    static void screens(RegisterMenuScreensEvent e) {
        e.register(Upgrades.MENU.get(), UpgradeScreen::new);
    }
}
