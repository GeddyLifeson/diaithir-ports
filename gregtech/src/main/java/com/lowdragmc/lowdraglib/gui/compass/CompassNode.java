package com.lowdragmc.lowdraglib.gui.compass;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.json.SimpleIGuiTextureJsonUtils;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author KilaBash
 * @date 2023/7/27
 * @implNote CompassNode
 */
@Accessors(chain = true)
public class CompassNode {
    @Getter
    protected final JsonObject config;
    @Getter @Setter
    protected Identifier nodeName;
    @Getter
    protected CompassSection section;
    @Getter @Setter
    protected Position position;
    @Getter
    protected int size;
    @Getter @Setter @Nullable
    protected IGuiTexture background, hoverBackground;
    @Getter @Setter
    protected IGuiTexture buttonTexture;
    @Getter
    protected Set<CompassNode> preNodes = new HashSet<>();
    @Getter
    protected Set<CompassNode> childNodes = new HashSet<>();
    protected List<Item> items;

    public CompassNode(Identifier nodeName, JsonObject config) {
        this.config = config;
        this.nodeName = nodeName;
        this.background = config.has("background") ? SimpleIGuiTextureJsonUtils.fromJson(config.get("background").getAsJsonObject()) : null;
        this.hoverBackground = config.has("hover_background") ? SimpleIGuiTextureJsonUtils.fromJson(config.get("hover_background").getAsJsonObject()) : null;
        this.buttonTexture = SimpleIGuiTextureJsonUtils.fromJson(config.get("button_texture").getAsJsonObject());
        JsonArray position = config.get("position").getAsJsonArray();
        this.position = (new Position(position.get(0).getAsInt(), position.get(1).getAsInt()));
        this.size = GsonHelper.getAsInt(config, "size", 24);
    }

    public JsonObject updateJson() { return null; /* ported stub */ }

    public void setSection(CompassSection section) {
        this.section = section;
        this.section.addNode(this);
    }

    public void initRelation() {
        if (config.has("pre_nodes")) {
            JsonArray pre = config.get("pre_nodes").getAsJsonArray();
            for (JsonElement element : pre) {
                var nodeName = Identifier.parse(element.getAsString());
                CompassNode node = CompassManager.INSTANCE.getNodeByName(nodeName);
                if (node != null) {
                    preNodes.add(node);
                    node.childNodes.add(this);
                }
            }
        }
    }

    @Override
    public final String toString() {
        return nodeName.toString();
    }

    public Identifier getPage() {
        return Identifier.parse(GsonHelper.getAsString(config, "page", "ldlib:missing"));
    }

    public List<Item> getItems() { return null; /* ported stub */ }

    public Component getChatComponent() {
        return Component.translatable(nodeName.toLanguageKey("compass.node"));
    }
}
