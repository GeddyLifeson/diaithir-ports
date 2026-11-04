package me.desht.pneumaticcraft.client.gui.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

/** Helpers to bridge the pre-26.1 primitive input callbacks to the 26.1 input event records. */
public final class GuiCompat {
    private GuiCompat() {}

    public static MouseButtonEvent mouse(double x, double y, int button) {
        return new MouseButtonEvent(x, y, new MouseButtonInfo(button, 0));
    }

    public static KeyEvent key(int key, int scan, int mods) {
        return new KeyEvent(key, scan, mods);
    }

    public static CharacterEvent chr(char c) {
        return new CharacterEvent(c);
    }

    public static boolean shift() {
        return Minecraft.getInstance().hasShiftDown();
    }

    public static boolean ctrl() {
        return Minecraft.getInstance().hasControlDown();
    }

    public static boolean alt() {
        return Minecraft.getInstance().hasAltDown();
    }
}
