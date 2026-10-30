package com.iafenvoy.jupiter.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.gui.screens.Screen;

public class MinecraftHelper {
    public static void openScreen(Screen screen){
        //? >=26.2 {
        Minecraft.getInstance().setScreenAndShow(screen);
        //?} else {
        //Minecraft.getInstance().setScreen(screen);
        //?}
    }

    public static ToastManager getToastManager(){
        //? >=26.2 {
        return Minecraft.getInstance().gui.toastManager();
        //?} else {
        //return Minecraft.getInstance().getToastManager();
        //?}
    }
}
