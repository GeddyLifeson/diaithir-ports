package com.lowdragmc.lowdraglib.gui.factory;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/** Client-only half of {@link UIFactory#initClientUI}, kept separate so dedicated servers never load client classes. */
@OnlyIn(Dist.CLIENT)
final class UIFactoryClient {
    private UIFactoryClient() {}

    static void open(UIFactory<?> factory, RegistryFriendlyByteBuf serializedHolder, int windowId) {
        Object holder = factory.readHolderErased(serializedHolder);
        Minecraft minecraft = Minecraft.getInstance();
        ModularUI uiTemplate = factory.createTemplateErased(holder, minecraft.player);
        if (uiTemplate == null) return;
        uiTemplate.initWidgets();
        ModularUIGuiContainer container = new ModularUIGuiContainer(uiTemplate, windowId);
        uiTemplate.mainGroup.readInitialData(serializedHolder);
        minecraft.setScreen(container);
        minecraft.player.containerMenu = container.getMenu();
    }
}
