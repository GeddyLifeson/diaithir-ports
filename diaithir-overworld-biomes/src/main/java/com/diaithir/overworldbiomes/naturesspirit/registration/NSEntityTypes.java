package com.diaithir.overworldbiomes.naturesspirit.registration;

import com.diaithir.overworldbiomes.naturesspirit.entity.CheeseArrowEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NSEntityTypes {

  public static final String MOD_ID = "natures_spirit";

  public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MOD_ID);

  public static final DeferredHolder<EntityType<?>, EntityType<CheeseArrowEntity>> CHEESE_ARROW = registerEntityType(
      "cheese_arrow",
      () -> {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "cheese_arrow"));
        return EntityType.Builder.<CheeseArrowEntity>of(CheeseArrowEntity::new, MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .clientTrackingRange(4)
            .updateInterval(20)
            .build(key);
      }
  );

  public static void init(IEventBus modBus) {
    ENTITY_TYPES.register(modBus);
  }

  public static void registerEntityTypes() {
  }

  public static <T extends EntityType<?>> DeferredHolder<EntityType<?>, T> registerEntityType(String id, Supplier<T> type) {
    return ENTITY_TYPES.register(id, type);
  }

}
