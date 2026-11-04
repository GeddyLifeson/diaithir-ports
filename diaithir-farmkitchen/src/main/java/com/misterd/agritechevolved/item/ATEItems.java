package com.misterd.agritechevolved.item;

import com.misterd.agritechevolved.Config;
import com.misterd.agritechevolved.item.custom.ClocheItem;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lwjgl.glfw.GLFW;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;

public class ATEItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("agritechevolved");

    public static final DeferredItem<Item> SM_MK1 = ITEMS.registerItem("sm_mk1",
            props -> new Item(props) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
                    if (isShiftDown()) {
                        int speedBoost = (int) Math.round((Config.getSpeedModuleMk1Multiplier() - 1.0D) * 100.0D);
                        int powerIncrease = (int) Math.round((Config.getSpeedModuleMk1PowerMultiplier() - 1.0D) * 100.0D);
                        adder.accept(Component.translatable("tooltip.agritechevolved.module.speed_boost", speedBoost));
                        adder.accept(Component.translatable("tooltip.agritechevolved.module.power_increase", powerIncrease));
                    } else {
                        adder.accept(Component.translatable("tooltip.agritechevolved.module.shift_info"));
                    }
                }
            });

    public static final DeferredItem<Item> SM_MK2 = ITEMS.registerItem("sm_mk2",
            props -> new Item(props) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
                    if (isShiftDown()) {
                        int speedBoost = (int) Math.round((Config.getSpeedModuleMk2Multiplier() - 1.0D) * 100.0D);
                        int powerIncrease = (int) Math.round((Config.getSpeedModuleMk2PowerMultiplier() - 1.0D) * 100.0D);
                        adder.accept(Component.translatable("tooltip.agritechevolved.module.speed_boost", speedBoost));
                        adder.accept(Component.translatable("tooltip.agritechevolved.module.power_increase", powerIncrease));
                    } else {
                        adder.accept(Component.translatable("tooltip.agritechevolved.module.shift_info"));
                    }
                }
            });

    public static final DeferredItem<Item> SM_MK3 = ITEMS.registerItem("sm_mk3",
            props -> new Item(props) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
                    if (isShiftDown()) {
                        int speedBoost = (int) Math.round((Config.getSpeedModuleMk3Multiplier() - 1.0D) * 100.0D);
                        int powerIncrease = (int) Math.round((Config.getSpeedModuleMk3PowerMultiplier() - 1.0D) * 100.0D);
                        adder.accept(Component.translatable("tooltip.agritechevolved.module.speed_boost", speedBoost));
                        adder.accept(Component.translatable("tooltip.agritechevolved.module.power_increase", powerIncrease));
                    } else {
                        adder.accept(Component.translatable("tooltip.agritechevolved.module.shift_info"));
                    }
                }
            });

    public static final DeferredItem<Item> YM_MK1 = ITEMS.registerItem("ym_mk1",
            props -> new Item(props) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
                    if (isShiftDown()) {
                        int yieldBoost = (int) Math.round((Config.getYieldModuleMk1Multiplier() - 1.0D) * 100.0D);
                        int speedReduction = (int) Math.round((1.0D - Config.getYieldModuleMk1SpeedPenalty()) * 100.0D);
                        adder.accept(Component.translatable("tooltip.agritechevolved.module.yield_boost", yieldBoost));
                        adder.accept(Component.translatable("tooltip.agritechevolved.module.speed_reduction", speedReduction));
                    } else {
                        adder.accept(Component.translatable("tooltip.agritechevolved.module.shift_info"));
                    }
                }
            });

    public static final DeferredItem<Item> YM_MK2 = ITEMS.registerItem("ym_mk2",
            props -> new Item(props) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
                    if (isShiftDown()) {
                        int yieldBoost = (int) Math.round((Config.getYieldModuleMk2Multiplier() - 1.0D) * 100.0D);
                        int speedReduction = (int) Math.round((1.0D - Config.getYieldModuleMk2SpeedPenalty()) * 100.0D);
                        adder.accept(Component.translatable("tooltip.agritechevolved.module.yield_boost", yieldBoost));
                        adder.accept(Component.translatable("tooltip.agritechevolved.module.speed_reduction", speedReduction));
                    } else {
                        adder.accept(Component.translatable("tooltip.agritechevolved.module.shift_info"));
                    }
                }
            });

    public static final DeferredItem<Item> YM_MK3 = ITEMS.registerItem("ym_mk3",
            props -> new Item(props) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
                    if (isShiftDown()) {
                        int yieldBoost = (int) Math.round((Config.getYieldModuleMk3Multiplier() - 1.0D) * 100.0D);
                        int speedReduction = (int) Math.round((1.0D - Config.getYieldModuleMk3SpeedPenalty()) * 100.0D);
                        adder.accept(Component.translatable("tooltip.agritechevolved.module.yield_boost", yieldBoost));
                        adder.accept(Component.translatable("tooltip.agritechevolved.module.speed_reduction", speedReduction));
                    } else {
                        adder.accept(Component.translatable("tooltip.agritechevolved.module.shift_info"));
                    }
                }
            });

    public static final DeferredItem<Item> RM_MK1 = ITEMS.registerItem("rm_mk1",
            props -> new Item(props) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
                    if (isShiftDown()) {
                        adder.accept(Component.translatable("tooltip.agritechevolved.module.rm_mk1"));
                    } else {
                        adder.accept(Component.translatable("tooltip.agritechevolved.module.shift_info"));
                    }
                }
            });

    public static final DeferredItem<Item> RM_MK2 = ITEMS.registerItem("rm_mk2",
            props -> new Item(props) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
                    if (isShiftDown()) {
                        adder.accept(Component.translatable("tooltip.agritechevolved.module.rm_mk2"));
                    } else {
                        adder.accept(Component.translatable("tooltip.agritechevolved.module.shift_info"));
                    }
                }
            });

    public static final DeferredItem<Item> RM_MK3 = ITEMS.registerItem("rm_mk3",
            props -> new Item(props) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
                    if (isShiftDown()) {
                        adder.accept(Component.translatable("tooltip.agritechevolved.module.rm_mk3"));
                    } else {
                        adder.accept(Component.translatable("tooltip.agritechevolved.module.shift_info"));
                    }
                }
            });

    public static final DeferredItem<Item> CRUDE_BIOMASS = ITEMS.registerItem("crude_biomass",
            props -> new Item(props) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
                    adder.accept(Component.translatable("tooltip.agritechevolved.crude_biomass"));
                    NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
                    int baseRF = Config.getBurnerCrudeBiomassRfValue();
                    int burnDuration = Config.getBurnerCrudeBiomassBurnDuration();
                    int baseDuration = 50;
                    int actualRF = (int) ((float) baseRF * ((float) burnDuration / baseDuration));
                    adder.accept(Component.translatable("tooltip.agritechevolved.crude_biomass.rf_generation", fmt.format(actualRF)).withStyle(ChatFormatting.GREEN));
                    if (isShiftDown()) {
                        double burnSeconds = burnDuration / 20.0D;
                        adder.accept(Component.translatable("tooltip.agritechevolved.fuel.burn_duration", String.format("%.1f", burnSeconds)).withStyle(ChatFormatting.AQUA));
                        adder.accept(Component.translatable("tooltip.agritechevolved.fuel.rf_per_second", fmt.format((int) Math.round(actualRF / burnSeconds))).withStyle(ChatFormatting.YELLOW));
                        if (burnDuration != baseDuration) {
                            int pct = (int) Math.round(((double) burnDuration / baseDuration - 1.0D) * 100.0D);
                            if (pct > 0) adder.accept(Component.translatable("tooltip.agritechevolved.fuel.duration_bonus", pct).withStyle(ChatFormatting.GREEN));
                            else if (pct < 0) adder.accept(Component.translatable("tooltip.agritechevolved.fuel.duration_penalty", Math.abs(pct)).withStyle(ChatFormatting.RED));
                        }
                    } else {
                        adder.accept(Component.translatable("tooltip.agritechevolved.crude_fuel.shift_info"));
                    }
                    adder.accept(Component.translatable("tooltip.agritechevolved.crude_biomass.inefficient").withStyle(ChatFormatting.RED));
                }
            });

    public static final DeferredItem<Item> BIOMASS = ITEMS.registerItem("biomass",
            props -> new Item(props) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
                    adder.accept(Component.translatable("tooltip.agritechevolved.biomass"));
                    NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
                    int baseRF = Config.getBurnerBiomassRfValue();
                    int burnDuration = Config.getBurnerBiomassBurnDuration();
                    int baseDuration = 100;
                    int actualRF = (int) ((float) baseRF * ((float) burnDuration / baseDuration));
                    if (isShiftDown()) {
                        adder.accept(Component.translatable("tooltip.agritechevolved.biomass.rf_generation", fmt.format(actualRF)).withStyle(ChatFormatting.GREEN));
                        double burnSeconds = burnDuration / 20.0D;
                        adder.accept(Component.translatable("tooltip.agritechevolved.fuel.burn_duration", String.format("%.1f", burnSeconds)).withStyle(ChatFormatting.AQUA));
                        adder.accept(Component.translatable("tooltip.agritechevolved.fuel.rf_per_second", fmt.format((int) Math.round(actualRF / burnSeconds))).withStyle(ChatFormatting.YELLOW));
                        if (burnDuration != baseDuration) {
                            int pct = (int) Math.round(((double) burnDuration / baseDuration - 1.0D) * 100.0D);
                            if (pct > 0) adder.accept(Component.translatable("tooltip.agritechevolved.fuel.duration_bonus", pct).withStyle(ChatFormatting.GREEN));
                            else if (pct < 0) adder.accept(Component.translatable("tooltip.agritechevolved.fuel.duration_penalty", Math.abs(pct)).withStyle(ChatFormatting.RED));
                        }
                    } else {
                        adder.accept(Component.translatable("tooltip.agritechevolved.fuel.shift_info"));
                    }
                }
            });

    public static final DeferredItem<Item> COMPACTED_BIOMASS = ITEMS.registerItem("compacted_biomass",
            props -> new Item(props) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
                    adder.accept(Component.translatable("tooltip.agritechevolved.compacted"));
                    NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
                    int baseRF = Config.getBurnerCompactedBiomassRfValue();
                    int burnDuration = Config.getBurnerCompactedBiomassBurnDuration();
                    int baseDuration = 180;
                    int actualRF = (int) ((float) baseRF * ((float) burnDuration / baseDuration));
                    if (isShiftDown()) {
                        adder.accept(Component.translatable("tooltip.agritechevolved.compacted_biomass.rf_generation", fmt.format(actualRF)).withStyle(ChatFormatting.GREEN));
                        double burnSeconds = burnDuration / 20.0D;
                        adder.accept(Component.translatable("tooltip.agritechevolved.fuel.burn_duration", String.format("%.1f", burnSeconds)).withStyle(ChatFormatting.AQUA));
                        adder.accept(Component.translatable("tooltip.agritechevolved.fuel.rf_per_second", fmt.format((int) Math.round(actualRF / burnSeconds))).withStyle(ChatFormatting.YELLOW));
                        if (burnDuration != baseDuration) {
                            int pct = (int) Math.round(((double) burnDuration / baseDuration - 1.0D) * 100.0D);
                            if (pct > 0) adder.accept(Component.translatable("tooltip.agritechevolved.fuel.duration_bonus", pct).withStyle(ChatFormatting.GREEN));
                            else if (pct < 0) adder.accept(Component.translatable("tooltip.agritechevolved.fuel.duration_penalty", Math.abs(pct)).withStyle(ChatFormatting.RED));
                        }
                    } else {
                        adder.accept(Component.translatable("tooltip.agritechevolved.fuel.shift_info"));
                    }
                }
            });

    public static final DeferredHolder<Item, ClocheItem> CLOCHE = ITEMS.registerItem("cloche_dome",
            props -> new ClocheItem(props));

    private static boolean isShiftDown() {
        Window window = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}