package net.diaithir.linkedstorage;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class Menus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, DiaithirLinkedStorage.ID);
    public static final DeferredHolder<MenuType<?>, MenuType<LinkedMenu>> LINKED = MENUS.register("linked",
            () -> new MenuType<>((id, inv) -> new LinkedMenu(id, inv, new ItemStackHandler(LinkedData.CHEST_SLOTS)), FeatureFlags.VANILLA_SET));

    private Menus() {}
}
