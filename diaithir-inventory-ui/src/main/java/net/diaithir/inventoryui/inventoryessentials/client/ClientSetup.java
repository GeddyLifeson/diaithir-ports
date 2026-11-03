package net.diaithir.inventoryui.inventoryessentials.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import org.lwjgl.glfw.GLFW;

public final class ClientSetup {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
            net.minecraft.resources.Identifier.fromNamespaceAndPath("inventoryui", "inventoryui"));
    public static final KeyMapping SORT = new KeyMapping("key.inventoryui.sort", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);
    public static final KeyMapping DRAG = new KeyMapping("key.inventoryui.drag", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, CATEGORY);
    public static final KeyMapping LOCK = new KeyMapping("key.inventoryui.lock", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_L, CATEGORY);
    public static final KeyMapping MOVE_ALL = new KeyMapping("key.inventoryui.move_all", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_CONTROL, CATEGORY);

    private ClientSetup() {}

    public static void init(IEventBus modBus, ModContainer container) {
        modBus.addListener(ClientSetup::registerKeys);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    private static void registerKeys(RegisterKeyMappingsEvent e) {
        e.registerCategory(CATEGORY);
        e.register(SORT);
        e.register(DRAG);
        e.register(LOCK);
        e.register(MOVE_ALL);
    }
}
