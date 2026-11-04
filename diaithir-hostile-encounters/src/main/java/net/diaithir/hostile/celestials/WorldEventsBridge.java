package net.diaithir.hostile.celestials;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * Registers every lunar event with diaithir-worldevents (WorldEvents.register) when that mod is present, so /worldevents start
 * and WorldEventStartedEvent see them. Weight is 0: the lunar forecast stays the only scheduler of moon nights, so the central
 * roll never fires a second one. Built with a dynamic proxy so this module needs no compile dependency on diaithir-worldevents.
 */
public final class WorldEventsBridge {
    private static boolean done;

    @SubscribeEvent
    public static void started(ServerStartedEvent e) {
        if (done || !ModList.get().isLoaded("diaithirworldevents")) return;
        try {
            Class<?> iface = Class.forName("net.diaithir.worldevents.WorldEvent");
            Method register = Class.forName("net.diaithir.worldevents.WorldEvents").getMethod("register", iface);
            for (Lunar.Event ev : Lunar.events().values()) {
                if (!ev.schedulable()) continue;
                String name = ev.id.substring(ev.id.indexOf(':') + 1);
                register.invoke(null, Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[] {iface}, (p, m, a) -> switch (m.getName()) {
                    case "id" -> name;
                    case "title" -> net.minecraft.network.chat.Component.translatable("diaithirhostile.name." + name).getString();
                    case "weight" -> 0;
                    case "possible" -> true;
                    case "trigger" -> {
                        ServerLevel lvl = (ServerLevel) a[0];
                        Forecast.of(lvl).force(lvl, ev.id);
                        yield name + " forced for tonight";
                    }
                    case "hashCode" -> System.identityHashCode(p);
                    case "equals" -> p == a[0];
                    case "toString" -> "lunar:" + name;
                    default -> throw new UnsupportedOperationException(m.getName());
                }));
            }
            done = true;
            Lunar.LOG.info("lunar events registered with diaithir-worldevents");
        } catch (ReflectiveOperationException ex) {
            Lunar.LOG.warn("diaithir-worldevents present but its API did not match: {}", ex.toString());
        }
    }

    private WorldEventsBridge() {}
}
