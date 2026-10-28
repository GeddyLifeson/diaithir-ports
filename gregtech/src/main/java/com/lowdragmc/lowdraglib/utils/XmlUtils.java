package com.lowdragmc.lowdraglib.utils;

import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;
import lombok.val;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Vector3f;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;

/**
 * @author KilaBash
 * @date 2022/9/4
 * @implNote XmlUtils
 */
public class XmlUtils {
    public final static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    @Nullable
    public static Document loadXml(InputStream inputstream) {
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            return documentBuilder.parse(inputstream);
        } catch (Exception e) {
            return null;
        }
    }

    public static int getAsInt(Element element, String name, int defaultValue) {
        if (element.hasAttribute(name)) {
            try {
                return Integer.parseInt(element.getAttribute(name));
            } catch (Exception ignored) {

            }
        }
        return defaultValue;
    }

    public static long getAsLong(Element element, String name, long defaultValue) {
        if (element.hasAttribute(name)) {
            try {
                return Long.parseLong(element.getAttribute(name));
            } catch (Exception ignored) {

            }
        }
        return defaultValue;
    }

    public static boolean getAsBoolean(Element element, String name, boolean defaultValue) {
        if (element.hasAttribute(name)) {
            try {
                return Boolean.parseBoolean(element.getAttribute(name));
            } catch (Exception ignored) {

            }
        }
        return defaultValue;
    }

    public static String getAsString(Element element, String name, String defaultValue) {
        if (element.hasAttribute(name)) {
            return element.getAttribute(name);
        }
        return defaultValue;
    }

    public static float getAsFloat(Element element, String name, float defaultValue) {
        if (element.hasAttribute(name)) {
            try {
                return Float.parseFloat(element.getAttribute(name));
            } catch (Exception ignored) {

            }
        }
        return defaultValue;
    }

    public static int getAsColor(Element element, String name, int defaultValue) {
        if (element.hasAttribute(name)) {
            try {
                var value = Long.decode(element.getAttribute(name)).intValue();
                if (value != 0 && ((value & 0xff000000) == 0)) {
                    value = value | 0xff000000;
                }
                return value;
            } catch (Exception ignored) {

            }
        }
        return defaultValue;
    }

    public static BlockPos getAsBlockPos(Element element, String name, BlockPos defaultValue) {
        if (element.hasAttribute(name)) {
            String pos = getAsString(element, name, "0 0 0");
            try {
                var s = pos.split(" ");
                return new BlockPos(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]));
            } catch (Exception ignored) {}
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> T getAsEnum(Element element, String name, Class<T> enumClass, T defaultValue) {
        if (element.hasAttribute(name)) {
            try {
                String data = element.getAttribute(name);
                Enum<T>[] values = enumClass.getEnumConstants();
                for (Enum<T> value : values) {
                    if (value.name().equals(data)) {
                        return (T)value;
                    }
                }
            } catch (Exception ignored) {

            }
        }
        return defaultValue;
    }

    public static CompoundTag getCompoundTag(Element element) { return null; /* ported stub */ }

    public static DataComponentMap getComponents(Element element) {
        NodeList nodeList = element.getChildNodes();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String text = node.getTextContent().replaceAll("\\h*\\R+\\h*", " ");
            if (!text.isEmpty() && text.charAt(0) == ' ') {
                text = text.substring(1);
            }
            builder.append(text);
        }
        if (!builder.isEmpty()) {
            return DataComponentMap.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(builder.toString())).result().orElse(DataComponentMap.EMPTY);
        }
        return DataComponentMap.EMPTY;
    }

    public static ItemStack getItemStack(Element element) {
        var ingredient = getIngredient(element);
        if (com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions.getItems(ingredient.ingredient).length > 0) {
            var stack = com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions.getItems(ingredient.ingredient)[0];
            stack.setCount(ingredient.count);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    public static Vector3f getAsVector3f(Element element, String name, Vector3f defaultValue) {
        if (element.hasAttribute(name)) {
            String pos = getAsString(element, name, "0 0 0");
            try {
                var s = pos.split(" ");
                return new Vector3f(Float.parseFloat(s[0]), Float.parseFloat(s[1]), Float.parseFloat(s[2]));
            } catch (Exception ignored) {}
        }
        return defaultValue;
    }

    public static Vec2 getAsVec2(Element element, String name, Vec2 defaultValue) {
        if (element.hasAttribute(name)) {
            String pos = getAsString(element, name, "0 0");
            try {
                var s = pos.split(" ");
                return new Vec2(Float.parseFloat(s[0]), Float.parseFloat(s[1]));
            } catch (Exception ignored) {}
        }
        return defaultValue;
    }

    public static EntityInfo getEntityInfo(Element element) {
        int id = getAsInt(element, "id", LDLib.RANDOM.nextInt());
        EntityType<?> entityType = null;
        if (element.hasAttribute("type")) {
            entityType = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(element.getAttribute("type")));
        }
        CompoundTag tag = null;
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i) instanceof Element subElement && subElement.getNodeName().equals("nbt")) {
                tag = getCompoundTag(subElement);
                break;
            }
        }
        return new EntityInfo(id, entityType, tag);
    }

    public record SizedIngredient(Ingredient ingredient, int count) {};

    public static SizedIngredient getIngredient(Element element) { return null; /* ported stub */ }

    public static FluidStack getFluidStack(Element element) { return null; /* ported stub */ }

    public static BlockInfo getBlockInfo(Element element) { return null; /* ported stub */ }

    public static BlockState setBlockState(BlockState blockState, Element element) {
        StateDefinition<Block, BlockState> stateDefinition = blockState.getBlock().getStateDefinition();
        var name = getAsString(element, "name", "");
        var value = getAsString(element, "value", "");
        if (!name.isEmpty()) {
            var property = stateDefinition.getProperty(name);
            if (property != null) {
                blockState = setValueHelper(blockState, property, value);
            }
        }

        return blockState;
    }

    private static BlockState setValueHelper(BlockState stateHolder, Property property, String value) {
        var optional = property.getValue(value);
        if (optional.isPresent()) {
            return stateHolder.setValue(property, (Comparable) optional.get());
        }
        return stateHolder;
    }

    public static String getContent(Element element, boolean pretty) {
        NodeList nodeList = element.getChildNodes();
        StringBuilder builder = new StringBuilder();
        boolean lastNodeIsText = false;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.TEXT_NODE) {
                String text = node.getTextContent();
                if (pretty) {
                    text = text.replaceAll("\\h*\\R+\\h*", " ");
                }
                if (!lastNodeIsText && !text.isEmpty() && text.charAt(0) == ' ') {
                    text = text.substring(1);
                }
                builder.append(text);
                lastNodeIsText = false;
            } else if (node instanceof Element nodeElement) {
                var nodeName = nodeElement.getNodeName();
                lastNodeIsText = false;
                switch (nodeName) {
                    case "lang" -> {
                        var key = XmlUtils.getAsString(nodeElement, "key", "");
                        builder.append(LocalizationUtils.format(key));
                        lastNodeIsText = true;
                    }
                    case "br" -> builder.append('\n');
                }
            }
        }
        return builder.toString();
    }

    public static List<MutableComponent> getComponents(Element element, Style style) { return null; /* ported stub */ }
}
