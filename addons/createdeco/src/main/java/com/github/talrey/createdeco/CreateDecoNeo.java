package com.github.talrey.createdeco;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod("diaithir_createdeco")
public class CreateDecoNeo {
  public CreateDecoNeo(IEventBus modBus) {
    Content.register();
    tab("props", Reg.TAB_ITEMS, Content.tabIcon());
    tab("bricks", Reg.BRICK_ITEMS, Content.brickIcon());
  }

  private static void tab(String name, java.util.List<net.minecraft.world.item.Item> items, ItemStack icon) {
    ResourceKey<CreativeModeTab> key = ResourceKey.create(Registries.CREATIVE_MODE_TAB, CreateDecoMod.id(name));
    Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key,
        CreativeModeTab.builder(null, -1).title(Component.translatable("itemGroup.createdeco." + name))
            .icon(() -> icon).displayItems((p, o) -> items.forEach(o::accept)).build());
  }
}
