package net.diaithir.steamrails;

import com.zurrtum.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Steam locomotion for our port of Create. Clean-room: written from public descriptions of the ideas only (a solid-fuel boiler with pressure, steam
 * cylinders that give a train its power, a tender, steam bogeys, a whistle, chimney smoke). No code, art, sound or name comes from any other mod.
 */
@Mod(SteamRails.ID)
public final class SteamRails {
    public static final String ID = "diaithirsteamrails";

    /** Water volume per boiler size unit in millibuckets. */
    public static final int WATER_PER_SIZE = 2000;
    /** Pressure ratings in bar. A boiler is only as strong as its weakest block. */
    public static final int CORE_RATING = 12;
    public static final int COPPER_RATING = 7;
    public static final int STEEL_RATING = 16;
    /** Steam made per tick per square root of size by a lit fire, and the gain from a chimney. */
    public static final float FIRE_STEAM = 0.03F;
    public static final float DRAFT_BONUS = 1.25F;
    /** Water boiled away for one unit of steam, in millibuckets. */
    public static final float WATER_PER_STEAM = 10F;
    /** Share of the steam that leaks away each tick. */
    public static final float LEAK = 0.0005F;
    /** Steam one cylinder uses per tick at full speed. */
    public static final float CYLINDER_STEAM = 0.025F;

    public SteamRails(IEventBus bus) {
        SteamBlocks.register(bus);
        bus.addListener(FMLCommonSetupEvent.class, e -> e.enqueueWork(SteamRails::createSetup));
        bus.addListener(RegisterCapabilitiesEvent.class, e -> {
            e.registerBlockEntity(Capabilities.Fluid.BLOCK, SteamBlocks.BOILER_BE.get(), (be, side) -> be.intake);
            e.registerBlockEntity(Capabilities.Fluid.BLOCK, SteamBlocks.TENDER_BE.get(), (be, side) -> be.intake);
        });
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) SteamBlocks.BLOCK_ITEMS.forEach(i -> e.accept(i.get()));
            if (e.getTabKey() == CreativeModeTabs.INGREDIENTS) SteamBlocks.PARTS.forEach(i -> e.accept(i.get()));
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> SteamCommands.register(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, e -> {
            TrainPower.tick();
            SelfTest.tick(e.getServer());
        });
    }

    /** Hooks into Create. Runs on the main thread once every registry is filled. */
    private static void createSetup() {
        SteamBlocks.registerStyles();
        MovementBehaviour.REGISTRY.register(SteamBlocks.BOILER.get(), new BoilerMovement());
        MovingInteractionBehaviour.REGISTRY.register(SteamBlocks.BOILER.get(), new ContraptionHand.Boiler());
        MovingInteractionBehaviour.REGISTRY.register(SteamBlocks.TENDER.get(), new ContraptionHand.Tender());
    }
}
