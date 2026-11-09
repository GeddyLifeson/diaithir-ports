package net.diaithir.units.fuel.station;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Client keybind (default K) that runs /diaithirvehicle shoot: start or stop the engines of the vehicle you are in or beside. */
final class FireKey {
    private static KeyMapping key;

    private FireKey() {}

    static void init(IEventBus bus) {
        bus.addListener(RegisterKeyMappingsEvent.class, e -> {
            key = new KeyMapping("key." + Station.ID + ".fire", InputConstants.Type.KEYSYM, org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE,
                    KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Station.ID, "main")));
            e.register(key);
        });
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, e -> {
            Minecraft mc = Minecraft.getInstance();
            if (key == null || mc.player == null || mc.screen != null) return;
            while (key.consumeClick()) mc.player.connection.sendCommand("diaithirvehicle shoot");
        });
    }
}
