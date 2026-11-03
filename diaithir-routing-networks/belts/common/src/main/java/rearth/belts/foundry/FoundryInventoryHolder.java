package rearth.belts.foundry;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import rearth.belts.api.item.ItemApi;

public interface FoundryInventoryHolder {
    
    @Nullable ItemApi.InventoryStorage getInventory(@Nullable Direction side);
}
