package org.diaithir.diesel;

import com.mojang.logging.LogUtils;
import com.zurrtum.create.api.stress.BlockStressValues;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.CachedFluidInventoryBehaviour;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.diaithir.diesel.content.DieselEngineBlockEntity;
import org.diaithir.diesel.registries.CDGBlockEntityTypes;
import org.diaithir.diesel.registries.CDGBlocks;
import org.diaithir.diesel.registries.CDGFluids;
import org.diaithir.diesel.registries.CDGItems;
import org.slf4j.Logger;

import java.util.function.DoubleSupplier;

/** Diaithir port of Create: Diesel Generators (George VI, MIT) to NeoForge 26.1.2 on the Diaithir Create port. */
@Mod(CreateDieselGenerators.ID)
public class CreateDieselGenerators {
    public static final String ID = "diaithir_create_diesel_generators";
    public static final String MODID = "createdieselgenerators";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.createdieselgenerators.main"))
        .icon(() -> new ItemStack(CDGBlocks.DIESEL_ENGINE.get()))
        .displayItems((params, out) -> {
            for (var item : CDGItems.all()) out.accept(item);
            for (var e : CDGFluids.ENTRIES.values()) if (e.bucket != null) out.accept(e.bucket);
        }).build());

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public CreateDieselGenerators(IEventBus bus, ModContainer container) {
        CDGBlocks.BLOCKS.register(bus);
        CDGItems.ITEMS.register(bus);
        CDGBlockEntityTypes.TYPES.register(bus);
        org.diaithir.diesel.registries.CDGComponents.COMPONENTS.register(bus);
        TABS.register(bus);
        bus.addListener(RegisterEvent.class, e -> {
            if (e.getRegistryKey().equals(NeoForgeRegistries.Keys.FLUID_TYPES)) CDGFluids.registerTypes();
            else if (e.getRegistryKey().equals(Registries.FLUID)) CDGFluids.registerFluids();
            else if (e.getRegistryKey().equals(Registries.BLOCK)) CDGFluids.registerBlocks();
            else if (e.getRegistryKey().equals(Registries.ITEM)) CDGFluids.registerItems();
        });
        bus.addListener(FMLCommonSetupEvent.class, e -> e.enqueueWork(CreateDieselGenerators::setup));
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            org.diaithir.diesel.client.CreateDieselGeneratorsClient.init(bus);
        }
    }

    private static void setup() {
        BlockEntityBehaviour.add(CDGBlockEntityTypes.DIESEL_ENGINE.get(),
            (DieselEngineBlockEntity be) -> new CachedFluidInventoryBehaviour<>(be, b -> b.tank.getPrimaryHandler()));
        BlockEntityBehaviour.add(CDGBlockEntityTypes.CANISTER.get(),
            (org.diaithir.diesel.content.canister.CanisterBlockEntity be) -> new CachedFluidInventoryBehaviour<>(be, b -> b.tank.getPrimaryHandler()));
        BlockEntityBehaviour.add(CDGBlockEntityTypes.BURNER.get(),
            (org.diaithir.diesel.content.burner.BurnerBlockEntity be) -> new CachedFluidInventoryBehaviour<>(be, b -> b.tank.getPrimaryHandler()));
        BlockStressValues.IMPACTS.register(CDGBlocks.BURNER.get(), (DoubleSupplier) () -> 1.0);
        BlockStressValues.IMPACTS.register(CDGBlocks.MODULAR_DIESEL_ENGINE.get(), (DoubleSupplier) () -> 0.0);
        BlockStressValues.RPM.register(CDGBlocks.MODULAR_DIESEL_ENGINE.get(), new BlockStressValues.GeneratedRpm(96, true));
        BlockEntityBehaviour.add(CDGBlockEntityTypes.HUGE_DIESEL_ENGINE.get(),
            (org.diaithir.diesel.content.huge.HugeDieselEngineBlockEntity be) -> new CachedFluidInventoryBehaviour<>(be, b -> b.tank.getPrimaryHandler()));
        BlockStressValues.IMPACTS.register(CDGBlocks.HUGE_DIESEL_ENGINE.get(), (DoubleSupplier) () -> 0.0);
        BlockStressValues.RPM.register(CDGBlocks.HUGE_DIESEL_ENGINE.get(), new BlockStressValues.GeneratedRpm(224, true));
        BlockEntityBehaviour.add(CDGBlockEntityTypes.DISTILLATION_TOWER.get(),
            (org.diaithir.diesel.content.distillation.DistillationTowerBlockEntity be) -> new CachedFluidInventoryBehaviour<>(be, b -> b.tank.getPrimaryHandler()));
        BlockStressValues.IMPACTS.register(CDGBlocks.DIESEL_ENGINE.get(), (DoubleSupplier) () -> 0.0);
        BlockStressValues.RPM.register(CDGBlocks.DIESEL_ENGINE.get(), new BlockStressValues.GeneratedRpm(96, true));
    }
}
