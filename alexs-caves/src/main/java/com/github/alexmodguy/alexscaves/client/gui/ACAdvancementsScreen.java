package com.github.alexmodguy.alexscaves.client.gui;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import org.jspecify.annotations.Nullable;

/** Advancements screen that swaps in ACAdvancementTab for Alex's Caves trees (no mixins needed). */
public class ACAdvancementsScreen extends AdvancementsScreen {
    public ACAdvancementsScreen(ClientAdvancements advancements, @Nullable Screen lastScreen) {
        super(advancements, lastScreen);
    }

    @Override
    public void onAddAdvancementRoot(AdvancementNode root) {
        super.onAddAdvancementRoot(root);
        AdvancementTab made = tabs.get(root.holder());
        if (made != null && root.holder().id().getNamespace().equals(AlexsCaves.MODID)) {
            tabs.put(root.holder(), new ACAdvancementTab(minecraft, this, made.getType(), made.getIndex(), made.getPage(), root, made.getDisplay()));
        }
    }

    @Override
    public void tick() {
        super.tick();
        ACAdvancementTabs.tick();
    }
}
