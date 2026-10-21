package net.diaithir.perf.mixin.compat.jei;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * JEI simulates the anvil for every enchantable item with every enchantment at every level, keeping each result
 * stack: about 950,000 enchanted stacks in this pack. Each item and enchantment pair now gets one recipe at the
 * enchantment's top level. Optional: a JEI update that renames the lambda only turns this off.
 */
@Mixin(targets = "mezz.jei.library.plugins.vanilla.anvil.AnvilRecipeMaker")
abstract class AnvilRecipeMakerMixin {
    @Redirect(method = "lambda$getBookEnchantmentRecipes$2", require = 0,
            at = @At(value = "INVOKE", target = "Lmezz/jei/library/plugins/vanilla/anvil/AnvilRecipeMaker$EnchantmentData;getEnchantedBooks()Ljava/util/List;"))
    private List<ItemStack> diaithirperf$topLevelOnly(@Coerce EnchantmentDataAccessor data) {
        List<ItemStack> books = data.diaithirperf$books();
        return books.isEmpty() ? books : List.of(books.getLast());
    }
}
