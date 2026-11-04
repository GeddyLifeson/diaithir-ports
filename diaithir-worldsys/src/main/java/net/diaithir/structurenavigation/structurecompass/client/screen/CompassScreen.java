// Ported from Structure Compass (MIT) https://github.com/Mrbysco/StructureCompass
// Modifications: GameStages compat removed, FML StringUtils replaced by Locale lowercasing, payload/translation keys renamed.
package net.diaithir.structurenavigation.structurecompass.client.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.diaithir.structurenavigation.structurecompass.client.screen.widget.StructureListWidget;
import net.diaithir.structurenavigation.structurecompass.client.screen.widget.ToggleButton;
import net.diaithir.structurenavigation.structurecompass.network.SetStructurePayload;
import net.diaithir.structurenavigation.structurecompass.registry.CompassComponents;
import net.diaithir.structurenavigation.structurecompass.util.StructureUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class CompassScreen extends Screen {
    private final Component showTagsText = Component.translatable("structurecompass.screen.search.show_tags");
    private final Component hideTagsText = Component.translatable("structurecompass.screen.search.hide_tags");

    private enum SortType {
        NORMAL,
        A_TO_Z,
        Z_TO_A;

        Button button;

        Component getButtonText() {
            return Component.translatable("structurecompass.screen.search." + name().toLowerCase(Locale.ROOT));
        }
    }

    private static final int PADDING = 6;

    private StructureListWidget structureWidget;
    private StructureListWidget.ListEntry selected = null;
    private int listWidth;
    private List<Identifier> structures;
    private final List<Identifier> unsortedStructures;
    private List<Identifier> tags;
    private final List<Identifier> unsortedTags;
    private Button loadButton;
    private ToggleButton showTags;

    private final InteractionHand usedHand;
    private final ItemStack compassStack;

    private final int buttonMargin = 1;
    private final int numButtons = SortType.values().length;
    private String lastFilterText = "";

    private EditBox search;
    private boolean sorted = false;
    private SortType sortType = SortType.NORMAL;

    public CompassScreen(InteractionHand hand, ItemStack compass, List<Identifier> allStructures, List<Identifier> allTags) {
        super(Component.translatable("structurecompass.screen.selection.title"));
        this.usedHand = hand;
        this.compassStack = compass;

        List<Identifier> structureList = new ArrayList<>();
        for (Identifier id : allStructures) {
            if (id != null) {
                structureList.add(id);
            }
        }
        Collections.sort(structureList);

        this.structures = Collections.unmodifiableList(structureList);
        this.unsortedStructures = Collections.unmodifiableList(allStructures);

        this.tags = Collections.unmodifiableList(allTags);
        this.unsortedTags = Collections.unmodifiableList(allTags);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        int centerWidth = this.width / 2;
        for (Identifier structureLocation : structures) {
            listWidth = Math.max(listWidth, getFontRenderer().width(structureLocation.toString()) + 10);
        }
        listWidth = Math.max(Math.min(listWidth, width / 3), 200);
        listWidth += listWidth % numButtons != 0 ? (numButtons - listWidth % numButtons) : 0;
        int structureWidth = this.width - this.listWidth - (PADDING * 3);
        int closeButtonWidth = Math.min(structureWidth, 200);
        int y = this.height - 20 - PADDING;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> CompassScreen.this.onClose())
                .bounds(centerWidth - (closeButtonWidth / 2) + PADDING, y, closeButtonWidth, 20).build());

        y -= 18 + PADDING;
        this.addRenderableWidget(this.loadButton = Button.builder(Component.translatable("structurecompass.screen.selection.select"), b -> {
            if (selected != null) {
                ClientPacketDistributor.sendToServer(new SetStructurePayload(usedHand, selected.getStructureLocation(), selected.isTag()));
            }

            if (this.minecraft.player != null && selected != null) {
                this.minecraft.player.sendSystemMessage(Component.translatable("structurecompass.screen.selection.selected").withStyle(ChatFormatting.GOLD));
            }
            this.onClose();
        }).bounds(centerWidth - (closeButtonWidth / 2) + PADDING, y, closeButtonWidth, 20).build());

        y -= 14 + PADDING;
        search = new EditBox(getFontRenderer(), centerWidth - listWidth / 2 + PADDING + 1, y, listWidth - 2, 14,
                Component.translatable("structurecompass.screen.search"));

        this.addRenderableWidget(this.showTags = new ToggleButton.Builder(false, showTagsText, hideTagsText, b -> {
            ToggleButton toggleButton = (ToggleButton) b;
            toggleButton.setValue(!toggleButton.getValue());
            this.updateTags();
        }).bounds(centerWidth + listWidth / 2 - 24, PADDING, 80, 20).build());

        int fullButtonHeight = PADDING + 20 + PADDING;
        this.structureWidget = new StructureListWidget(this, this.width, fullButtonHeight, search.getY() - getFontRenderer().lineHeight - PADDING);
        this.structureWidget.setX(0);

        addRenderableWidget(search);
        addRenderableWidget(structureWidget);
        search.setFocused(false);
        search.setCanLoseFocus(true);
        Identifier currentStructure = this.compassStack.get(CompassComponents.STRUCTURE.get());
        if (currentStructure != null) {
            if (this.compassStack.getOrDefault(CompassComponents.IS_TAG.get(), false)) {
                this.showTags.setValue(true);
            }
            search.setValue(currentStructure.toString());
        }

        final int sortButtonWidth = listWidth / numButtons;
        int x = centerWidth + PADDING - sortButtonWidth;
        addRenderableWidget(SortType.A_TO_Z.button = Button.builder(SortType.A_TO_Z.getButtonText(), b ->
                        resortStructures(SortType.A_TO_Z))
                .bounds(x, PADDING, sortButtonWidth - buttonMargin, 20).build());
        x += sortButtonWidth + buttonMargin;
        addRenderableWidget(SortType.Z_TO_A.button = Button.builder(SortType.Z_TO_A.getButtonText(), b ->
                        resortStructures(SortType.Z_TO_A))
                .bounds(x, PADDING, sortButtonWidth - buttonMargin, 20).build());

        resortStructures(SortType.A_TO_Z);
        updateCache();
    }

    private void updateTags() {
        reloadStructures();
        this.structureWidget.refreshList();
        updateCache();
    }

    @Override
    public void tick() {
        if (structureWidget.getSelected() != selected) {
            structureWidget.setSelected(selected);
        }

        if (!search.getValue().equals(lastFilterText)) {
            reloadStructures();
            sorted = false;
        }

        if (!sorted) {
            reloadStructures();
            if (sortType == SortType.A_TO_Z) {
                Collections.sort(structures);
                Collections.sort(tags);
            } else if (sortType == SortType.Z_TO_A) {
                structures.sort(Collections.reverseOrder());
                tags.sort(Collections.reverseOrder());
            }
            structureWidget.refreshList();
            if (selected != null) {
                selected = structureWidget.children().stream().filter(e -> e == selected).findFirst().orElse(null);
                updateCache();
            }
            sorted = true;
        }
    }

    public <T extends ObjectSelectionList.Entry<T>> void buildTagList(Consumer<T> listViewConsumer, Function<Identifier, T> newEntry) {
        if (showTags.getValue()) {
            tags.forEach(tag -> listViewConsumer.accept(newEntry.apply(tag)));
        }
    }

    public <T extends ObjectSelectionList.Entry<T>> void buildStructureList(Consumer<T> listViewConsumer, Function<Identifier, T> newEntry) {
        structures.forEach(structure -> listViewConsumer.accept(newEntry.apply(structure)));
    }

    private void reloadStructures() {
        String filter = search.getValue().toLowerCase(Locale.ROOT);
        this.structures = this.unsortedStructures.stream()
                .filter(struc -> StructureUtil.getStructureName(struc, false).getString().toLowerCase(Locale.ROOT).contains(filter))
                .collect(Collectors.toList());
        this.tags = this.unsortedTags.stream()
                .filter(tag -> StructureUtil.getStructureName(tag, true).getString().toLowerCase(Locale.ROOT).contains(filter))
                .collect(Collectors.toList());
        lastFilterText = search.getValue();
    }

    private void resortStructures(SortType newSort) {
        this.sortType = newSort;

        for (SortType sort : SortType.values()) {
            if (sort.button != null) {
                sort.button.active = sortType != sort;
            }
        }
        sorted = false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);

        if (this.search != null) {
            Component text = Component.translatable("structurecompass.screen.search");
            guiGraphics.centeredText(getFontRenderer(), text, this.width / 2 + PADDING,
                    search.getY() - getFontRenderer().lineHeight - 2, 0xFFFFFFFF);
        }
    }

    public Font getFontRenderer() {
        return font;
    }

    public void setSelected(StructureListWidget.ListEntry previousEntry, StructureListWidget.ListEntry entry) {
        if (this.selected == previousEntry) {
            this.selected = entry;
        } else if (this.selected == null || entry != null) {
            this.selected = entry;
        }
        updateCache();
    }

    private void updateCache() {
        this.loadButton.active = selected != null;
    }

    /**
     * Clear the search field when right-clicked on it.
     */
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        boolean flag = super.mouseClicked(event, isDoubleClick);
        if (event.button() == 1 && search.isMouseOver(event.x(), event.y())) {
            search.setValue("");
        }
        return flag;
    }

    @Override
    public void resize(int width, int height) {
        boolean showTagsValue = this.showTags.getValue();
        String s = this.search.getValue();
        SortType sort = this.sortType;
        StructureListWidget.ListEntry previouslySelected = this.selected;
        this.init(width, height);
        this.search.setValue(s);
        this.selected = previouslySelected;
        this.showTags.setValue(showTagsValue);
        if (!this.search.getValue().isEmpty()) {
            reloadStructures();
        }
        if (sort != SortType.NORMAL) {
            resortStructures(sort);
        }
        updateCache();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }
}
