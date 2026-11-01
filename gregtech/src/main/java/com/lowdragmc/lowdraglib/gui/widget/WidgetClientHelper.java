package com.lowdragmc.lowdraglib.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/** Client-only helpers split out so dedicated servers never load these classes while verifying Widget. */
@OnlyIn(Dist.CLIENT)
final class WidgetClientHelper {
    private WidgetClientHelper() {}

    static void playButtonClick() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
