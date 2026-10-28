/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.inventory.ItemSearcherMenu;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import me.desht.pneumaticcraft.client.gui.compat.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemSearcherScreen extends AbstractContainerScreen<ItemSearcherMenu> {
    private static final Identifier GUI_TEXTURE = Textures.GUI_ITEM_SEARCHER;
    private static final int SEARCH_SLOT = 48;

    private static List<SearchEntry> cachedSearchEntries;

    private final ItemStackHandler inventory = new ItemStackHandler(49);  // 6 * 8 slots, plus the selected item
    private final Screen parentScreen;
    // Amount scrolled in Creative mode inventory (0 = top, 1 = bottom)
    private double currentScroll;
    // True if the scrollbar is being dragged
    private boolean isScrolling;
    // True if the left mouse button was held down last time drawScreen was called.
    private EditBox searchField;
    private Rect2i scrollArea;
    private String lastSearch = "";
    private int updateCounter = 0;
    private long lastClickTime = 0L;

    public ItemSearcherScreen(ItemSearcherMenu container, Inventory playerInventory, Component displayString) {
        super(container, playerInventory, displayString);

        imageHeight = 176;
        parentScreen = Minecraft.getInstance().screen;
        container.init(this);
    }

    @Nonnull
    public ItemStack getSearchStack() {
        return inventory.getStackInSlot(SEARCH_SLOT);
    }

    public void setSearchStack(@Nonnull ItemStack stack) {
        inventory.setStackInSlot(SEARCH_SLOT, stack);
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ContainerInput type) {
        if (slot != null) {
            if (slot.index == SEARCH_SLOT) {
                slot.set(ItemStack.EMPTY);
            } else {
                long now = Util.getMillis();
                if (now - lastClickTime < 250 && ItemStack.isSameItemSameComponents(getSearchStack(), slot.getItem())) {
                    onClose();
                } else {
                    inventory.setStackInSlot(SEARCH_SLOT, slot.getItem());
                }
                lastClickTime = now;
            }
        }
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void init() {
        super.init();

        searchField = new WidgetTextField(font, leftPos + 8, topPos + 35, 89);
        searchField.setMaxLength(15);
        searchField.setBordered(true);
        searchField.setVisible(true);
        searchField.setTextColor(0xFFFFFF);
        searchField.setResponder(s -> textFieldResponder());
        addRenderableWidget(searchField);
        setFocused(searchField);

        scrollArea = new Rect2i(leftPos + 156, topPos + 52, 14, 108);

        updateCreativeSearch();
    }

    private void textFieldResponder() {
        if (!searchField.getValue().equals(lastSearch)) {
            updateCounter = 5;
        }
        lastSearch = searchField.getValue();
    }

    @Override
    public void onClose() {
        if (parentScreen != null) {
            ClientUtils.closeContainerGui(parentScreen);
        } else {
            super.onClose();
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();

        if (updateCounter > 0 && --updateCounter == 0) {
            updateCreativeSearch();
        }

        if (false
                && minecraft.player.getItemBySlot(EquipmentSlot.HEAD).getItem() != ModItems.PNEUMATIC_HELMET.get()) {
            onClose();
        }
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent _ev) {
        int keyCode = _ev.key(), scanCode = _ev.scancode(), modifiers = _ev.modifiers();
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
        }
        return !searchField.keyPressed(me.desht.pneumaticcraft.client.gui.compat.GuiCompat.key(keyCode, scanCode, modifiers))
                && searchField.canConsumeInput() || super.keyPressed(_ev);
    }

    private Stream<SearchEntry> getSearchEntries() {
        if (cachedSearchEntries == null) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {  // should always be the case at this point
                CreativeModeTabs.tryRebuildTabContents(player.connection.enabledFeatures(), Minecraft.getInstance().options.operatorItemsTab().get(), player.level().registryAccess());
            }
            cachedSearchEntries = CreativeModeTabs.searchTab().getDisplayItems().stream()
                    .map(SearchEntry::new)
                    .toList();
        }
        return cachedSearchEntries.stream();
    }

    private void updateCreativeSearch() {
        menu.itemList.clear();

        String s = searchField.getValue().toLowerCase();

        List<ItemStack> applicableEntries = getSearchEntries()
                .filter(entry -> entry.test(s))
                .map(entry -> entry.stack)
                .toList();

        menu.itemList.addAll(applicableEntries);

        currentScroll = 0.0F;
        menu.scrollTo(0.0F);
    }

    private boolean needsScrollBars() {
        return menu.hasMoreThan1PageOfItemsInList();
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dirX, double dirY) {
        if (dirY != 0 && needsScrollBars()) {
            int j = menu.itemList.size() / 9 - 5 + 1;
            float i = dirY > 0 ? 1f : -1f;
            currentScroll = Mth.clamp(currentScroll - i / j, 0.0, 1.0);
            menu.scrollTo(currentScroll);
            return true;
        }
        return super.mouseScrolled(x, y, dirX, dirY);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent _ev, boolean _dbl) {
        double mouseX = _ev.x(), mouseY = _ev.y(); int button = _ev.button();
        isScrolling = button == 0 && needsScrollBars() && scrollArea.contains((int)mouseX, (int)mouseY);
        if (isScrolling) {
            scrollTo(mouseY);
            return true;
        }
        return super.mouseClicked(_ev, _dbl);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent _ev, double dragX, double dragY) {
        double mouseX = _ev.x(), mouseY = _ev.y(); int button = _ev.button();
        if (isScrolling) {
            scrollTo(mouseY);
            return true;
        }
        return super.mouseDragged(_ev, dragX, dragY);
    }

    private void scrollTo(double mouseY) {
        currentScroll = (mouseY - scrollArea.getY()) / scrollArea.getHeight();
        currentScroll = Mth.clamp(currentScroll, 0F, 1F);
        menu.scrollTo(currentScroll);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent _ev) {
        double mouseX = _ev.x(), mouseY = _ev.y(); int button = _ev.button();
        isScrolling = false;

        return super.mouseReleased(_ev);
    }

    @Override
    public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor graphics_raw, int x, int y, float partialTicks) {
        GuiGraphics graphics = GuiGraphics.of(graphics_raw);
        super.extractRenderState(graphics.raw(), x, y, partialTicks);
    }

    @Override
    protected void extractLabels(net.minecraft.client.gui.GuiGraphicsExtractor graphics_raw, int par1, int par2) {
        GuiGraphics graphics = GuiGraphics.of(graphics_raw);
        graphics.drawString(font, xlate("pneumaticcraft.armor.upgrade.search"), 5, 5, 0x404040, false);
        graphics.drawString(font, xlate("pneumaticcraft.gui.progWidget.itemFilter.filterLabel"), 8, 25, 0x404040, false);
    }

    @Override
    public void extractBackground(net.minecraft.client.gui.GuiGraphicsExtractor graphics_raw, int par2, int par3, float par1) {
        GuiGraphics graphics = GuiGraphics.of(graphics_raw);
        int xStart = (width - imageWidth) / 2;
        int yStart = (height - imageHeight) / 2;
        graphics.blit(GUI_TEXTURE, xStart, yStart, 0, 0, imageWidth, imageHeight);

        int x = scrollArea.getX();
        int y1 = scrollArea.getY();
        int y2 = y1 + scrollArea.getHeight();

        graphics.blit(Textures.WIDGET_VERTICAL_SCROLLBAR, x, y1 + (int) ((y2 - y1 - 17) * currentScroll), (needsScrollBars() ? 0 : 12), 0, 12, 15, 26, 15);

    }

    public IItemHandlerModifiable getInventory() {
        return inventory;
    }
    
    public class SearchEntry implements Predicate<String> {
        public final ItemStack stack;
        private final String tooltip;
        
        SearchEntry(ItemStack stack) {
            this.stack = stack;

            List<String> l;
            try {
                l = stack.getTooltipLines(Item.TooltipContext.of(minecraft.level), minecraft.player, minecraft.options.advancedItemTooltips ? Default.ADVANCED : Default.NORMAL).stream()
                        .map(Component::getString)
                        .collect(Collectors.toList());
            } catch (Exception ignored) {
                // it's possible some modded item could have a buggy addInformation() implementation
                l = Collections.emptyList();
            }
            tooltip = StringUtils.join(l, "\n").toLowerCase();
        }
        
        @Override
        public boolean test(String searchString) {
            if (searchString.startsWith("@")) {
                Identifier id = PneumaticCraftUtils.getRegistryName(stack.getItem()).orElse(Identifier.withDefaultNamespace("none"));
                return id.getNamespace().startsWith(searchString.substring(1));
            } else if (searchString.startsWith("#")) {
                Identifier id = Identifier.tryParse(searchString.substring(1));
                return id != null && stack.is(TagKey.create(Registries.ITEM, id));
            } else {
                return tooltip.contains(searchString);
            }
        }
    }
}
