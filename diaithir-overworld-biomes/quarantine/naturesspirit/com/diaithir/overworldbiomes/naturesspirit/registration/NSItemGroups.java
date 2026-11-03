package com.diaithir.overworldbiomes.naturesspirit.registration;

import com.diaithir.overworldbiomes.naturesspirit.NatureSpirit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.diaithir.overworldbiomes.naturesspirit.NatureSpirit.MOD_ID;

public class NSItemGroups {

  public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

  public static final ResourceKey<CreativeModeTab> NS_ITEM_GROUP = register("tab");

  public static final DeferredHolder<CreativeModeTab, CreativeModeTab> NS_TAB = CREATIVE_TABS.register(
      "tab",
      () -> CreativeModeTab.builder()
          .title(Component.translatable("itemGroup." + MOD_ID + ".item_group"))
          .icon(() -> new ItemStack(NSWoods.REDWOOD.getSapling()))
          .build()
  );

  public static void init(IEventBus modBus) {
    CREATIVE_TABS.register(modBus);
  }

  public static void registerItemGroup() {
    // Registration is handled by the DeferredRegister; call init(IEventBus) from the mod constructor.
  }

  private static ResourceKey<CreativeModeTab> register(String id) {
    return ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MOD_ID, id));
  }

}
