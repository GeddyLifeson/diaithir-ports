package net.diaithir.wiretech.circuit;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/** Program editor for a circuit block: a text box, Apply, and live lines for what the block sees on red and green and what it says. */
public final class CircuitScreen extends AbstractContainerScreen<CircuitMenu> {
    private static final int PANEL = 0xFF3A4048, EDGE = 0xFF1B1F24, HI = 0xFF5C646D, TEXT = 0xFFD8DEE4, DIM = 0xFF8D969F, RED = 0xFFE05A4E, GREEN = 0xFF5CCB6A, WARN = 0xFFE8B04A;
    private EditBox program;
    private Button apply;
    private CircuitPayloads.View shown;

    public CircuitScreen(CircuitMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 240, 118);
    }

    @Override
    protected void init() {
        super.init();
        boolean editable = menu.kind != CircuitKind.READER;
        program = new EditBox(font, leftPos + 8, topPos + 20, 224, 16, Component.literal("Program"));
        program.setMaxLength(200);
        program.setValue(menu.view.program());
        program.setEditable(editable);
        addRenderableWidget(program);
        apply = Button.builder(Component.literal("Apply"), b -> send()).bounds(leftPos + 8, topPos + 40, 56, 16).build();
        apply.active = editable;
        addRenderableWidget(apply);
        shown = menu.view;
    }

    @Override
    protected void setInitialFocus() {
        if (menu.kind != CircuitKind.READER) setInitialFocus(program);
    }

    private void send() {
        menu.msg = "";
        ClientPacketDistributor.sendToServer(new CircuitPayloads.Set(menu.pos, program.getValue()));
    }

    /** Called when a refreshed view arrives. */
    public void refreshed() {
        if (menu.view.resetProgram() && menu.msg.equals("Applied.") && !program.isFocused()) program.setValue(menu.view.program());
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) {
            minecraft.player.closeContainer();
            return true;
        }
        if (program.isFocused() && (event.key() == 257 || event.key() == 335)) { // enter
            send();
            return true;
        }
        return program.keyPressed(event) || program.canConsumeInput() || super.keyPressed(event);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        super.extractBackground(g, mouseX, mouseY, a);
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, EDGE);
        g.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + imageHeight - 1, PANEL);
        g.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + 2, HI);
    }

    private String fit(String s, int width) {
        return font.width(s) <= width ? s : font.plainSubstrByWidth(s, width - font.width("...")) + "...";
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mx, int my) {
        CircuitPayloads.View v = menu.view;
        g.text(font, menu.tier.id + " " + menu.kind.display.toLowerCase() + "  (every " + String.format("%.2f", menu.tier.period / 20.0) + " s)", 8, 6, TEXT, false);
        String msg = menu.msg;
        if (!msg.isEmpty() && !msg.equals("Applied.")) g.text(font, fit(msg, 160), 72, 44, WARN, false);
        else if (msg.equals("Applied.")) g.text(font, "Applied.", 70, 44, GREEN, false);
        g.text(font, "red", 8, 62, RED, false);
        g.text(font, fit(v.red(), 200), 32, 62, TEXT, false);
        g.text(font, "green", 8, 74, GREEN, false);
        g.text(font, fit(v.green(), 190), 42, 74, TEXT, false);
        g.text(font, "says", 8, 86, DIM, false);
        g.text(font, fit(v.out(), 190), 38, 86, TEXT, false);
        g.text(font, fit(help(menu.kind), 224), 8, 104, DIM, false);
    }

    private static String help(CircuitKind k) {
        return switch (k) {
            case CONSTANT -> "const a=10 iron_ingot=5";
            case ARITHMETIC -> "arith each * 2 -> each   (+ - * / % ^ << >> and or xor)";
            case DECIDER -> "decide a > 5 and red:b < 3 -> c input";
            case SELECTOR -> "select max 0 | min 1 | count -> n | random 40 | stack";
            case READER -> "Reads the block behind it. No program.";
            default -> "when a > 5   (and/or, each, anything, everything)";
        };
    }
}
