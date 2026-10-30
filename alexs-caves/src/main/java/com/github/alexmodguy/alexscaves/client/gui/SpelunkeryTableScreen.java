package com.github.alexmodguy.alexscaves.client.gui;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.inventory.SpelunkeryTableMenu;
import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import com.github.alexmodguy.alexscaves.server.item.CaveInfoItem;
import com.github.alexmodguy.alexscaves.server.message.SpelunkeryTableChangeMessage;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.IOUtils;
import org.joml.Matrix4f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import net.neoforged.neoforge.network.PacketDistributor;

public class SpelunkeryTableScreen extends AbstractContainerScreen<SpelunkeryTableMenu> {

    protected static final Style GLYPH_FONT = Style.EMPTY
            .withFont(new net.minecraft.network.chat.FontDescription.Resource(Identifier.fromNamespaceAndPath("minecraft", "alt")));

    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID,
            "textures/gui/spelunkery_table.png");
    public static final Identifier TABLET_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID,
            "textures/gui/spelunkery_table_tablet.png");
    public static final Identifier WIDGETS_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID,
            "textures/gui/spelunkery_table_widgets.png");
    public static final Identifier DEFAULT_WORDS = Identifier.fromNamespaceAndPath(AlexsCaves.MODID,
            "minigame/en_us/magnetic_caves.txt");
    private int tickCount = 0;

    private int attemptsLeft = 0;

    private boolean draggingMagnify = false;

    private float magnifyPosX;
    private float magnifyPosY;

    private float prevMagnifyPosX;
    private float prevMagnifyPosY;

    private int lastMouseX;
    private int lastMouseY;

    private Identifier prevWordsFile = null;
    private List<SpelunkeryTableWordButton> wordButtons = new ArrayList<>();
    private SpelunkeryTableWordButton targetWordButton = null;
    private int highlightColor = 0XFFFFFF;
    private int level = 0;
    private boolean finishedLevel;

    private float prevPassLevelProgress = 0;
    private float passLevelProgress = 0;

    private int tutorialStep = 0;
    private boolean hasClickedLens = false;
    private boolean doneWithTutorial = false;
    private boolean invalidTablet = false;
    private ItemStack lastTablet;

    private Random random = new Random();

    public SpelunkeryTableScreen(SpelunkeryTableMenu menu, Inventory inventory, Component name) {
        super(menu, inventory, name, 208, 256);
        this.titleLabelX = this.imageWidth / 2;
    }

    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        magnifyPosX = this.leftPos + 170;
        prevMagnifyPosX = magnifyPosX;
        magnifyPosY = this.topPos + 130;
        prevMagnifyPosY = magnifyPosY;
        for (SpelunkeryTableWordButton button : wordButtons) {
            this.addRenderableWidget(button);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int x, int y, float partialTick) {
        super.extractBackground(guiGraphics, x, y, partialTick);
        this.renderBg(guiGraphics, partialTick, x, y);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int x, int y, float partialTick) {
        super.extractRenderState(guiGraphics, x, y, partialTick);
        this.renderMagnify(guiGraphics, partialTick);
        this.renderDescText(guiGraphics);
        this.renderTabletText(guiGraphics);
    }

    private void renderDescText(GuiGraphicsExtractor guiGraphics) {
        int i = this.leftPos - 58;
        int j = this.topPos;
        if (invalidTablet) {
            Component badTablet = Component.translatable("alexscaves.container.spelunkery_table.bad_tablet");
            guiGraphics.text(font, badTablet, leftPos + 105 - (font.width(badTablet) / 2), j + 60, 0X000000,
                    false);
            // Invalid tablet display simplified for 1.21 - uses DataComponents instead of NBT

        } else if (targetWordButton != null && hasTablet() && hasPaper()) {
            Component find = Component.translatable("alexscaves.container.spelunkery_table.find");
            Component attempts = Component.translatable("alexscaves.container.spelunkery_table.attempts");
            guiGraphics.text(font, find, i + 20 - (font.width(find) / 2), j + 20, 0X99876C, false);
            guiGraphics.text(font, targetWordButton.getNormalText(),
                    i + 20 - (font.width(targetWordButton.getNormalText()) / 2), j + 35, highlightColor, false);
            guiGraphics.text(font, attempts, i + 20 - (font.width(attempts) / 2), j + 60, 0X99876C, false);
            int tallySpace = 0;
            for (int tally = 1; tally <= attemptsLeft; tally++) {
                if (tally % 5 == 0) {
                    guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, WIDGETS_TEXTURE, i + 10 + tallySpace - 22, j + 70, (float)(3), (float)(52), 27, 14, 256, 256);
                    tallySpace += 7;
                } else {
                    guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, WIDGETS_TEXTURE, i + 10 + tallySpace, j + 70, (float)(0), (float)(52), 3, 14, 256, 256);
                    guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, WIDGETS_TEXTURE, i + 10 + tallySpace, j + 70, (float)(0), (float)(52), 3, 14, 256, 256);
                    tallySpace += 4;
                }
            }
        }
    }

    private void renderTabletText(GuiGraphicsExtractor guiGraphics) {
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float x = getMagnifyPosX(partialTick);
        float y = getMagnifyPosY(partialTick);
        if (hasTablet()) {
            guiGraphics.pose().pushMatrix();
            for (SpelunkeryTableWordButton tableWordButton : wordButtons) {
                {
                    tableWordButton.renderTranslationText(tickCount, highlightColor, guiGraphics, font, x + 5, x + 32,
                            y + 6, y + 32);
                }
            }
            guiGraphics.pose().popMatrix();
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (isFirstTimeUsing() && tutorialStep < 6) {
            int i = this.leftPos;
            int j = this.topPos;
            int exclaimX = 0;
            int exclaimY = 0;
            if (tutorialStep == 0) {
                exclaimX = 54;
                exclaimY = 143;
                if (mouseX > i + exclaimX - 5 && mouseY > j + exclaimY - 5 && mouseX < i + exclaimX + 15
                        && mouseY < j + exclaimY + 15) {
                    Component tabletName = Component.translatable(ACItemRegistry.CAVE_TABLET.get().getDescriptionId())
                            .withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.YELLOW);
                    List<Component> step1Tooltip = List.of(
                            Component.translatable("alexscaves.container.spelunkery_table.slot_info_tablet_0",
                                    tabletName).withStyle(ChatFormatting.GRAY),
                            Component.translatable("alexscaves.container.spelunkery_table.slot_info_tablet_1")
                                    .withStyle(ChatFormatting.GRAY));
                    guiGraphics.setTooltipForNextFrame(font, step1Tooltip, Optional.empty(), mouseX, mouseY);
                }
            } else if (tutorialStep == 1) {
                exclaimX = 74;
                exclaimY = 143;
                if (mouseX > i + exclaimX - 5 && mouseY > j + exclaimY - 5 && mouseX < i + exclaimX + 15
                        && mouseY < j + exclaimY + 15) {
                    Component paperName = Component.translatable(Items.PAPER.getDescriptionId())
                            .withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.WHITE);
                    List<Component> step1Tooltip = List.of(
                            Component.translatable("alexscaves.container.spelunkery_table.slot_info_paper", paperName)
                                    .withStyle(ChatFormatting.GRAY));
                    guiGraphics.setTooltipForNextFrame(font, step1Tooltip, Optional.empty(), mouseX, mouseY);
                }
            } else if (tutorialStep == 2) {
                exclaimX = 170;
                exclaimY = 23;
                if (mouseX > i + exclaimX - 5 && mouseY > j + exclaimY - 5 && mouseX < i + exclaimX + 15
                        && mouseY < j + exclaimY + 15) {
                    List<Component> step1Tooltip = List
                            .of(Component.translatable("alexscaves.container.spelunkery_table.translate")
                                    .withStyle(ChatFormatting.GRAY));
                    guiGraphics.setTooltipForNextFrame(font, step1Tooltip, Optional.empty(), mouseX, mouseY);
                }
            } else if (tutorialStep == 3) {
                exclaimX = 185;
                exclaimY = 140;
                if (mouseX > i + exclaimX - 5 && mouseY > j + exclaimY - 5 && mouseX < i + exclaimX + 15
                        && mouseY < j + exclaimY + 15) {
                    List<Component> step1Tooltip = List
                            .of(Component.translatable("alexscaves.container.spelunkery_table.glass")
                                    .withStyle(ChatFormatting.GRAY));
                    guiGraphics.setTooltipForNextFrame(font, step1Tooltip, Optional.empty(), mouseX, mouseY);
                }
            } else if (tutorialStep == 4) {
                exclaimX = -15;
                exclaimY = 15;
                if (mouseX > i + exclaimX - 5 && mouseY > j + exclaimY - 5 && mouseX < i + exclaimX + 15
                        && mouseY < j + exclaimY + 15) {
                    List<Component> step1Tooltip = List
                            .of(Component.translatable("alexscaves.container.spelunkery_table.guess_name")
                                    .withStyle(ChatFormatting.GRAY));
                    guiGraphics.setTooltipForNextFrame(font, step1Tooltip, Optional.empty(), mouseX, mouseY);
                }
            } else if (tutorialStep == 5) {
                exclaimX = 35;
                exclaimY = 142;
                if (mouseX > i + exclaimX - 5 && mouseY > j + exclaimY - 5 && mouseX < i + exclaimX + 15
                        && mouseY < j + exclaimY + 15) {
                    Component scrollName = Component.translatable(ACItemRegistry.CAVE_CODEX.get().getDescriptionId())
                            .withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.YELLOW);
                    int toDoLevels = Math.max(0, 3 - level);
                    List<Component> step1Tooltip = List.of(Component
                            .translatable(toDoLevels == 1 ? "alexscaves.container.spelunkery_table.level"
                                    : "alexscaves.container.spelunkery_table.levels", toDoLevels, scrollName)
                            .withStyle(ChatFormatting.GRAY));
                    guiGraphics.setTooltipForNextFrame(font, step1Tooltip, Optional.empty(), mouseX, mouseY);
                }
            }
            guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, WIDGETS_TEXTURE, i + exclaimX, j + exclaimY, (float)(tickCount % 20 < 10 ? 7 : 0), (float)(70), 7, 16, 256, 256);

        }
        super.extractTooltip(guiGraphics, mouseX, mouseY);
    }

    public float getMagnifyPosX(float f) {
        return prevMagnifyPosX + (magnifyPosX - prevMagnifyPosX) * f;
    }

    public float getMagnifyPosY(float f) {
        return prevMagnifyPosY + (magnifyPosY - prevMagnifyPosY) * f;
    }

    private void renderMagnify(GuiGraphicsExtractor guiGraphics, float partialTick) {
        float actualPartialTick = Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks();
        int lerpX = (int) getMagnifyPosX(actualPartialTick);
        int lerpY = (int) getMagnifyPosY(actualPartialTick);
        guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, WIDGETS_TEXTURE, lerpX, lerpY, 0F, 14F, 38, 38, 256, 256);
    }

    protected void renderBg(GuiGraphicsExtractor guiGraphics, float f, int x, int y) {
        int i = this.leftPos;
        int j = this.topPos;
        guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, (float)(0), (float)(0), this.imageWidth, this.imageHeight, 256, 256);
        for (int bulb = 0; bulb < Math.min(level, 3); bulb++) {
            guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, WIDGETS_TEXTURE, i + 92 + bulb * 15, j + 143, (float)(0), (float)(0), 13, 14, 256, 256);
        }
        if (hasPaper()) {
            guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, WIDGETS_TEXTURE, i - 80, j + 10, (float)(176), (float)(0), 80, 149, 256, 256);
        }
        int tablet = hasTablet() ? attemptsLeft <= 1 ? 2 : 1 : 0;
        if (tablet > 0) {
            guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TABLET_TEXTURE, i + 20, j + 19, (float)(0), (float)((tablet - 1) * 121), 168, 120, 256, 256);
        }
    }

    public boolean hasTablet() {
        return menu.getSlot(0).hasItem() && menu.getSlot(0).getItem().is(ACItemRegistry.CAVE_TABLET.get());
    }

    public boolean hasPaper() {
        return menu.getSlot(1).hasItem() && menu.getSlot(1).getItem().is(Items.PAPER);
    }

    public boolean isFirstTimeUsing() {
        return !AlexsCaves.PROXY.isSpelunkeryTutorialComplete();
    }

    protected void containerTick() {
        tickCount++;
        if (lastTablet == null && hasTablet()) {
            lastTablet = menu.getSlot(0).getItem();
        } else if (lastTablet != null && hasTablet() && lastTablet != menu.getSlot(0).getItem()) {
            lastTablet = menu.getSlot(0).getItem();
            invalidTablet = false;
            fullResetWords();
        }
        this.prevMagnifyPosX = magnifyPosX;
        this.prevMagnifyPosY = magnifyPosY;
        this.prevPassLevelProgress = passLevelProgress;
        int targetMagnifyX;
        int targetMagnifyY;
        int maxDistance;
        if (draggingMagnify) {
            targetMagnifyX = lastMouseX - 19;
            targetMagnifyY = lastMouseY - 19;
            maxDistance = 15;
        } else {
            targetMagnifyX = this.leftPos + 170;
            targetMagnifyY = this.topPos + 130;
            maxDistance = 20;
        }
        Vec3 vec3 = new Vec3(targetMagnifyX - this.magnifyPosX, targetMagnifyY - this.magnifyPosY, 0.0);
        if (vec3.length() > maxDistance) {
            vec3 = vec3.normalize().scale(maxDistance);
        }
        this.magnifyPosX += vec3.x;
        this.magnifyPosY += vec3.y;

        if (finishedLevel && passLevelProgress < 10.0F) {
            passLevelProgress += 0.5F;
        }
        if (!finishedLevel && passLevelProgress > 0.0F) {
            passLevelProgress -= 0.5F;
        }
        boolean resetTabletFromWin = finishedLevel && passLevelProgress >= 10.0F && attemptsLeft > 0;
        if (!menu.getSlot(0).hasItem()) {
            prevWordsFile = null;
            invalidTablet = false;
        } else if (prevWordsFile == null || resetTabletFromWin) {
            prevWordsFile = getWordsForItem(menu.getSlot(0).getItem());
            if (prevWordsFile == null) {
                clearWordWidgets();
            } else {
                finishedLevel = false;
                generateWords(prevWordsFile);
            }
        }
        int currentColor = menu.getHighlightColor(Minecraft.getInstance().level);
        if (currentColor != -1) {
            highlightColor = currentColor;
        }
        if (resetTabletFromWin && level >= 3) {
            doneWithTutorial = true;
            menu.setTutorialComplete(Minecraft.getInstance().player, true);
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new SpelunkeryTableChangeMessage(true));
            level = 0;
            fullResetWords();
        } else if (finishedLevel && passLevelProgress >= 10.0F && attemptsLeft <= 0) {
            level = 0;
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new SpelunkeryTableChangeMessage(false));
            fullResetWords();
            Minecraft.getInstance().setScreen(null);
        }
        if (!hasTablet() && !wordButtons.isEmpty()) {
            clearWordWidgets();
        }
        if (doneWithTutorial) {
            tutorialStep = 6;
        } else if (!hasTablet()) {
            tutorialStep = 0;
        } else if (!hasPaper()) {
            tutorialStep = 1;
        } else if (attemptsLeft == 5 && level == 0) {
            tutorialStep = 2;
        } else if (!hasClickedLens) {
            tutorialStep = 3;
        } else if (level == 0) {
            tutorialStep = 4;
        } else {
            tutorialStep = 5;
        }
    }

    public void fullResetWords() {
        clearWordWidgets();
        prevWordsFile = getWordsForItem(menu.getSlot(0).getItem());
        if (prevWordsFile != null) {
            generateWords(prevWordsFile);
        }
    }

    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        boolean prev = super.mouseClicked(event, doubleClick);
        if (!prev) {

        }
        return prev;
    }

    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dx, double dy) {
        double width = event.x();
        double height = event.y();
        boolean prev = super.mouseDragged(event, dx, dy);
        if (prev) {
            lastMouseX = (int) width;
            lastMouseY = (int) height;
            if (!draggingMagnify && lastMouseX >= this.magnifyPosX && lastMouseX <= this.magnifyPosX + 38
                    && lastMouseY >= this.magnifyPosY && lastMouseY <= this.magnifyPosY + 38) {
                draggingMagnify = true;
                if (tutorialStep > 2) {
                    hasClickedLens = true;
                }
            }
        }
        return prev;
    }

    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
        draggingMagnify = false;
        return super.mouseReleased(event);
    }

    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int x, int y) {
        guiGraphics.text(font, this.title, this.titleLabelX - (font.width(title) / 2), this.titleLabelY, 4210752,
                false);
    }

    private Identifier getWordsForItem(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != ACItemRegistry.CAVE_TABLET.get()) {
            return null;
        }
        String s1 = getMinigameStr(stack) + ".txt";
        String lang = Minecraft.getInstance().getLanguageManager().getSelected().toLowerCase();
        Identifier resourceLocation = Identifier.fromNamespaceAndPath(AlexsCaves.MODID,
                "minigame/" + lang + "/" + s1);
        try {
            InputStream is = Minecraft.getInstance().getResourceManager().open(resourceLocation);
            is.close();
        } catch (Exception var4) {
            AlexsCaves.LOGGER.warn("Could not find language file for translation, defaulting to english");
            resourceLocation = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "minigame/en_us/" + s1);
        }
        return resourceLocation;
    }

    private String getMinigameStr(ItemStack stack) {
        ResourceKey<Biome> biomeResourceKey = CaveInfoItem.getCaveBiome(stack);
        return biomeResourceKey == null ? "magnetic_caves" : biomeResourceKey.identifier().getPath();
    }

    private void clearWordWidgets() {
        for (SpelunkeryTableWordButton button : wordButtons) {
            this.removeWidget(button);
        }
        wordButtons.clear();
    }

    private void addWordWidget(SpelunkeryTableWordButton button) {
        wordButtons.add(button);
        this.addRenderableWidget(button);
    }

    private void generateWords(Identifier file) {
        clearWordWidgets();
        List<String> allWords;
        try {
            BufferedReader bufferedreader = Minecraft.getInstance().getResourceManager().openAsReader(file);
            allWords = IOUtils.readLines(bufferedreader);
        } catch (IOException e) {
            allWords = new ArrayList<>();
            this.invalidTablet = true;
            AlexsCaves.LOGGER.error("Could not load in spelunkery minigame file {}", file);
        }
        int maxWidth = 160;
        int maxLines = 8;
        int wordLines = 0;
        int wordLineWidth = 0;
        int letterWidth = 6;
        Collections.shuffle(allWords);
        while (wordLines < maxLines && !allWords.isEmpty()) {
            MutableComponent component = Component.literal(allWords.remove(0));
            int maxWordWidth = component.getString().length() * letterWidth;
            while (wordLineWidth + maxWordWidth + 30 < maxWidth && !allWords.isEmpty()) {
                component = Component.literal(allWords.remove(0).toUpperCase());
                maxWordWidth = component.getString().length() * letterWidth;
                SpelunkeryTableWordButton tableWordButton = new SpelunkeryTableWordButton(this, this.font,
                        25 + wordLineWidth, 25 + 12 * wordLines, maxWordWidth, 12, component.withStyle(Style.EMPTY));
                this.addWordWidget(tableWordButton);
                wordLineWidth += maxWordWidth;
            }
            wordLineWidth = 0;
            wordLines++;
        }
        if (!wordButtons.isEmpty()) {
            if (Minecraft.getInstance().level != null) {
                targetWordButton = wordButtons.size() <= 1 ? wordButtons.get(0)
                        : wordButtons.get(random.nextInt(wordButtons.size()));
            }
            attemptsLeft = 5;
        } else {
            targetWordButton = null;
        }
    }

    public int getHighlightColor() {
        return highlightColor;
    }

    public void onClickWord(SpelunkeryTableWordButton tableWordButton) {
        if (finishedLevel) {
            return;
        }
        if (tableWordButton == targetWordButton) {
            level++;
            Minecraft.getInstance().getSoundManager()
                    .play(SimpleSoundInstance.forUI(level >= 3 ? ACSoundRegistry.SPELUNKERY_TABLE_SUCCESS_COMPLETE.get()
                            : ACSoundRegistry.SPELUNKERY_TABLE_SUCCESS.get(), 1.0F));
            finishedLevel = true;
        } else {
            if (attemptsLeft > 0) {
                attemptsLeft--;
            }
            if (attemptsLeft <= 1) {
                Minecraft.getInstance().getSoundManager()
                        .play(SimpleSoundInstance.forUI(ACSoundRegistry.SPELUNKERY_TABLE_CRACK.get(), 1.0F));
            } else {
                Minecraft.getInstance().getSoundManager()
                        .play(SimpleSoundInstance.forUI(ACSoundRegistry.SPELUNKERY_TABLE_ATTEMPT_FAIL.get(), 1.0F));
            }
            if (attemptsLeft <= 0) {
                finishedLevel = true;
            }
        }
    }

    public float getRevealWordsAmount(float partialTick) {
        if (finishedLevel) {
            return Math.min((prevPassLevelProgress + (passLevelProgress - prevPassLevelProgress) * partialTick) * 0.33F,
                    1F);
        } else {
            return 0.0F;
        }
    }

    public boolean isTargetWord(SpelunkeryTableWordButton tableWordButton) {
        return targetWordButton == tableWordButton;
    }

    private boolean hasClickedAnyWord() {
        boolean flag = false;
        for (SpelunkeryTableWordButton button : wordButtons) {
            if (!button.active) {
                flag = true;
            }
        }
        return flag;
    }

    public void onClose() {
        if (hasPaper() && hasTablet() && hasClickedAnyWord() && level < 3) {
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new SpelunkeryTableChangeMessage(false));
        }
        super.onClose();
    }
}
