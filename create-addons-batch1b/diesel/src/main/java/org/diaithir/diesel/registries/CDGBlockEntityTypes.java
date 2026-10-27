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
}
