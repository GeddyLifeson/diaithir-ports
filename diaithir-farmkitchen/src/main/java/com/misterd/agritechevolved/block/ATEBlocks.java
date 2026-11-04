package com.misterd.agritechevolved.block;

import com.misterd.agritechevolved.AgritechEvolved;
import com.misterd.agritechevolved.Config;
import com.misterd.agritechevolved.component.ATEDataComponents;
import com.misterd.agritechevolved.item.ATEItems;
import com.misterd.agritechevolved.block.custom.*;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lwjgl.glfw.GLFW;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

public class ATEBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AgritechEvolved.MODID);

    public static final DeferredBlock<Block> ACACIA_PLANTER = registerBlock("basic_acacia_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));

    public static final DeferredBlock<Block> BAMBOO_PLANTER = registerBlock("basic_bamboo_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));

    public static final DeferredBlock<Block> BIRCH_PLANTER = registerBlock("basic_birch_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));

    public static final DeferredBlock<Block> CHERRY_PLANTER = registerBlock("basic_cherry_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));

    public static final DeferredBlock<Block> CRIMSON_PLANTER = registerBlock("basic_crimson_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));

    public static final DeferredBlock<Block> DARK_OAK_PLANTER = registerBlock("basic_dark_oak_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));

    public static final DeferredBlock<Block> JUNGLE_PLANTER = registerBlock("basic_jungle_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));

    public static final DeferredBlock<Block> MANGROVE_PLANTER = registerBlock("basic_mangrove_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));

    public static final DeferredBlock<Block> OAK_PLANTER = registerBlock("basic_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));

    public static final DeferredBlock<Block> SPRUCE_PLANTER = registerBlock("basic_spruce_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));

    public static final DeferredBlock<Block> WARPED_PLANTER = registerBlock("basic_warped_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));

    public static final DeferredBlock<Block> PALE_OAK_PLANTER = registerBlock("basic_pale_oak_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));

    public static final DeferredBlock<Block> ADVANCED_PLANTER = registerBlock("advanced_planter",
            id -> new AdvancedPlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> BIOMASS_BURNER = registerBlock("biomass_burner",
            id -> new BiomassBurnerBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> COMPOSTER = registerBlock("composter",
            id -> new ComposterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> CAPACITOR_TIER_1 = registerBlock("capacitor_tier1",
            id -> new CapacitorTier1Block(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> CAPACITOR_TIER_2 = registerBlock("capacitor_tier2",
            id -> new CapacitorTier2Block(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> CAPACITOR_TIER_3 = registerBlock("capacitor_tier3",
            id -> new CapacitorTier3Block(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> COMPACTED_BIOMASS_BLOCK = registerBlock("compacted_biomass_block",
            id -> new Block(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(1.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> INFUSED_FARMLAND = registerBlock("infused_farmland",
            id -> new InfusedFarmlandBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(1.0F, 3.0F).sound(SoundType.MOSS).noOcclusion().randomTicks()));

    public static final DeferredBlock<Block> MULCH = registerBlock("mulch",
            id -> new MulchBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(1.0F, 3.0F).sound(SoundType.MOSS).noOcclusion()));

    public static final DeferredBlock<Block> SILO = registerBlock("silo",
            id -> new SiloBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> FERT_SPREADER = registerBlock("fertilizer_spreader",
            id -> new FertilizerSpreaderBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> TERRACOTTA_PLANTER = registerBlock("terracotta_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> BLACK_TERRACOTTA_PLANTER = registerBlock("black_terracotta_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> BLUE_TERRACOTTA_PLANTER = registerBlock("blue_terracotta_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> BROWN_TERRACOTTA_PLANTER = registerBlock("brown_terracotta_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> CYAN_TERRACOTTA_PLANTER = registerBlock("cyan_terracotta_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> GRAY_TERRACOTTA_PLANTER = registerBlock("gray_terracotta_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> GREEN_TERRACOTTA_PLANTER = registerBlock("green_terracotta_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> LIGHT_BLUE_TERRACOTTA_PLANTER = registerBlock("light_blue_terracotta_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> LIGHT_GRAY_TERRACOTTA_PLANTER = registerBlock("light_gray_terracotta_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> LIME_TERRACOTTA_PLANTER = registerBlock("lime_terracotta_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> MAGENTA_TERRACOTTA_PLANTER = registerBlock("magenta_terracotta_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> ORANGE_TERRACOTTA_PLANTER = registerBlock("orange_terracotta_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> PINK_TERRACOTTA_PLANTER = registerBlock("pink_terracotta_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> PURPLE_TERRACOTTA_PLANTER = registerBlock("purple_terracotta_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> RED_TERRACOTTA_PLANTER = registerBlock("red_terracotta_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> WHITE_TERRACOTTA_PLANTER = registerBlock("white_terracotta_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> YELLOW_TERRACOTTA_PLANTER = registerBlock("yellow_terracotta_planter",
            id -> new PlanterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<Identifier, T> factory) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, factory);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ATEItems.ITEMS.register(name, regName -> {

            if (name.equals("capacitor_tier1") || name.equals("capacitor_tier2") || name.equals("capacitor_tier3")) {
                return new BlockItem(block.get(),
                        new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AgritechEvolved.MODID, name))).useBlockDescriptionPrefix()) {

                    @Override
                    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {

                        Integer storedEnergy = stack.get(ATEDataComponents.STORED_ENERGY.get());

                        if (storedEnergy != null && storedEnergy > 0) {
                            NumberFormat format = NumberFormat.getNumberInstance(Locale.US);

                            adder.accept(Component.translatable("tooltip.agritechevolved.capacitor.stored_energy", format.format(storedEnergy)).withStyle(ChatFormatting.GOLD));
                        }
                    }
                };
            }

            if (name.equals("compacted_biomass_block")) {
                return new BlockItem(block.get(),
                        new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AgritechEvolved.MODID, name))).useBlockDescriptionPrefix()) {

                    @Override
                    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
                        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
                        int totalRF = Config.getBurnerCompactedBiomassBlockRfValue();
                        int burnDuration = Config.getBurnerCompactedBiomassBlockBurnDuration();
                        if (isShiftDown()) {
                            adder.accept(Component.translatable("tooltip.agritechevolved.compacted_biomass.rf_generation", fmt.format(totalRF)).withStyle(ChatFormatting.GREEN));
                            double burnSeconds = burnDuration / 20.0D;
                            adder.accept(Component.translatable("tooltip.agritechevolved.fuel.burn_duration", String.format("%.1f", burnSeconds)).withStyle(ChatFormatting.AQUA));
                            adder.accept(Component.translatable("tooltip.agritechevolved.fuel.rf_per_second", fmt.format((int) Math.round(totalRF / burnSeconds))).withStyle(ChatFormatting.YELLOW));
                        } else {
                            adder.accept(Component.translatable("tooltip.agritechevolved.crude_fuel.shift_info"));
                        }
                    }
                };
            }

            if (name.equals("infused_farmland")) {
                return new BlockItem(block.get(),
                        new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AgritechEvolved.MODID, name))).useBlockDescriptionPrefix()) {
                    @Override
                    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
                        adder.accept(Component.translatable("tooltip.agritechevolved.infused_farmland.growth_boost").withStyle(ChatFormatting.GREEN));
                        adder.accept(Component.translatable("tooltip.agritechevolved.infused_farmland.hoe_craft").withStyle(ChatFormatting.GRAY));
                        adder.accept(Component.translatable("tooltip.agritechevolved.infused_farmland.hoe_craft2").withStyle(ChatFormatting.GRAY));
                    }
                };
            }

            if (name.equals("mulch")) {
                return new BlockItem(block.get(),
                        new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AgritechEvolved.MODID, name))).useBlockDescriptionPrefix()) {
                    @Override
                    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
                        adder.accept(Component.translatable("tooltip.agritechevolved.mulch.growth_boost").withStyle(ChatFormatting.GREEN));
                        adder.accept(Component.translatable("tooltip.agritechevolved.mulch.hoe_craft").withStyle(ChatFormatting.GRAY));
                    }
                };
            }

            if (name.startsWith("basic_") && name.endsWith("_planter")) {
                return new BlockItem(block.get(),
                        new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AgritechEvolved.MODID, name))).useBlockDescriptionPrefix()) {
                    @Override
                    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
                        adder.accept(Component.translatable("tooltip.agritechevolved.planter.till").withStyle(ChatFormatting.GRAY));
                        adder.accept(Component.translatable("tooltip.agritechevolved.planter.fertilize").withStyle(ChatFormatting.GRAY));
                    }
                };
            }

            if (name.equals("advanced_planter")) {
                return new BlockItem(block.get(),
                        new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AgritechEvolved.MODID, name))).useBlockDescriptionPrefix()) {
                    @Override
                    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
                        adder.accept(Component.translatable("tooltip.agritechevolved.planter.till").withStyle(ChatFormatting.GRAY));
                    }
                };
            }

            if (name.equals("composter")) {
                return new BlockItem(block.get(),
                        new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AgritechEvolved.MODID, name))).useBlockDescriptionPrefix()) {
                    @Override
                    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
                        adder.accept(Component.translatable("tooltip.agritechevolved.composting_info").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
                    }
                };
            }

            return new BlockItem(block.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AgritechEvolved.MODID, name))).useBlockDescriptionPrefix());
        });
    }

    private static boolean isShiftDown() {
        Window window = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}