package com.otectus.immersivetanning.hide;

import com.otectus.immersivetanning.item.RawHideItem;
import com.otectus.immersivetanning.recipe.HideMountingLoader;
import com.otectus.immersivetanning.recipe.HideMountingRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * What the Tanner's Rack accepts as a raw hide: this mod's raw hides, then any item a hide mounting entry names
 * (butchery skins, modded pelts). Mounting entries are datapack files, loaded on both sides; on the client a mounted
 * trait is not resolved, which only matters to the server.
 */
public final class HideItems {

    /** The raw hide a stack mounts as, if the rack accepts it. */
    public static Optional<HideData> resolve(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        if (stack.getItem() instanceof RawHideItem raw) return Optional.of(raw.data(stack));
        return mounting(stack).map(m -> m.hide(stack));
    }

    public static boolean isMountable(ItemStack stack) {
        return stack.getItem() instanceof RawHideItem || mounting(stack).isPresent();
    }

    /** The mounting entry for a foreign hide item; empty for this mod's raw hides and anything else. */
    public static Optional<HideMountingRecipe> mounting(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof RawHideItem) return Optional.empty();
        for (HideMountingRecipe recipe : HideMountingLoader.all()) {
            if (recipe.accepts(stack)) return Optional.of(recipe);
        }
        return Optional.empty();
    }

    private HideItems() {}
}
