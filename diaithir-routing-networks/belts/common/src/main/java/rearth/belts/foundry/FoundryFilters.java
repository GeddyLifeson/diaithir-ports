package rearth.belts.foundry;

import dev.architectury.platform.Platform;
import dev.ftb.mods.ftbfiltersystem.api.FTBFilterSystemAPI;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class FoundryFilters {
    
    private FoundryFilters() {
    }
    
    public static boolean matches(ItemStack filter, ItemStack stack, Level level) {
        if (filter.isEmpty()) return true;
        
        if (Platform.isModLoaded("ftbfiltersystem")) {
            var filterAPI = FTBFilterSystemAPI.api();
            if (filterAPI.isFilterItem(filter))
                return filterAPI.doesFilterMatch(filter, stack, level.registryAccess());
        }
        
        return stack.getItem().equals(filter.getItem());
    }
}
