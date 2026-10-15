package de.ellpeck.actuallyadditions.mod.inventory.gui;

import net.minecraft.ChatFormatting;

import net.minecraft.client.input.KeyEvent;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import de.ellpeck.actuallyadditions.mod.inventory.ItemTagContainer;
import de.ellpeck.actuallyadditions.mod.network.packet.ButtonToContainerPacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.Optional;

public class ItemTagScreen extends AAScreen<ItemTagContainer> {
    private EditBox tagBox;
    private StringWidget testText;
    private boolean validTag = false;
    public ItemTagScreen(ItemTagContainer container, Inventory inventory, Component pTitle) {
        super(container, inventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();

        this.testText = new StringWidget(this.leftPos + 7, this.topPos + 27, 162, 20, Component.literal(""), font);
        this.addRenderableWidget(this.testText);

        this.tagBox = new EditBox(this.font, this.leftPos + 7, this.topPos + 7, 162, 20, Component.nullToEmpty("Tag"));
        this.tagBox.setMaxLength(128);
        this.tagBox.setCanLoseFocus(false);
        setInitialFocus(this.tagBox);
        this.tagBox.setValue("");
        this.tagBox.setResponder(this::textChanged);

        this.addRenderableWidget(this.tagBox);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.extractBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

    }

    private void textChanged(String text) {
        boolean valid = validateTag(text);

        if (valid) {
            this.tagBox.setTextColor(0xFF00FF00);
            TagKey<Item> tagKey = TagKey.create(Registries.ITEM, Identifier.tryParse(text));
            Optional<HolderSet.Named<Item>> optionalNamed = BuiltInRegistries.ITEM.get(tagKey);

            if (optionalNamed.isPresent()) {
                this.testText.setMessage(Component.literal("Valid Tag").withStyle(ChatFormatting.GREEN));
                validTag = true;
            } else {
                this.testText.setMessage(Component.literal("Invalid Tag").withStyle(ChatFormatting.RED));
            }
        }
        else {
            tagBox.setTextColor(0xFFFF0000);
            validTag = false;
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            this.minecraft.player.closeContainer();
        }
        if (event.key() == GLFW.GLFW_KEY_ENTER && validTag) {
//            data.putString("Tag", tagBox.getValue()); TODO: This value was never used by the old packet (GUI_BUTTON_TO_CONTAINER_HANDLER)
            ClientPacketDistributor.sendToServer(new ButtonToContainerPacket(
                    minecraft.level.dimension().identifier(),
                    minecraft.player.getId(),
                    0
            ));

            this.minecraft.player.closeContainer();
        }

        return tagBox.keyPressed(event) || tagBox.canConsumeInput() || super.keyPressed(event);
    }
    private boolean validateTag(String tag) {
        return Identifier.tryParse(tag) != null;
        //return !tag.matches("^[a-z0-9_\\-]+:[a-z0-9_\\-]+(/[a-z0-9_\\-]+)*$");
    }
}
