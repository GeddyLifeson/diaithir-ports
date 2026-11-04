package io.github.tr100000.researcher.screen;

import io.github.tr100000.researcher.ClientResearchTracker;
import io.github.tr100000.researcher.Research;
import io.github.tr100000.researcher.networking.StartResearchC2SPacket;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public class StartResearchButton extends AbstractButton {
    private final Identifier researchId;
    private final boolean pin;
    private final int originalX;
    private boolean isCurrent;

    private StartResearchButton(int x, int y, int width, int height, Component text, Identifier researchId, boolean pin, boolean isCurrent) {
        super(x, y, width, height, text);
        this.researchId = researchId;
        this.pin = pin;
        this.originalX = x;
        this.isCurrent = isCurrent;
    }

    public static StartResearchButton create(int x, int y, ClientResearchTracker researchTracker, Research research, boolean pin, boolean isCurrent) {
        StartResearchButton button = new StartResearchButton(x, y, 120, 20, Component.empty(), researchTracker.getIdOrEmpty(research), pin, isCurrent);
        button.updateText(isCurrent);
        return button;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (isCurrent) {
            if (input.hasShiftDown() && Minecraft.getInstance().player.hasInfiniteMaterials()) {
                ClientPacketDistributor.sendToServer(new StartResearchC2SPacket(StartResearchC2SPacket.Mode.REVOKE, Optional.of(researchId)));
            }
            else if (pin) {
                ClientPacketDistributor.sendToServer(new StartResearchC2SPacket(StartResearchC2SPacket.Mode.UNPIN, Optional.of(researchId)));
            }
            else {
                ClientPacketDistributor.sendToServer(new StartResearchC2SPacket(StartResearchC2SPacket.Mode.SET_CURRENT, Optional.empty()));
            }
            isCurrent = false;
        }
        else {
            if (input.hasShiftDown() && Minecraft.getInstance().player.hasInfiniteMaterials()) {
                ClientPacketDistributor.sendToServer(new StartResearchC2SPacket(StartResearchC2SPacket.Mode.GRANT, Optional.of(researchId)));
            }
            else if (pin) {
                ClientPacketDistributor.sendToServer(new StartResearchC2SPacket(StartResearchC2SPacket.Mode.PIN, Optional.of(researchId)));
            }
            else {
                ClientPacketDistributor.sendToServer(new StartResearchC2SPacket(StartResearchC2SPacket.Mode.SET_CURRENT, Optional.of(researchId)));
            }
            isCurrent = true;
        }
        setFocused(false);
        updateText(isCurrent);
    }

    @Override
    protected void extractContents(final GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
        extractDefaultLabel(graphics.textRenderer());
    }

    public void updateText(boolean isCurrent) {
        if (pin) {
            setText(isCurrent ? "unpin" : "pin");
        }
        else {
            setText(isCurrent ? "stop" : "start");
        }
        this.isCurrent = isCurrent;
    }

    private void setText(String key) {
        Component text = Component.translatable("screen.researcher." + key);
        int newWidth = Minecraft.getInstance().font.width(text) + 8;
        setWidth(newWidth);
        setX(originalX - newWidth);
        setMessage(text);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        super.defaultButtonNarrationText(builder);
    }
}
