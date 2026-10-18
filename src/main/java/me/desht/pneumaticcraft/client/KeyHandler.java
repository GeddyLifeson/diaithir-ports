/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

import net.minecraft.resources.Identifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public enum KeyHandler {
    INSTANCE;

    private static final String DESCRIPTION_ARMOR_OPTIONS = "pneumaticcraft.armor.options";
    private static final String DESCRIPTION_HELMET_HACK = "pneumaticcraft.helmet.hack";
    private static final String DESCRIPTION_HELMET_DEBUGGING_DRONE = "pneumaticcraft.helmet.debugging.drone";
    private static final String DESCRIPTION_BOOTS_KICK = "pneumaticcraft.boots.kick";
    private static final String DESCRIPTION_LAUNCHER = "pneumaticcraft.chestplate.launcher";
    private static final String DESCRIPTION_JET_BOOTS = "pneumaticcraft.boots.jet_boots";

    public final KeyMapping keybindOpenOptions;
    public final KeyMapping keybindHack;
    public final KeyMapping keybindDebuggingDrone;
    public final KeyMapping keybindKick;
    public final KeyMapping keybindLauncher;
    public final KeyMapping keybindJetBoots;

    private final List<IKeyListener> keyListeners = new ArrayList<>();
    private final List<KeyMapping> knownKeyMappings = new ArrayList<>();

    public static KeyHandler getInstance() {
        return INSTANCE;
    }

    KeyHandler() {
        registerKeyListener(HUDHandler.getInstance());

        keybindOpenOptions = addKnownMapping(new KeyMapping(KeyHandler.DESCRIPTION_ARMOR_OPTIONS, KeyConflictContext.IN_GAME,
                KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_U, category(Names.PNEUMATIC_KEYBINDING_CATEGORY_MAIN)));
        keybindHack = addKnownMapping(new KeyMapping(KeyHandler.DESCRIPTION_HELMET_HACK, KeyConflictContext.IN_GAME,
                KeyModifier.NONE, InputConstants.Type.KEYSYM,  GLFW.GLFW_KEY_H, category(Names.PNEUMATIC_KEYBINDING_CATEGORY_MAIN)));
        keybindDebuggingDrone = addKnownMapping(new KeyMapping(KeyHandler.DESCRIPTION_HELMET_DEBUGGING_DRONE, KeyConflictContext.IN_GAME,
                KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Y, category(Names.PNEUMATIC_KEYBINDING_CATEGORY_MAIN)));
        keybindKick = addKnownMapping(new KeyMapping(KeyHandler.DESCRIPTION_BOOTS_KICK, KeyConflictContext.IN_GAME,
                KeyModifier.CONTROL, InputConstants.Type.KEYSYM,  GLFW.GLFW_KEY_X, category(Names.PNEUMATIC_KEYBINDING_CATEGORY_MAIN)));
        keybindLauncher = addKnownMapping(new KeyMapping(KeyHandler.DESCRIPTION_LAUNCHER, KeyConflictContext.IN_GAME,
                KeyModifier.CONTROL, InputConstants.Type.KEYSYM,  GLFW.GLFW_KEY_C, category(Names.PNEUMATIC_KEYBINDING_CATEGORY_MAIN)));
        keybindJetBoots = addKnownMapping(new KeyMapping(KeyHandler.DESCRIPTION_JET_BOOTS, KeyConflictContext.IN_GAME,
                KeyModifier.NONE, InputConstants.Type.KEYSYM,  GLFW.GLFW_KEY_SPACE, category(Names.PNEUMATIC_KEYBINDING_CATEGORY_MAIN)));
    }

    // Held in a nested class: an enum's own static fields are still null while INSTANCE's constructor calls category().
    private static final class Categories {
        static final Map<String, KeyMapping.Category> MAP = new HashMap<>();
    }

    /**
     * 26.1 key categories are registered objects rather than lang-key strings; map the old string to one, creating it on demand.
     * The lang key for the label is key.category.pneumaticcraft.&lt;path&gt;.
     */
    @SuppressWarnings("deprecation")
    public static KeyMapping.Category category(String oldLangKey) {
        return Categories.MAP.computeIfAbsent(oldLangKey, k -> {
            String prefix = "key." + Names.MOD_ID + ".category.";
            String path = k.startsWith(prefix) ? k.substring(prefix.length()) : k.replaceAll("[^a-z0-9_]", "_").toLowerCase(java.util.Locale.ROOT);
            return KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Names.MOD_ID, path));
        });
    }

    private KeyMapping addKnownMapping(KeyMapping key) {
        knownKeyMappings.add(key);
        return key;
    }

    public void registerKeyMappings(RegisterKeyMappingsEvent event) {
        knownKeyMappings.forEach(event::register);
    }

    @SubscribeEvent
    public void onKey(InputEvent.Key event) {
        if (Minecraft.getInstance().screen == null && event.getAction() == InputConstants.RELEASE) {
            knownKeyMappings.stream().filter(KeyMapping::consumeClick).forEach(this::dispatchInput);
        }
    }

    @SubscribeEvent
    public void onMouse(InputEvent.MouseButton.Post event) {
        if (Minecraft.getInstance().screen == null && event.getAction() == InputConstants.RELEASE) {
            knownKeyMappings.stream().filter(KeyMapping::consumeClick).forEach(this::dispatchInput);
        }
    }

    private void registerKeyListener(IKeyListener listener) {
        keyListeners.add(listener);
    }

    private void dispatchInput(KeyMapping keyMapping) {
        keyListeners.forEach(listener -> listener.handleInput(keyMapping));
    }
}
