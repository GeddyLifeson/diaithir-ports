package com.diaithir.addons.newage;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.diaithir.addons.newage.config.CNAConfig;
import com.diaithir.addons.newage.content.electricity.network.NetworkTicker;
import com.diaithir.addons.newage.esl.EnergyBridge;
import com.diaithir.addons.newage.esl.EnergyStorage;

/** Create: New Age ported onto the Diaithir Create port. Registry namespace stays create_new_age. */
@Mod("diaithir_create_new_age")
public class CreateNewAge {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "create_new_age";

    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + MOD_ID))
            .icon(() -> new ItemStack(CNABlocks.BASIC_MOTOR.get()))
            .displayItems((params, out) -> {
                for (var s : Reg.TAB) {
                    ItemLike i = s.get();
                    out.accept(i);
                }
            }).build());

    private static CreateNewAge instance;

    public CreateNewAge(IEventBus bus, ModContainer container) {
        instance = this;
        CNABlocks.load();
        CNAItems.load();
        CNABlockEntityTypes.load();
        CNATags.load();
        Reg.register(bus);
        CNAEffects.EFFECTS.register(bus);
        CNASounds.SOUNDS.register(bus);
        CNARecipeTypes.register(bus);
        CNADataComponents.DATA_COMPONENTS.register(bus);
        TABS.register(bus);
        CNAConfig.register(container);

        bus.addListener(this::commonSetup);
        bus.addListener(this::registerCapabilities);
        NeoForge.EVENT_BUS.addListener((LevelTickEvent.Pre e) -> NetworkTicker.tickWorld(e.getLevel()));
    }

    private void commonSetup(FMLCommonSetupEvent e) {
        e.enqueueWork(() -> {
            for (Runnable r : Reg.POST) r.run();
            CNAIntegration.commonSetup();
        });
    }

    /** Every energy block entity of the mod is exposed to the NeoForge FE capability (Mekanism, Immersive Engineering, ...). */
    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (var type : java.util.List.of(
                CNABlockEntityTypes.BASIC_MOTOR, CNABlockEntityTypes.ADVANCED_MOTOR, CNABlockEntityTypes.REINFORCED_MOTOR,
                CNABlockEntityTypes.ELECTRICAL_CONNECTOR, CNABlockEntityTypes.CARBON_BRUSHES, CNABlockEntityTypes.ENERGISER,
                CNABlockEntityTypes.STREET_LIGHT)) {
            event.registerBlockEntity(Capabilities.Energy.BLOCK, type.get(),
                    (be, side) -> EnergyBridge.asHandler(EnergyStorage.provided(be, side)));
        }
    }

    public static CreateNewAge getInstance() {
        return instance;
    }
}
