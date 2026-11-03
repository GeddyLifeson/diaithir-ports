package com.diaithir.overworld_biomes.space;

import com.diaithir.overworld_biomes.realms.RealmRegistrar;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Clean-room replacement for Stellaris (CC-BY-NC-SA, so it cannot be folded). Keeps the modid and the ids of its blocks,
 * items, dimensions and creatures; the machines are not rebuilt (their ids migrate to vanilla stand-ins, see
 * data/stellaris/id_migrations.json). Own code: space air (SpaceAir), rocket (RocketItem), oxygen distributor.
 */
@Mod(StellarisRealm.MOD_ID)
public final class StellarisRealm {
    public static final String MOD_ID = "stellaris";

    public StellarisRealm(IEventBus bus) {
        RealmRegistrar.BLOCK_OVERRIDES.put(MOD_ID + ":oxygen_distributor", props -> new net.minecraft.world.level.block.Block(props.lightLevel(s -> 5)));
        RealmRegistrar.ITEM_OVERRIDES.put(MOD_ID + ":rocket", props -> new RocketItem(props.stacksTo(1)));
        RealmRegistrar.register(MOD_ID, bus);
        NeoForge.EVENT_BUS.register(new SpaceAir());
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.RegisterCommandsEvent e) -> SpaceSelfTest.register(e.getDispatcher()));
    }
}
