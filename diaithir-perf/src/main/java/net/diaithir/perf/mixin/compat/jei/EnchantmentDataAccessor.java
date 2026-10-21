package net.diaithir.perf.mixin.compat.jei;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "mezz.jei.library.plugins.vanilla.anvil.AnvilRecipeMaker$EnchantmentData")
public interface EnchantmentDataAccessor {
    @Invoker("getEnchantedBooks")
    List<ItemStack> diaithirperf$books();
}
