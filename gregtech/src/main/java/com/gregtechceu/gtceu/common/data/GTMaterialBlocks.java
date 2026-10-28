package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.MaterialBlockItem;
import com.gregtechceu.gtceu.api.item.MaterialPipeBlockItem;
import com.gregtechceu.gtceu.api.item.SurfaceRockBlockItem;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.block.*;
import com.gregtechceu.gtceu.common.pipelike.cable.Insulation;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.FluidPipeType;
import com.gregtechceu.gtceu.common.pipelike.item.ItemPipeType;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import java.util.Map;

@SuppressWarnings("removal")
public class GTMaterialBlocks {

    // Reference Table Builders
    static ImmutableTable.Builder<TagPrefix, Material, BlockEntry<? extends Block>> MATERIAL_BLOCKS_BUILDER = ImmutableTable
            .builder();
    static ImmutableMap.Builder<Material, BlockEntry<SurfaceRockBlock>> SURFACE_ROCK_BLOCKS_BUILDER = ImmutableMap
            .builder();
    static ImmutableTable.Builder<TagPrefix, Material, BlockEntry<CableBlock>> CABLE_BLOCKS_BUILDER = ImmutableTable
            .builder();
    static ImmutableTable.Builder<TagPrefix, Material, BlockEntry<FluidPipeBlock>> FLUID_PIPE_BLOCKS_BUILDER = ImmutableTable
            .builder();
    static ImmutableTable.Builder<TagPrefix, Material, BlockEntry<ItemPipeBlock>> ITEM_PIPE_BLOCKS_BUILDER = ImmutableTable
            .builder();

    // Reference Tables
    public static Table<TagPrefix, Material, BlockEntry<? extends Block>> MATERIAL_BLOCKS;
    public static Map<Material, BlockEntry<SurfaceRockBlock>> SURFACE_ROCK_BLOCKS;
    public static Table<TagPrefix, Material, BlockEntry<CableBlock>> CABLE_BLOCKS;
    public static Table<TagPrefix, Material, BlockEntry<FluidPipeBlock>> FLUID_PIPE_BLOCKS;
    public static Table<TagPrefix, Material, BlockEntry<ItemPipeBlock>> ITEM_PIPE_BLOCKS;

    // Material Blocks
    public static void generateMaterialBlocks() {
        GTCEu.LOGGER.debug("Generating GTCEu Material Blocks...");

        for (TagPrefix tagPrefix : GTRegistries.TAG_PREFIXES) {
            if (!TagPrefix.ORES.containsKey(tagPrefix) && tagPrefix.doGenerateBlock()) {
                for (Material material : GTCEuAPI.materialManager) {
                    GTRegistrate registrate = GTRegistrate.createIgnoringListenerErrors(material.getModid());
                    if (tagPrefix.doGenerateBlock(material)) {
                        registerMaterialBlock(tagPrefix, material, registrate);
                    }
                }
            }
        }
        GTCEu.LOGGER.debug("Generating GTCEu Material Blocks... Complete!");
    }

    private static void registerMaterialBlock(TagPrefix tagPrefix, Material material, GTRegistrate registrate) {  /* ported stub */ }

    // Material Ore Blocks
    public static void generateOreBlocks() {
        GTCEu.LOGGER.debug("Generating GTCEu Ore Blocks...");
        for (Material material : GTCEuAPI.materialManager) {
            if (allowOreBlock(material)) {
                GTRegistrate registrate = GTRegistrate.createIgnoringListenerErrors(material.getModid());
                registerOreBlock(material, registrate);
            }
        }
        GTCEu.LOGGER.debug("Generating GTCEu Ore Blocks... Complete!");
    }

    private static boolean allowOreBlock(Material material) {
        return material.hasProperty(PropertyKey.ORE);
    }

    private static void registerOreBlock(Material material, GTRegistrate registrate) {  /* ported stub */ }

    public static void buildMaterialBlockTable() {
        MATERIAL_BLOCKS = MATERIAL_BLOCKS_BUILDER.build();
        MATERIAL_BLOCKS_BUILDER = null;
    }

    // Material Ore Indicator Piles
    public static void generateOreIndicators() {
        GTCEu.LOGGER.debug("Generating GTCEu Surface Rock Indicator Blocks...");
        for (Material material : GTCEuAPI.materialManager) {
            if (allowOreIndicator(material)) {
                GTRegistrate registrate = GTRegistrate.createIgnoringListenerErrors(material.getModid());
                registerOreIndicator(material, registrate);
            }
        }
        SURFACE_ROCK_BLOCKS = SURFACE_ROCK_BLOCKS_BUILDER.build();
        SURFACE_ROCK_BLOCKS_BUILDER = null;
        GTCEu.LOGGER.debug("Generating GTCEu Surface Rock Indicator Blocks... Complete!");
    }

    private static boolean allowOreIndicator(Material material) {
        return material.hasProperty(PropertyKey.ORE);
    }

    private static void registerOreIndicator(Material material, GTRegistrate registrate) {  /* ported stub */ }

    // Material Cable & Wire Blocks
    public static void generateCableBlocks() {
        GTCEu.LOGGER.debug("Generating GTCEu Cable/Wire Blocks...");
        for (Insulation insulation : Insulation.values()) {
            for (Material material : GTCEuAPI.materialManager) {
                if (allowCableBlock(material, insulation)) {
                    GTRegistrate registrate = GTRegistrate.createIgnoringListenerErrors(material.getModid());
                    registerCableBlock(material, insulation, registrate);
                }
            }
        }
        CABLE_BLOCKS = CABLE_BLOCKS_BUILDER.build();
        CABLE_BLOCKS_BUILDER = null;
        GTCEu.LOGGER.debug("Generating GTCEu Cable/Wire Blocks... Complete!");
    }

    private static boolean allowCableBlock(Material material, Insulation insulation) {
        return material.hasProperty(PropertyKey.WIRE) && !insulation.tagPrefix.isIgnored(material) &&
                !(insulation.isCable && material.getProperty(PropertyKey.WIRE).isSuperconductor());
    }

    private static void registerCableBlock(Material material, Insulation insulation, GTRegistrate registrate) {  /* ported stub */ }

    // Material Fluid Pipe Blocks
    public static void generateFluidPipeBlocks() {
        GTCEu.LOGGER.debug("Generating GTCEu Fluid Pipe Blocks...");
        for (var fluidPipeType : FluidPipeType.values()) {
            for (Material material : GTCEuAPI.materialManager) {
                if (allowFluidPipeBlock(material, fluidPipeType)) {
                    GTRegistrate registrate = GTRegistrate.createIgnoringListenerErrors(material.getModid());
                    registerFluidPipeBlock(material, fluidPipeType, registrate);
                }
            }
        }
        FLUID_PIPE_BLOCKS = FLUID_PIPE_BLOCKS_BUILDER.build();
        FLUID_PIPE_BLOCKS_BUILDER = null;
        GTCEu.LOGGER.debug("Generating GTCEu Fluid Pipe Blocks... Complete!");
    }

    private static boolean allowFluidPipeBlock(Material material, FluidPipeType fluidPipeType) {
        return material.hasProperty(PropertyKey.FLUID_PIPE) && !fluidPipeType.tagPrefix.isIgnored(material);
    }

    private static void registerFluidPipeBlock(Material material, FluidPipeType fluidPipeType,
                                               GTRegistrate registrate) {  /* ported stub */ }

    // Material Item Pipe Blocks
    public static void generateItemPipeBlocks() {
        GTCEu.LOGGER.debug("Generating GTCEu Item Pipe Blocks...");
        for (var itemPipeType : ItemPipeType.values()) {
            for (Material material : GTCEuAPI.materialManager) {
                if (allowItemPipeBlock(material, itemPipeType)) {
                    GTRegistrate registrate = GTRegistrate.createIgnoringListenerErrors(material.getModid());
                    registerItemPipeBlock(material, itemPipeType, registrate);
                }
            }
        }
        ITEM_PIPE_BLOCKS = ITEM_PIPE_BLOCKS_BUILDER.build();
        ITEM_PIPE_BLOCKS_BUILDER = null;
        GTCEu.LOGGER.debug("Generating GTCEu Item Pipe Blocks... Complete!");
    }

    private static boolean allowItemPipeBlock(Material material, ItemPipeType itemPipeType) {
        return material.hasProperty(PropertyKey.ITEM_PIPE) && !itemPipeType.getTagPrefix().isIgnored(material);
    }

    private static void registerItemPipeBlock(Material material, ItemPipeType itemPipeType, GTRegistrate registrate) {  /* ported stub */ }
}
