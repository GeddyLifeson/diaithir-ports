package com.diaithir.overworldbiomes.naturesspirit.registration;

import com.diaithir.overworldbiomes.naturesspirit.NatureSpirit;
import com.diaithir.overworldbiomes.naturesspirit.blocks.block_entities.PizzaToppingVariant;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class NSDataComponents {

  public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
      DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, NatureSpirit.MOD_ID);

  public static final Supplier<DataComponentType<List<PizzaToppingVariant>>> TOPPINGS_HOLDER = register("toppings", (builder) -> {
    return builder.persistent(PizzaToppingVariant.CODEC.listOf()).cacheEncoding();
  });

  /**
   * Convenience accessor kept for code referencing the component type directly.
   * Only valid after registries have been populated.
   */
  public static DataComponentType<List<PizzaToppingVariant>> toppings() {
    return TOPPINGS_HOLDER.get();
  }

  private static <T> Supplier<DataComponentType<T>> register(String id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
    return DATA_COMPONENTS.register(id, () -> builderOperator.apply(DataComponentType.<T>builder()).build());
  }

  public static void init(IEventBus modBus) {
    DATA_COMPONENTS.register(modBus);
  }

  public static void registerDataComponents() {
  }

}
