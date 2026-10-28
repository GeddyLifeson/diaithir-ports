package com.github.alexmodguy.alexscaves.client.gui.book;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.gui.book.widget.BookWidget;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import com.github.alexmodguy.alexscaves.server.misc.CaveBookProgress;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The Cave Book. 26.1 port: upstream drew a 3D book model with the page text laid out in world space.
 * The 3D-in-GUI path is not available to mods in 26.1, so the same entries (json + text files) are drawn
 * as a flat open book: two pages of 15 lines each, with the same widgets, links and progression locks.
 */
public class CaveBookScreen extends Screen {

    public static final int PAGE_SIZE_IN_LINES = 15;
    public static final int TEXT_COLOR = 0xFF826A41;
    public static final int TEXT_LINK_COLOR = 0xFF111111;
    public static final int TEXT_LINK_HOVER_COLOR = 0xFF0094FF;
    public static final int TEXT_LINK_LOCKED_COLOR = 0xFFB7AA82;

    private static final int PAGE_W = 140;
    private static final int PAGE_H = 168;
    private static final int MARGIN = 11;
    private static final int SPINE = 8;
    private static final int BOOK_W = PAGE_W * 2 + MARGIN * 2 + SPINE;
    private static final int BOOK_H = PAGE_H + MARGIN * 2;

    private final CaveBookProgress caveBookProgress;
    public boolean unlockTooltip;
    private int tickCount = 0;
    private Identifier currentEntryJSON;
    private Identifier prevEntryJSON;
    private BookEntry currentEntry;
    private int entryPageNumber = 0;
    private int lastEntryPageBeforeLinkClick = -1;

    // layout of the last frame, used for hit testing
    private float pageScale = 1F;
    private float bookX;
    private float bookY;

    public CaveBookScreen(String openTo) {
        super(Component.translatable("item.alexscaves.cave_book"));
        caveBookProgress = CaveBookProgress.getCaveBookProgress(Minecraft.getInstance().player);
        currentEntryJSON = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, openTo);
        resetEntry();
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ACSoundRegistry.CAVE_BOOK_OPEN.get(), 1.0F));
    }

    public CaveBookScreen() {
        this("books/root.json");
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        tickCount++;
    }

    public void resetEntry() {
        currentEntry = currentEntryJSON == null ? null : readBookEntry(currentEntryJSON);
        if (currentEntry != null) {
            currentEntry.init(this);
        }
        if (currentEntry != null && currentEntry.getParent() != null && !currentEntry.getParent().isEmpty()) {
            prevEntryJSON = Identifier.parse(getBookFileDirectory() + currentEntry.getParent());
        } else {
            prevEntryJSON = null;
        }
        entryPageNumber = 0;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        Font font = this.font;
        pageScale = Math.min(Math.min((this.height - 16F) / BOOK_H, (this.width - 16F) / BOOK_W), 2.0F);
        bookX = (this.width - BOOK_W * pageScale) / 2F;
        bookY = (this.height - BOOK_H * pageScale) / 2F;
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(bookX, bookY);
        pose.scale(pageScale, pageScale);
        // cover, spine and the two pages
        graphics.fill(0, 0, BOOK_W, BOOK_H, 0xFF2E1B0E);
        graphics.fill(2, 2, BOOK_W - 2, BOOK_H - 2, 0xFF5B3A20);
        graphics.fill(MARGIN - 3, MARGIN - 3, BOOK_W - MARGIN + 3, BOOK_H - MARGIN + 3, 0xFFC9B27E);
        graphics.fill(MARGIN - 1, MARGIN - 1, MARGIN + PAGE_W + 1, BOOK_H - MARGIN + 1, 0xFFE9DAB0);
        graphics.fill(MARGIN + PAGE_W + SPINE - 1, MARGIN - 1, MARGIN + PAGE_W * 2 + SPINE + 1, BOOK_H - MARGIN + 1, 0xFFE9DAB0);
        graphics.fill(MARGIN + PAGE_W - 6, MARGIN, MARGIN + PAGE_W, BOOK_H - MARGIN, 0x18704A20);
        graphics.fill(MARGIN + PAGE_W + SPINE, MARGIN, MARGIN + PAGE_W + SPINE + 6, BOOK_H - MARGIN, 0x18704A20);
        graphics.fill(MARGIN + PAGE_W, MARGIN - 1, MARGIN + PAGE_W + SPINE, BOOK_H - MARGIN + 1, 0xFF8C6A3A);
        pose.popMatrix();

        unlockTooltip = false;
        if (currentEntry != null) {
            currentEntry.getEntryLinks().forEach(l -> l.setHovered(false));
            renderPage(graphics, font, 0, mouseX, mouseY, partialTick);
            renderPage(graphics, font, 1, mouseX, mouseY, partialTick);
        }
        renderArrows(graphics, font, mouseX, mouseY);
        if (unlockTooltip) {
            List<Component> list = new ArrayList<>();
            list.add(Component.translatable("book.alexscaves.page_locked_0").withStyle(ChatFormatting.GRAY));
            list.add(Component.translatable("book.alexscaves.page_locked_1").withStyle(ChatFormatting.GRAY));
            graphics.setComponentTooltipForNextFrame(font, list, mouseX - 5, mouseY - 5);
        }
    }

    private float pageOriginX(int side) {
        return bookX + (MARGIN + 6 + side * (PAGE_W + SPINE)) * pageScale;
    }

    private float pageOriginY() {
        return bookY + (MARGIN + 6) * pageScale;
    }

    private void renderPage(GuiGraphicsExtractor graphics, Font font, int side, int mouseX, int mouseY, float partialTick) {
        int pgNumber = 1 + side + entryPageNumber * 2;
        float ox = pageOriginX(side);
        float oy = pageOriginY();
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(ox, oy);
        pose.scale(pageScale, pageScale);
        if (pgNumber == 1 && !currentEntry.getTranslatableTitle().isEmpty()) {
            Component title = Component.translatable(currentEntry.getTranslatableTitle());
            int titleLength = Math.max(font.width(title), 1);
            float titleScale = Math.min(PAGE_W * 0.9F / titleLength, 2.0F);
            pose.pushMatrix();
            pose.translate(PAGE_W / 2F, 7F - 5F * titleScale);
            pose.scale(titleScale, titleScale);
            graphics.text(font, title, -titleLength / 2, 0, 0xFF5A4326, false);
            pose.popMatrix();
        }
        int start = (pgNumber - 1) * PAGE_SIZE_IN_LINES;
        List<String> lines = currentEntry.getEntryText();
        for (int i = start; i < start + PAGE_SIZE_IN_LINES && i < lines.size(); i++) {
            graphics.text(font, lines.get(i), 0, (i - start) * 10, TEXT_COLOR, false);
        }
        for (BookLink link : currentEntry.getEntryLinks()) {
            int line = link.getLineNumber();
            if (line < start || line >= start + PAGE_SIZE_IN_LINES || line >= lines.size()) {
                continue;
            }
            String text = lines.get(line);
            int x = font.width(text.substring(0, Math.min(link.getCharacterStartsAt(), text.length())));
            int y = (line - start) * 10;
            int w = font.width(link.getDisplayText());
            float lx = (mouseX - ox) / pageScale;
            float ly = (mouseY - oy) / pageScale;
            boolean hover = lx >= x && lx <= x + w && ly >= y - 1 && ly <= y + 9;
            link.setHovered(hover && link.isEnabled());
            if (hover && !link.isEnabled()) {
                unlockTooltip = true;
            }
            int color = !link.isEnabled() ? TEXT_LINK_LOCKED_COLOR : link.isHovered() ? TEXT_LINK_HOVER_COLOR : TEXT_LINK_COLOR;
            // cover the plain text the line already drew, then draw the link on top of it
            graphics.fill(x, y, x + w, y + 9, 0xFFE9DAB0);
            graphics.text(font, Component.literal(link.getDisplayText()).withStyle(ChatFormatting.UNDERLINE), x, y, color, false);
        }
        String number = String.valueOf(pgNumber);
        graphics.text(font, number, (int) (PAGE_W / 2F - font.width(number) / 2F), PAGE_H - 12, TEXT_COLOR, false);
        BookWidget.PageContext ctx = new BookWidget.PageContext(ox, oy, pageScale);
        for (BookWidget widget : currentEntry.getWidgets()) {
            if (widget.getDisplayPage() == pgNumber) {
                widget.render(graphics, ctx, partialTick);
            }
        }
        pose.popMatrix();
    }

    private boolean overLeftArrow(double mx, double my) {
        return canGoLeft() && mx >= bookX + 4 * pageScale && mx <= bookX + (MARGIN + 28) * pageScale && my >= bookY + (BOOK_H - MARGIN - 14) * pageScale && my <= bookY + (BOOK_H - 1) * pageScale;
    }

    private boolean overRightArrow(double mx, double my) {
        return canGoRight() && mx <= bookX + (BOOK_W - 4) * pageScale && mx >= bookX + (BOOK_W - MARGIN - 28) * pageScale && my >= bookY + (BOOK_H - MARGIN - 14) * pageScale && my <= bookY + (BOOK_H - 1) * pageScale;
    }

    private void renderArrows(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        var pose = graphics.pose();
        float ay = bookY + (BOOK_H - MARGIN - 12) * pageScale;
        if (canGoLeft()) {
            int color = overLeftArrow(mouseX, mouseY) ? TEXT_LINK_HOVER_COLOR : 0xFF3E2A14;
            pose.pushMatrix();
            pose.translate(bookX + (MARGIN + 6) * pageScale, ay);
            pose.scale(pageScale, pageScale);
            graphics.text(font, "<", 0, 0, color, false);
            pose.popMatrix();
        }
        if (canGoRight()) {
            int color = overRightArrow(mouseX, mouseY) ? TEXT_LINK_HOVER_COLOR : 0xFF3E2A14;
            pose.pushMatrix();
            pose.translate(bookX + (BOOK_W - MARGIN - 12) * pageScale, ay);
            pose.scale(pageScale, pageScale);
            graphics.text(font, ">", 0, 0, color, false);
            pose.popMatrix();
        }
    }

    public boolean canGoLeft() {
        return currentEntry != null && (entryPageNumber > 0 || prevEntryJSON != null);
    }

    public boolean canGoRight() {
        return currentEntry != null && entryPageNumber + 1 < currentEntry.getPageCount();
    }

    private void goLeft() {
        if (!canGoLeft()) {
            return;
        }
        playTurn();
        if (entryPageNumber > 0) {
            entryPageNumber--;
        } else if (prevEntryJSON != null) {
            int page = lastEntryPageBeforeLinkClick == -1 ? 0 : lastEntryPageBeforeLinkClick;
            lastEntryPageBeforeLinkClick = -1;
            currentEntryJSON = prevEntryJSON;
            resetEntry();
            entryPageNumber = Math.min(page, Math.max(0, currentEntry == null ? 0 : currentEntry.getPageCount() - 1));
        }
    }

    private void goRight() {
        if (canGoRight()) {
            playTurn();
            entryPageNumber++;
        }
    }

    private void playTurn() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ACSoundRegistry.CAVE_BOOK_TURN.get(), 1.0F));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        if (currentEntry != null) {
            for (BookLink link : currentEntry.getEntryLinks()) {
                int minLine = entryPageNumber * PAGE_SIZE_IN_LINES * 2;
                if (link.isEnabled() && link.isHovered() && link.getLineNumber() >= minLine && link.getLineNumber() < minLine + PAGE_SIZE_IN_LINES * 2) {
                    return attemptChangePage(Identifier.parse(getBookFileDirectory() + link.getLinksTo()));
                }
            }
        }
        if (overLeftArrow(event.x(), event.y())) {
            goLeft();
            return true;
        }
        if (overRightArrow(event.x(), event.y())) {
            goRight();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 263) { // left
            goLeft();
            return true;
        } else if (event.key() == 262) { // right
            goRight();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0) {
            goLeft();
        } else if (scrollY < 0) {
            goRight();
        }
        return true;
    }

    @Nullable
    protected BookEntry readBookEntry(Identifier res) {
        BookEntry page = null;
        try {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(res);
            if (resource.isPresent()) {
                BufferedReader inputstream = resource.get().openAsReader();
                page = BookEntry.deserialize(inputstream);
            }
        } catch (IOException e1) {
            if (!(e1 instanceof AccessDeniedException)) {
                e1.printStackTrace();
            }
        }
        return page;
    }

    public CaveBookProgress getCaveBookProgress() {
        return caveBookProgress;
    }

    public int getEntryPageNumber() {
        return entryPageNumber;
    }

    public static String getBookFileDirectory() {
        return AlexsCaves.MODID + ":books/";
    }

    public boolean attemptChangePage(Identifier changePageTo) {
        if (!currentEntryJSON.equals(changePageTo)) {
            lastEntryPageBeforeLinkClick = this.entryPageNumber;
        }
        prevEntryJSON = currentEntryJSON;
        currentEntryJSON = changePageTo;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ACSoundRegistry.CAVE_BOOK_CLOSE.get(), 1.0F));
        resetEntry();
        // resetEntry points "back" at the parent entry; keep the page we came from for the return trip
        return true;
    }

    public int getEntryVisiblity(String linkTo) {
        Identifier resourceLocation = Identifier.parse(CaveBookScreen.getBookFileDirectory() + linkTo);
        BookEntry dummyEntry = readBookEntry(resourceLocation);
        int visiblity = 0;
        if (dummyEntry != null) {
            visiblity = dummyEntry.getVisibility(this);
        }
        if (visiblity != 2 && Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCreative()) {
            visiblity = 0;
        }
        return visiblity;
    }
}
