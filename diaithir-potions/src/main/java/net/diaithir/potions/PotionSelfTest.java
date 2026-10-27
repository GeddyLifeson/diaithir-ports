package net.diaithir.potions;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** /potions selftest: checks the reforms are live and prints PASS or what failed. */
final class PotionSelfTest {
    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("potions").requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
            .then(Commands.literal("selftest").executes(c -> {
                String r = run(c.getSource());
                c.getSource().sendSuccess(() -> Component.literal(r), false);
                return 1;
            })));
    }

    private static String run(CommandSourceStack src) {
        ItemStack potion = new ItemStack(Items.POTION);
        if (potion.getMaxStackSize() != DiaithirPotions.POTION_STACK) return "FAIL potion stack " + potion.getMaxStackSize();
        var consumable = potion.get(DataComponents.CONSUMABLE);
        if (consumable == null || consumable.consumeSeconds() != DiaithirPotions.DRINK_SECONDS) return "FAIL drink time " + consumable;
        if (potion.get(DataComponents.USE_COOLDOWN) == null) return "FAIL no cooldown";
        if (new ItemStack(Items.RABBIT_STEW).getMaxStackSize() != DiaithirPotions.STEW_STACK) return "FAIL stew stack";
        if (!new ItemStack(Items.COAL).is(ItemTags.BREWING_FUEL) || !new ItemStack(Items.BLAZE_POWDER).is(ItemTags.BREWING_FUEL)) return "FAIL fuel tag";
        var brewing = src.getServer().potionBrewing();
        if (!brewing.hasMix(new ItemStack(Items.SPLASH_POTION), new ItemStack(Items.GLOW_INK_SAC))) return "FAIL lingering recipe";
        if (!brewing.hasMix(new ItemStack(Items.SPLASH_POTION), new ItemStack(Items.DRAGON_BREATH))) return "FAIL vanilla lingering lost";
        return "PASS potions stack 8, drink 1s, cooldown, stews 16, coal fuel, glow ink lingering";
    }
}
