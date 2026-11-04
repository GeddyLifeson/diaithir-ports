package net.diaithir.druidry.ars.common.armor;

import net.diaithir.druidry.ars.api.perk.ITickablePerk;
import net.diaithir.druidry.ars.api.perk.PerkInstance;
import net.diaithir.druidry.ars.api.util.PerkUtil;
import net.diaithir.druidry.ars.common.crafting.recipes.IDyeable;
import net.diaithir.druidry.ars.common.items.data.ArmorPerkHolder;
import net.diaithir.druidry.ars.common.perk.RepairingPerk;
import net.diaithir.druidry.ars.setup.registry.DataComponentRegistry;
import net.diaithir.druidry.ars.setup.registry.ItemsRegistry;
import net.diaithir.druidry.ars.setup.registry.MaterialRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Port note: geckolib rendering and the dynamic per-stack perk attribute modifiers were removed.
 * Armor is a plain item using Item.Properties#humanoidArmor.
 */
public class AnimatedMagicArmor extends Item implements IDyeable {
    public final ArmorType type;

    public AnimatedMagicArmor(ArmorMaterial material, ArmorType type, Properties builder) {
        super(builder.humanoidArmor(material, type));
        this.type = type;
    }

    private static Properties base() {
        return ItemsRegistry.defaultItemProperties()
                .stacksTo(1)
                .component(DataComponentRegistry.ARMOR_PERKS, new ArmorPerkHolder())
                .component(DataComponents.BASE_COLOR, DyeColor.PURPLE);
    }

    public static AnimatedMagicArmor light(ArmorType slot) {
        return new AnimatedMagicArmor(MaterialRegistry.LIGHT, slot, base());
    }

    public static AnimatedMagicArmor medium(ArmorType slot) {
        return new AnimatedMagicArmor(MaterialRegistry.MEDIUM, slot, base());
    }

    public static AnimatedMagicArmor heavy(ArmorType slot) {
        return new AnimatedMagicArmor(MaterialRegistry.HEAVY, slot, base());
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull net.minecraft.server.level.ServerLevel world, @NotNull Entity player, net.minecraft.world.entity.EquipmentSlot slot) {
        super.inventoryTick(stack, world, player, slot);
        if (slot != null && slot.isArmor()) {
            if (player instanceof LivingEntity livingEntity) {
                RepairingPerk.attemptRepair(stack, livingEntity);
                var perkHolder = PerkUtil.getPerkHolder(stack);
                if (perkHolder == null)
                    return;
                for (PerkInstance instance : perkHolder.getPerkInstances(stack)) {
                    if (instance.getPerk() instanceof ITickablePerk tickablePerk) {
                        tickablePerk.tick(stack, world, livingEntity, instance);
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext world, @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, world, display, tooltip, flag);
        var data = stack.get(DataComponentRegistry.ARMOR_PERKS);
        if (data != null) {
            tooltip.accept(Component.translatable("ars_nouveau.tier", data.getTier() + 1).withStyle(ChatFormatting.GOLD));
            List<Component> perkTooltips = new ArrayList<>();
            data.appendPerkTooltip(perkTooltips, stack);
            perkTooltips.forEach(tooltip);
        }
    }

    @Deprecated(forRemoval = true) // Use BASE_COLOR data component instead
    public String getColor(ItemStack object) {
        return object.getOrDefault(DataComponents.BASE_COLOR, DyeColor.PURPLE).getName();
    }

    //needed to exclude armors that can't be upgraded via recipe from jei info
    public int getMinTier() {
        return 0;
    }
}
