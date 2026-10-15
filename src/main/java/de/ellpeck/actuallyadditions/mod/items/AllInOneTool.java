package de.ellpeck.actuallyadditions.mod.items;

import net.minecraft.world.item.ItemInstance;

import net.minecraft.world.item.ToolMaterial;

import net.minecraft.world.item.Item;

import de.ellpeck.actuallyadditions.registration.RegistryIds;

import de.ellpeck.actuallyadditions.api.ActuallyTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import javax.annotation.Nonnull;
import java.util.List;

public class AllInOneTool extends Item {

    private static final List<ItemAbility> ACTIONS = List.of(
        ItemAbilities.HOE_TILL,
        ItemAbilities.SHOVEL_FLATTEN,
        ItemAbilities.AXE_STRIP
    );

    public AllInOneTool(ToolMaterial material) {
        // tool() sets the material's durability; the AIOT lasts four tools' worth
        super(RegistryIds.withId(new Properties())
            .tool(material, ActuallyTags.Blocks.MINEABLE_WITH_AIO, 4.0F, -2.0F, 0.0F)
            .durability(material.durability() * 4));
    }

    @Override
    public boolean canPerformAction(@Nonnull ItemInstance stack, @Nonnull ItemAbility toolAction) {
        if (ACTIONS.contains(toolAction))
            return true;
        return super.canPerformAction(stack, toolAction);
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        // How, no idea, possible, most likely :cry:
        if (context.getPlayer() == null) {
            return InteractionResult.FAIL;
        }

        // Player not sneaking? Act as a Hoe to the block, else, Act as a shovel
        if (context.getPlayer().isCrouching()) {
            return Items.IRON_SHOVEL.useOn(context);
        }
        InteractionResult tmp = Items.IRON_AXE.useOn(context);
        if (tmp == InteractionResult.SUCCESS)
            return tmp;
        return Items.IRON_HOE.useOn(context);
    }

/*    @Override //TODO help, enchantments are weird now.
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment) || enchantment.category.canEnchant(Items.DIAMOND_SWORD);
    }*/
}
