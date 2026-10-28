package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.compat.ArmorLayer;

import net.minecraft.world.item.equipment.ArmorMaterial;

import com.gregtechceu.gtceu.compat.ArmorItem;

import net.minecraft.world.item.equipment.ArmorType;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ArmorProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.api.item.datacomponents.ToolBehaviors;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IGTToolDefinition;
import com.gregtechceu.gtceu.api.item.tool.MaterialToolTier;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.item.armor.GTArmorItem;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import java.util.*;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.common.data.GTCreativeModeTabs.MATERIAL_ITEM;
import static com.gregtechceu.gtceu.common.data.GTCreativeModeTabs.TOOL;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class GTMaterialItems {

    // spotless:off

    // Reference Table Builders
    static ImmutableTable.Builder<TagPrefix, Material, ItemEntry<? extends Item>> MATERIAL_ITEMS_BUILDER = ImmutableTable.builder();

    // Reference Maps
    public static final Map<MaterialEntry, Supplier<? extends ItemLike>> toUnify = new HashMap<>();
    public static final Map<TagPrefix, TagPrefix> purifyMap = new HashMap<>();
    static {
        purifyMap.put(TagPrefix.crushed, TagPrefix.crushedPurified);
        purifyMap.put(TagPrefix.dustImpure, TagPrefix.dust);
        purifyMap.put(TagPrefix.dustPure, TagPrefix.dust);
    }

    // Reference Tables
    public static Table<TagPrefix, Material, ItemEntry<? extends Item>> MATERIAL_ITEMS;

    public final static Table<Material, GTToolType, ItemProviderEntry<Item, ? extends IGTTool>> TOOL_ITEMS = ArrayTable.create(
            GTCEuAPI.materialManager.stream().filter(mat -> mat.hasProperty(PropertyKey.TOOL)).toList(),
            GTToolType.getTypes().values().stream().toList());

    public static final Table<Material, ArmorType, ItemEntry<? extends ArmorItem>> ARMOR_ITEMS = ArrayTable.create(
            GTCEuAPI.materialManager.stream().filter(mat -> mat.hasProperty(PropertyKey.ARMOR)).toList(),
            Arrays.asList(ArmorType.values()));

    // spotless:on

    // Material Items
    public static void generateMaterialItems() {
        REGISTRATE.creativeModeTab(MATERIAL_ITEM);
        for (TagPrefix tagPrefix : GTRegistries.TAG_PREFIXES) {
            if (tagPrefix.doGenerateItem()) {
                for (Material material : GTCEuAPI.materialManager) {
                    GTRegistrate registrate = GTRegistrate.createIgnoringListenerErrors(material.getModid());
                    if (tagPrefix.doGenerateItem(material)) {
                        generateMaterialItem(tagPrefix, material, registrate);
                    }
                }
            }
        }
        MATERIAL_ITEMS = MATERIAL_ITEMS_BUILDER.build();
    }

    private static void generateMaterialItem(TagPrefix tagPrefix, Material material, GTRegistrate registrate) {  /* ported stub */ }

    // Material Tools
    public static void generateTools() {
        REGISTRATE.creativeModeTab(TOOL);
        for (GTToolType toolType : GTToolType.getTypes().values()) {
            for (Material material : GTCEuAPI.materialManager) {
                GTRegistrate registrate = GTRegistrate.createIgnoringListenerErrors(material.getModid());
                if (material.hasProperty(PropertyKey.TOOL)) {
                    var property = material.getProperty(PropertyKey.TOOL);
                    if (property.hasType(toolType)) {
                        generateTool(material, toolType, registrate);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void generateTool(final Material material, final GTToolType toolType, GTRegistrate registrate) {  /* ported stub */ }

    // Material Armors
    public static void generateArmors() {
        REGISTRATE.creativeModeTab(TOOL);
        for (ArmorType type : ArmorType.values()) {
            if (type.getSlot().getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;

            for (Material material : GTCEuAPI.materialManager) {
                GTRegistrate registrate = GTRegistrate.createIgnoringListenerErrors(material.getModid());
                if (material.hasProperty(PropertyKey.ARMOR)) {
                    generateArmor(material, type, registrate);
                }
            }
        }
    }

    private static void generateArmor(final Material material, final ArmorType type, GTRegistrate registrate) {  /* ported stub */ }
}
