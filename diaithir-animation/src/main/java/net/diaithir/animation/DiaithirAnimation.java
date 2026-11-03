package net.diaithir.animation;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

/** Diaithir Animation: melee, tool, bow, gun and casting clips for the player on Player Animation Library. */
@Mod(DiaithirAnimation.ID)
public final class DiaithirAnimation {
    public static final String ID = "diaithiranimation";

    public DiaithirAnimation(IEventBus bus) {
        bus.addListener(Net::register);
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new BodyServer());
        if (FMLEnvironment.getDist().isClient()) { AnimClient.init(bus); BodyClient.init(bus); GunFx.init(); ProjectileFx.init(); }
    }

    @SubscribeEvent public void tags(TagsUpdatedEvent e) { Kind.invalidate(); BodyKind.invalidate(); }
    @SubscribeEvent public void commands(RegisterCommandsEvent e) { SelfTest.register(e.getDispatcher()); }
}
