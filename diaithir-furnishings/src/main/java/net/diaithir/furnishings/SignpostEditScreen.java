package net.diaithir.furnishings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.KeyEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/** Two lines of text for one arm. Plain vanilla widgets, so the player's Age skin applies. */
final class SignpostEditScreen extends Screen {
    private final BlockPos pos;
    private final int slot;
    private final String[] initial = {"", ""};
    private EditBox first;
    private EditBox second;
    private boolean sent;

    SignpostEditScreen(BlockPos pos, int slot) {
        super(Component.translatable("gui.diaithirfurnishings.signpost"));
        this.pos = pos;
        this.slot = slot;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.getBlockEntity(pos) instanceof SignpostEntity post && post.arm(slot) != null) {
            initial[0] = post.arm(slot).line1();
            initial[1] = post.arm(slot).line2();
        }
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 100;
        first = new EditBox(this.font, x, this.height / 2 - 30, 200, 20, Component.translatable("gui.diaithirfurnishings.signpost.line1"));
        second = new EditBox(this.font, x, this.height / 2 - 4, 200, 20, Component.translatable("gui.diaithirfurnishings.signpost.line2"));
        for (EditBox box : new EditBox[] {first, second}) {
            box.setMaxLength(SignpostEntity.MAX_LINE);
            this.addRenderableWidget(box);
        }
        first.setValue(initial[0]);
        second.setValue(initial[1]);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.onClose())
            .pos(this.width / 2 - 50, this.height / 2 + 26).size(100, 20).build());
        this.setInitialFocus(first);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isConfirmation() && first.isFocused()) {
            this.setFocused(second);
            return true;
        }
        if (event.isConfirmation()) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        graphics.centeredText(this.font, this.title, this.width / 2, this.height / 2 - 52, 0xFFFFFFFF);
    }

    @Override
    public void removed() {
        if (!sent) {
            sent = true;
            ClientPacketDistributor.sendToServer(new SetArmText(pos, slot, first.getValue(), second.getValue()));
        }
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
