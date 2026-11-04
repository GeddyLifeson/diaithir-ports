package com.otectus.immersivetanning.recipe;

import com.otectus.immersivetanning.ImmersiveTanning;
import com.otectus.immersivetanning.minigame.SeamPatterns;
import com.otectus.immersivetanning.minigame.StitchPattern;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

/**
 * Picks a seam pattern for a result that does not name one: an item tag {@code <namespace>:seams/<path>} naming the
 * seam {@code <namespace>:<path>} (so modded saddles can take the saddle seam), then equipment slot, or the generic seam.
 */
public final class EquipmentSeams {

    public static Identifier classify(ItemStack stack) {
        for (Identifier seam : SeamPatterns.ids()) {
            if (stack.is(ItemTags.create(Identifier.fromNamespaceAndPath(seam.getNamespace(), "seams/" + seam.getPath())))) return seam;
        }
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null) return StitchPattern.GENERIC;
        EquipmentSlot slot = equippable.slot();
        return switch (slot) {
            case HEAD -> ImmersiveTanning.id("helmet");
            case CHEST -> ImmersiveTanning.id("chestplate");
            case LEGS -> ImmersiveTanning.id("leggings");
            case FEET -> ImmersiveTanning.id("boots");
            case SADDLE -> ImmersiveTanning.id("saddle");
            case BODY -> ImmersiveTanning.id("horse_armor");
            default -> StitchPattern.GENERIC;
        };
    }

    /** Vanilla-style leather armor and dyeable leather gear, which are leatherwork even without a tag. */
    public static boolean isLeatherArmor(ItemStack stack) {
        return stack.has(DataComponents.EQUIPPABLE) && stack.has(DataComponents.DYED_COLOR);
    }

    private EquipmentSeams() {}
}
