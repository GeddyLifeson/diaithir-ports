package net.diaithir.units.fuel.station;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/** While riding a vehicle, holding the jump key (Space) runs /diaithirvehicle shoot every few ticks: fires the vehicle's guns. No new keybind, so nothing to collide. */
final class FireKey {
    private FireKey() {}

    static void init(net.neoforged.bus.api.IEventBus bus) {
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, e -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.screen != null || !mc.player.isPassenger() || !mc.options.keyJump.isDown()) return;
            if (mc.player.tickCount % 3 == 0) mc.player.connection.sendCommand("diaithirvehicle shoot");
        });
    }
}
