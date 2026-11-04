package com.diaithir.overworldbiomes.naturesspirit.registration;

import com.diaithir.overworldbiomes.naturesspirit.NatureSpirit;
import com.diaithir.overworldbiomes.naturesspirit.advancements.CoconutHitCriterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NSCriteria {

  public static final DeferredRegister<CriterionTrigger<?>> CRITERIA =
      DeferredRegister.create(BuiltInRegistries.TRIGGER_TYPES, NatureSpirit.MOD_ID);

  public static final DeferredHolder<CriterionTrigger<?>, CoconutHitCriterion> COCONUT_HIT_CRITERION =
      CRITERIA.register("coconut_hit", CoconutHitCriterion::new);

  public static void init(IEventBus modBus) {
    CRITERIA.register(modBus);
  }

  public static void registerCriteria() {
  }

  public static <T extends CriterionTrigger<?>> DeferredHolder<CriterionTrigger<?>, T> register(String id, java.util.function.Supplier<T> criterion) {
    return CRITERIA.register(id, criterion);
  }
}
