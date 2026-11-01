package net.diaithir.wiretech;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/** Draws the machine GUI from flat fills: FE bar, progress arrow, tank, redstone button and the six-face side config. */
public final class MachineScreen extends AbstractContainerScreen<MachineMenu> {
    private static final int PANEL = 0xFF3A4048, EDGE = 0xFF1B1F24, HI = 0xFF5C646D, SLOT = 0xFF14171A, TEXT = 0xFFD8DEE4, DIM = 0xFF8D969F;
    private static final int[] MODE_COL = {0xFF2B2F33, 0xFF3F7FD0, 0xFFD98A2B, 0xFF4FAF63};
    private static final String[] MODE_NAME = {"closed", "in", "out", "in and out"};
    private static final String[] RS_NAME = {"ignored", "needs a signal", "stops on a signal"};
    private static final String FACE_LETTER = "DUNSWE";
    private static final Direction[] FACES = Direction.values(); // DOWN UP NORTH SOUTH WEST EAST
    private static final int[] GRID_ORDER = {1, 2, 5, 0, 3, 4}; // row 0: up north east; row 1: down south west
    private static final int GX = 120, GY = 20, CELL = 14, SQ = 12;
    private static final int FE_X = 8, FE_Y = 18, FE_W = 10, FE_H = 58;
    private static final int RS_X = 120, RS_Y = 54;
    private static final int ARROW_X = 54, ARROW_Y = 40, ARROW_W = 26;
    private static final int TANK_X = 86, TANK_Y = 20, TANK_W = 18, TANK_H = 52;

    public MachineScreen(MachineMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 176, 166);
    }

    private static void box(GuiGraphicsExtractor g, int x, int y, int w, int h, int fill, int edge) {
        g.fill(x, y, x + w, y + h, edge);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        super.extractBackground(g, mouseX, mouseY, a);
        int x = leftPos, y = topPos;
        box(g, x, y, imageWidth, imageHeight, PANEL, EDGE);
        g.fill(x + 1, y + 1, x + imageWidth - 1, y + 2, HI);
        for (Slot s : menu.slots) box(g, x + s.x - 1, y + s.y - 1, 18, 18, SLOT, EDGE);

        // FE bar
        box(g, x + FE_X, y + FE_Y, FE_W, FE_H, SLOT, EDGE);
        int cap = Math.max(1, menu.capacity());
        int fill = (int) ((long) (FE_H - 2) * menu.energy() / cap);
        g.fill(x + FE_X + 1, y + FE_Y + FE_H - 1 - fill, x + FE_X + FE_W - 1, y + FE_Y + FE_H - 1, 0xFFD9B13B);
        g.fill(x + FE_X + 1, y + FE_Y + FE_H - 1 - fill, x + FE_X + 3, y + FE_Y + FE_H - 1, 0xFFF2D75F);

        // progress arrow
        box(g, x + ARROW_X, y + ARROW_Y, ARROW_W, 8, SLOT, EDGE);
        int pw = (ARROW_W - 2) * menu.progressPermille() / 1000;
        g.fill(x + ARROW_X + 1, y + ARROW_Y + 1, x + ARROW_X + 1 + pw, y + ARROW_Y + 7, 0xFFE0762A);

        // tank
        if (menu.kind.fluid) {
            box(g, x + TANK_X, y + TANK_Y, TANK_W, TANK_H, SLOT, EDGE);
            int fcap = Math.max(1, menu.fluidCapacity());
            int h = (int) ((long) (TANK_H - 2) * menu.fluidAmount() / fcap);
            g.fill(x + TANK_X + 1, y + TANK_Y + TANK_H - 1 - h, x + TANK_X + TANK_W - 1, y + TANK_Y + TANK_H - 1, fluidColor());
        }

        // redstone button
        box(g, x + RS_X, y + RS_Y, 18, 18, PANEL, HI);
        int rs = menu.redstone();
        int c = rs == 0 ? DIM : rs == 1 ? 0xFFE04A3A : 0xFF7A1F18;
        g.fill(x + RS_X + 5, y + RS_Y + 5, x + RS_X + 13, y + RS_Y + 13, c);
        if (rs == 2) g.fill(x + RS_X + 7, y + RS_Y + 7, x + RS_X + 11, y + RS_Y + 11, PANEL);

        // side config
        for (int i = 0; i < 6; i++) {
            int face = GRID_ORDER[i];
            int sx = x + GX + (i % 3) * CELL, sy = y + GY + (i / 3) * CELL;
            box(g, sx, sy, SQ, SQ, MODE_COL[menu.itemMode(face)], menu.energyOn(face) ? 0xFFF2D14B : 0xFF5A2626);
            g.text(font, String.valueOf(FACE_LETTER.charAt(face)), sx + 4, sy + 2, 0xFFFFFFFF, false);
        }
    }

    private int fluidColor() {
        int id = menu.fluidId();
        if (id < 0) return 0xFF9AA0A6;
        Fluid f = BuiltInRegistries.FLUID.byId(id);
        if (f == Fluids.LAVA || f == Fluids.FLOWING_LAVA) return 0xFFE8741A;
        if (f == Fluids.WATER || f == Fluids.FLOWING_WATER) return 0xFF3F76E4;
        return 0xFF9AA0A6;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mx, int my) {
        g.text(font, title, 8, 6, TEXT, false);
        g.text(font, playerInventoryTitle, 8, 73, DIM, false);
        g.text(font, "Aug", 150, 45, DIM, false);
    }

    private int faceAt(double mx, double my) {
        for (int i = 0; i < 6; i++) {
            int sx = leftPos + GX + (i % 3) * CELL, sy = topPos + GY + (i / 3) * CELL;
            if (mx >= sx && mx < sx + SQ && my >= sy && my < sy + SQ) return GRID_ORDER[i];
        }
        return -1;
    }

    private boolean inRect(double mx, double my, int x, int y, int w, int h) {
        return mx >= leftPos + x && mx < leftPos + x + w && my >= topPos + y && my < topPos + y + h;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x(), my = event.y();
        int face = faceAt(mx, my);
        if (face >= 0) {
            press(event.button() == 1 ? 7 + face : 1 + face);
            return true;
        }
        if (inRect(mx, my, RS_X, RS_Y, 18, 18)) {
            press(0);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void press(int id) {
        if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor g, int mx, int my) {
        super.extractTooltip(g, mx, my);
        if (inRect(mx, my, FE_X, FE_Y, FE_W, FE_H)) {
            String t = String.format("%,d / %,d FE", menu.energy(), menu.capacity());
            if (menu.draw() > 0) t += "  (-" + menu.draw() + " FE/t)";
            g.setTooltipForNextFrame(Component.literal(t), mx, my);
        } else if (menu.kind.fluid && inRect(mx, my, TANK_X, TANK_Y, TANK_W, TANK_H)) {
            String name = menu.fluidId() < 0 ? "Empty" : BuiltInRegistries.FLUID.getKey(BuiltInRegistries.FLUID.byId(menu.fluidId())).toString();
            g.setTooltipForNextFrame(Component.literal(name + "  " + menu.fluidAmount() + " / " + menu.fluidCapacity() + " mB"), mx, my);
        } else if (inRect(mx, my, ARROW_X, ARROW_Y, ARROW_W, 8)) {
            int sp = menu.speedPercent();
            g.setTooltipForNextFrame(Component.literal(sp > 0 ? "Working at " + sp + "% speed" : "Idle"), mx, my);
        } else if (inRect(mx, my, RS_X, RS_Y, 18, 18)) {
            g.setTooltipForNextFrame(Component.literal("Redstone: " + RS_NAME[menu.redstone()] + " (click)"), mx, my);
        } else {
            int face = faceAt(mx, my);
            if (face >= 0) {
                g.setTooltipForNextFrame(Component.literal(FACES[face].getName() + ": items " + MODE_NAME[menu.itemMode(face)] + ", power "
                        + (menu.energyOn(face) ? "on" : "off") + " (click items, right-click power)"), mx, my);
            }
        }
    }
}
