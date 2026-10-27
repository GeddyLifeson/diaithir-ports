package org.diaithir.diesel.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.diaithir.diesel.CreateDieselGenerators;
import org.diaithir.diesel.content.DieselEngineBlockEntity;

import java.util.Set;

public class CDGBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CreateDieselGenerators.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DieselEngineBlockEntity>> DIESEL_ENGINE =
        TYPES.register("diesel_engine", () -> new BlockEntityType<>(DieselEngineBlockEntity::new, Set.of(CDGBlocks.DIESEL_ENGINE.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<org.diaithir.diesel.content.canister.CanisterBlockEntity>> CANISTER =
        TYPES.register("canister", () -> new BlockEntityType<>(org.diaithir.diesel.content.canister.CanisterBlockEntity::new, Set.of(CDGBlocks.CANISTER.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<org.diaithir.diesel.content.burner.BurnerBlockEntity>> BURNER =
        TYPES.register("burner", () -> new BlockEntityType<>(org.diaithir.diesel.content.burner.BurnerBlockEntity::new, Set.of(CDGBlocks.BURNER.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<org.diaithir.diesel.content.modular.ModularDieselEngineBlockEntity>> MODULAR_DIESEL_ENGINE =
        TYPES.register("modular_diesel_engine", () -> new BlockEntityType<>(org.diaithir.diesel.content.modular.ModularDieselEngineBlockEntity::new, Set.of(CDGBlocks.MODULAR_DIESEL_ENGINE.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<org.diaithir.diesel.content.huge.HugeDieselEngineBlockEntity>> HUGE_DIESEL_ENGINE =
        TYPES.register("huge_diesel_engine", () -> new BlockEntityType<>(org.diaithir.diesel.content.huge.HugeDieselEngineBlockEntity::new, Set.of(CDGBlocks.HUGE_DIESEL_ENGINE.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<org.diaithir.diesel.content.distillation.DistillationTowerBlockEntity>> DISTILLATION_TOWER =
        TYPES.register("distillation_tower", () -> new BlockEntityType<>(org.diaithir.diesel.content.distillation.DistillationTowerBlockEntity::new, Set.of(CDGBlocks.DISTILLATION_TOWER.get())));
}
