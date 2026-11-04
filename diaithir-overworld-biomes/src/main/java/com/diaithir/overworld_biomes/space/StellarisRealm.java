package com.diaithir.overworld_biomes.space;

import com.diaithir.overworld_biomes.realms.RealmRegistrar;
import net.neoforged.bus.api.IEventBus;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Clean-room replacement for Stellaris (CC-BY-NC-SA, so it cannot be folded). Keeps the modid and the ids of its blocks,
 * items, dimensions and creatures; the machines are not rebuilt (their ids migrate to vanilla stand-ins, see
 * data/stellaris/id_migrations.json). Own code: space air (SpaceAir), rocket (RocketItem), oxygen distributor.
 */
@Mod(StellarisRealm.MOD_ID)
public final class StellarisRealm {
    public static final String MOD_ID = "stellaris";

    /** Names the log an SD card holds; the chest loot tables set it. */
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MOD_ID);
    static {
        COMPONENTS.register("sd_card_name", () -> DataComponentType.<Identifier>builder().persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC).build());
    }

    public StellarisRealm(IEventBus bus) {
        COMPONENTS.register(bus);
        OilFluid.register(bus);
        if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient()) OilClient.init(bus);
        RealmRegistrar.ITEM_OVERRIDES.put(MOD_ID + ":oil_bucket", OilFluid::bucket);
        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, e -> PlanetsLoader.load(e.getServer()));
        // before the pack's Age gate script (also highest, registered later): a reload (no single player) re-reads the planet files
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, (OnDatapackSyncEvent e) -> {
            if (e.getPlayer() == null) PlanetsLoader.load(e.getPlayerList().getServer());
        });
        RealmRegistrar.BLOCK_OVERRIDES.put(MOD_ID + ":oxygen_distributor", props -> new net.minecraft.world.level.block.Block(props.lightLevel(s -> 5)));
        RealmRegistrar.ITEM_OVERRIDES.put(MOD_ID + ":rocket", props -> new RocketItem(props.stacksTo(1)));
        RealmRegistrar.register(MOD_ID, bus);
        NeoForge.EVENT_BUS.register(new SpaceAir());
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.RegisterCommandsEvent e) -> SpaceSelfTest.register(e.getDispatcher()));
    }
}
