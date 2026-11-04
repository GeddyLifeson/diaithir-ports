package org.diaithir.connected;

import com.mojang.logging.LogUtils;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.stress.BlockStressValues;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.diaithir.connected.registries.CCBlockEntityTypes;
import org.diaithir.connected.registries.CCBlocks;
import org.diaithir.connected.registries.CCItems;
import org.slf4j.Logger;

import java.util.function.DoubleSupplier;

/** Diaithir port of Create: Connected (hlysine, AGPL-3.0-or-later) to NeoForge 26.1.2 on the Diaithir Create port. */
@Mod(CreateConnected.ID)
public class CreateConnected {
    /** Mod id as loaded. */
    public static final String ID = "diaithir_create_connected";
    /** Registry namespace, kept from upstream so ids and recipes stay recognisable. */
    public static final String MODID = "create_connected";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.create_connected.main"))
        .icon(() -> new ItemStack(CCItems.BRASS_GEARBOX.get()))
        .displayItems((params, out) -> {
            for (var item : CCItems.all()) out.accept(item);
        }).build());

    public static Identifier asResource(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public CreateConnected(IEventBus bus, ModContainer container) {
        CCBlocks.BLOCKS.register(bus);
        CCItems.ITEMS.register(bus);
        CCBlockEntityTypes.TYPES.register(bus);
        TABS.register(bus);
        bus.addListener(FMLCommonSetupEvent.class, e -> e.enqueueWork(CreateConnected::setup));
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            org.diaithir.connected.client.CreateConnectedClient.init(bus);
        }
    }

    private static void setup() {
        com.zurrtum.create.content.decoration.encasing.EncasingRegistry.addVariant(CCBlocks.CROSS_CONNECTOR.get(), CCBlocks.ANDESITE_ENCASED_CROSS_CONNECTOR.get());
        com.zurrtum.create.content.decoration.encasing.EncasingRegistry.addVariant(CCBlocks.CROSS_CONNECTOR.get(), CCBlocks.BRASS_ENCASED_CROSS_CONNECTOR.get());
        for (var b : new net.minecraft.world.level.block.Block[]{CCBlocks.PARALLEL_GEARBOX.get(), CCBlocks.SIX_WAY_GEARBOX.get(),
            CCBlocks.SHEAR_PIN.get(), CCBlocks.BRASS_GEARBOX.get(), CCBlocks.INVERTED_CLUTCH.get(), CCBlocks.INVERTED_GEARSHIFT.get(), CCBlocks.BRAKE.get(), CCBlocks.OVERSTRESS_CLUTCH.get(), CCBlocks.FREEWHEEL_CLUTCH.get(), CCBlocks.CENTRIFUGAL_CLUTCH.get(), CCBlocks.CROSS_CONNECTOR.get(), CCBlocks.ANDESITE_ENCASED_CROSS_CONNECTOR.get(), CCBlocks.BRASS_ENCASED_CROSS_CONNECTOR.get()}) {
            BlockStressValues.IMPACTS.register(b, (DoubleSupplier) () -> 0.0);
        }
    }
}
