package com.misterd.agritechevolved.gui;

import com.misterd.agritechevolved.gui.custom.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ATEMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, "agritechevolved");

    public static final DeferredHolder<MenuType<?>, MenuType<BasicPlanterMenu>> PLANTER_BLOCK_MENU = registerMenuType("planter_block_menu", BasicPlanterMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedPlanterMenu>> ADVANCED_PLANTER_BLOCK_MENU = registerMenuType("advanced_planter_block_menu", AdvancedPlanterMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<ComposterMenu>> COMPOSTER_MENU = registerMenuType("composter_menu", ComposterMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<CapacitorMenu>> CAPACITOR_MENU = registerMenuType("capacitor_menu", CapacitorMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<BiomassBurnerMenu>> BURNER_MENU = registerMenuType("burner_menu", BiomassBurnerMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<SiloMenu>> SILO_MENU = registerMenuType("silo_menu", SiloMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<FertilizerSpreaderMenu>> FERTILIZER_SPREADER_MENU = registerMenuType("fertilizer_spreader_menu", FertilizerSpreaderMenu::new);

    private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
