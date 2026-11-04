package dev.shadowsoffire.apothic_enchanting.library;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import com.google.common.base.Strings;
import com.mojang.blaze3d.platform.InputConstants;

import dev.shadowsoffire.apothic_attributes.ApothicAttributes;
import dev.shadowsoffire.apothic_enchanting.ApothicEnchanting;
import dev.shadowsoffire.placebo.payloads.ButtonClickPayload;
import dev.shadowsoffire.placebo.util.DrawsOnLeft;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class EnchLibraryScreen extends AbstractContainerScreen<EnchLibraryContainer> implements DrawsOnLeft {

    public static final Identifier TEXTURES = ApothicEnchanting.loc("textures/gui/library.png");
    public static final int MAX_ENTRIES = 5;
    public static final int ENTRY_WIDTH = 113;
    public static final int ENTRY_HEIGHT = 20;

    protected float scrollOffs;
    protected boolean scrolling;
    protected int startIndex;

    protected List<LibrarySlot> data = new ArrayList<>();
    protected EditBox filter = null;

    public EnchLibraryScreen(EnchLibraryContainer container, Inventory inv, Component title) {
        super(container, inv, title, 176, 230);
        this.containerChanged();
        container.setNotifier(this::containerChanged);
    }

    @Override
    protected void init() {
        super.init();
        this.filter = this.addRenderableWidget(new EditBox(this.font, this.getLeftPos() + 16, this.getTopPos() + 16, 110, 11, this.filter, Component.literal("")));
        this.filter.setBordered(false);
        this.filter.setTextColor(0xFF97714F);
        this.filter.setResponder(t -> this.containerChanged());
        this.setFocused(this.filter);
        this.containerChanged();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        InputConstants.Key mouseKey = InputConstants.getKey(event);
        if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey) && this.getFocused() == this.filter) {
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(gfx, mouseX, mouseY, partialTicks);
        this.extractHoveredTooltip(gfx, mouseX, mouseY);
    }

    protected void extractHoveredTooltip(GuiGraphicsExtractor gfx, int mouseX, int mouseY) {
        LibrarySlot libSlot = this.getHoveredSlot(mouseX, mouseY);
        if (libSlot != null) {
            List<FormattedText> list = new ArrayList<>();

            MutableComponent name = libSlot.ench.value().description().copy().setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFF80)).withUnderlined(true));
            if (ApothicAttributes.getTooltipFlag().isAdvanced()) {
                name = name.append(Component.literal(" [" + libSlot.ench.getKey().identifier() + "]").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withUnderlined(false)));
            }
            list.add(name);

            String descKey = libSlot.ench.getKey().identifier().toLanguageKey("enchantment") + ".desc";

            if (I18n.exists(descKey) || ApothicAttributes.getTooltipFlag().isAdvanced()) {
                Component txt = Component.translatable(descKey).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true));
                list.addAll(this.font.getSplitter().splitLines(txt, this.getLeftPos() - 16, txt.getStyle()));
                list.add(CommonComponents.SPACE);
            }

            list.add(Component.translatable("tooltip.enchlib.max_lvl", Component.translatable("enchantment.level." + libSlot.maxLvl)).withStyle(ChatFormatting.GRAY));
            list.add(Component.translatable("tooltip.enchlib.points", format(libSlot.points), format(this.menu.getPointCap())).withStyle(ChatFormatting.GRAY));
            list.add(CommonComponents.SPACE);
            ItemStack outSlot = this.menu.ioInv.getItem(1);
            int current = EnchantmentHelper.getEnchantmentsForCrafting(outSlot).getLevel(libSlot.ench);
            boolean shift = Minecraft.getInstance().hasShiftDown();
            int targetLevel = shift ? Math.min(libSlot.maxLvl, 1 + (int) (Math.log(libSlot.points + EnchLibraryTile.levelToPoints(current)) / Math.log(2))) : current + 1;
            if (targetLevel == current) targetLevel++;
            int cost = EnchLibraryTile.levelToPoints(targetLevel) - EnchLibraryTile.levelToPoints(current);
            if (targetLevel > libSlot.maxLvl) list.add(Component.translatable("tooltip.enchlib.unavailable").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            else {
                list.add(Component.translatable("tooltip.enchlib.extracting", Component.translatable("enchantment.level." + targetLevel)).withStyle(ChatFormatting.BLUE));
                list.add(Component.translatable("tooltip.enchlib.cost", cost).withStyle(cost > libSlot.points ? ChatFormatting.RED : ChatFormatting.GOLD));
            }
            this.drawOnLeft(gfx, list, mouseY);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float a) {
        super.extractBackground(gfx, mouseX, mouseY, a);
        int left = this.leftPos;
        int top = this.topPos;
        gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURES, left, top, 0, 0, this.imageWidth, this.imageHeight, 307, 256);
        int scrollbarPos = (int) (90F * this.scrollOffs);
        gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURES, left + 13, top + 29 + scrollbarPos, 303, 40 + (this.isScrollBarActive() ? 0 : 12), 4, 12, 307, 256);
        int idx = this.startIndex;
        while (idx < this.startIndex + MAX_ENTRIES && idx < this.data.size()) {
            this.extractEntry(gfx, this.data.get(idx), this.leftPos + 20, this.topPos + 30 + ENTRY_HEIGHT * (idx - this.startIndex), mouseX, mouseY);
            idx++;
        }
    }

    private void extractEntry(GuiGraphicsExtractor gfx, LibrarySlot data, int x, int y, int mouseX, int mouseY) {
        LibrarySlot hover = this.getHoveredSlot(mouseX, mouseY);
        gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURES, x, y, 194, data == hover ? ENTRY_HEIGHT : 0, ENTRY_WIDTH, ENTRY_HEIGHT, 307, 256);
        int progress = (int) Math.round(85 * Math.sqrt(data.points) / (float) Math.sqrt(this.menu.getPointCap()));
        gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURES, x + 3, y + 14, 197, 42, progress, 3, 307, 256);
        Matrix3x2fStack stack = gfx.pose();
        stack.pushMatrix();
        Component txt = data.ench().value().description().plainCopy();
        float scale = 1;
        if (this.font.width(txt) > 85) {
            scale = 85F / this.font.width(txt);
        }
        stack.scale(scale, scale);
        gfx.text(this.font, txt, (int) ((x + 3) / scale), (int) ((y + 3) / scale), 0xFF8EE14D, false);
        stack.popMatrix();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double pMouseX = event.x();
        double pMouseY = event.y();
        int pButton = event.button();
        this.scrolling = false;
        if (this.isHovering(14, 29, 4, 103, pMouseX, pMouseY)) {
            this.scrolling = true;
            this.mouseDragged(event, 0, 0);
            return true;
        }

        LibrarySlot libSlot = this.getHoveredSlot((int) pMouseX, (int) pMouseY);
        if (libSlot != null) {
            int id = Minecraft.getInstance().level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getId(libSlot.ench.value());
            if (Minecraft.getInstance().hasShiftDown()) id |= 0x80000000;
            this.menu.onButtonClick(id);
            ClientPacketDistributor.sendToServer(new ButtonClickPayload(id));
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
            return true;
        }

        if (this.filter.isHovered() && pButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            this.filter.setValue("");
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double pDragX, double pDragY) {
        if (this.scrolling && this.isScrollBarActive()) {
            int barTop = this.topPos + 14;
            int barBot = barTop + 103;
            this.scrollOffs = ((float) event.y() - barTop - 6F) / (barBot - barTop - 12F) - 0.12F;
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int) (this.scrollOffs * this.getOffscreenRows() + 0.5D);
            return true;
        }
        else {
            return super.mouseDragged(event, pDragX, pDragY);
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        if (this.isScrollBarActive()) {
            int i = this.getOffscreenRows();
            this.scrollOffs = (float) (this.scrollOffs - pScrollY / i);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int) (this.scrollOffs * i + 0.5D);
        }
        return true;
    }

    private boolean isScrollBarActive() {
        return this.data.size() > MAX_ENTRIES;
    }

    protected int getOffscreenRows() {
        return this.data.size() - MAX_ENTRIES;
    }

    private void containerChanged() {
        this.data.clear();
        List<Entry<Holder<Enchantment>>> entries = this.filter(this.menu.getPointsForDisplay());
        for (Entry<Holder<Enchantment>> e : entries) {
            this.data.add(new LibrarySlot(e.getKey(), e.getIntValue(), this.menu.getMaxLevel(e.getKey())));
        }

        if (!this.isScrollBarActive()) {
            this.scrollOffs = 0.0F;
            this.startIndex = 0;
        }
        Collections.sort(this.data, Comparator.comparing(slot -> slot.ench.value().description().getString()));
    }

    private List<Entry<Holder<Enchantment>>> filter(List<Entry<Holder<Enchantment>>> list) {
        return list.stream().filter(this::isAllowedByItem).filter(this::isAllowedBySearch).toList();
    }

    private boolean isAllowedByItem(Entry<Holder<Enchantment>> e) {
        ItemStack stack = this.menu.ioInv.getItem(2);
        return stack.isEmpty() || stack.supportsEnchantment(e.getKey());
    }

    private boolean isAllowedBySearch(Entry<Holder<Enchantment>> e) {
        String name = e.getKey().value().description().getString().toLowerCase(Locale.ROOT);
        String search = this.filter == null ? "" : this.filter.getValue().trim().toLowerCase(Locale.ROOT);
        return Strings.isNullOrEmpty(search) || ChatFormatting.stripFormatting(name).contains(search);
    }

    @Nullable
    public LibrarySlot getHoveredSlot(int mouseX, int mouseY) {
        for (int i = 0; i < MAX_ENTRIES; i++) {
            if (this.startIndex + i < this.data.size()) {
                if (this.isHovering(21, 31 + i * ENTRY_HEIGHT, ENTRY_WIDTH, ENTRY_HEIGHT - 2, mouseX, mouseY)) {
                    return this.data.get(this.startIndex + i);
                }
            }
        }
        return null;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY) {}

    public int getSlotColor(int index) {
        return 0x40FFFFFF;
    }

    private static record LibrarySlot(Holder<Enchantment> ench, int points, int maxLvl) {}

    private static DecimalFormat f = new DecimalFormat("##.#");

    private static String format(int n) {
        int log = (int) StrictMath.log10(n);
        if (log <= 3) return String.valueOf(n);
        else if (log <= 6) return f.format(n / 1000D) + "K";
        else if (log <= 8) return f.format(n / 1000000D) + "M";
        else return f.format(n / 1000000000D) + "B";
    }

}
