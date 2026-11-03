package net.diaithir.bosses;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Ties the bosses to Global Events (diaithir-worldevents, WorldEvents.register / WorldEventStartedEvent) without a hard dependency:
 * the classes are found by name when that mod is present. Blood moon halves altar rest times, a storm sharpens the sea and ash
 * bosses, and an Ascendant eclipse event (registered here) strengthens the Dusk Wyrm. Our own events go out on the NeoForge bus.
 */
public final class EventHooks {
    public static final long BLOOD_MOON_TICKS = 12000, ECLIPSE_TICKS = 12000;
    private static long bloodMoonUntil = -1, eclipseUntil = -1;

    /** Posted when a boss dies. */
    public static final class BossDefeatedEvent extends Event {
        public final String boss;
        public final int age;
        public BossDefeatedEvent(String boss, int age) { this.boss = boss; this.age = age; }
    }

    /** Posted when the boss rush is cleared. */
    public static final class RushClearedEvent extends Event {
        public final long seconds;
        public final String mode;
        public RushClearedEvent(long seconds, String mode) { this.seconds = seconds; this.mode = mode; }
    }

    public static double respawnMul(ServerLevel level) { return level.getGameTime() < bloodMoonUntil ? 0.5 : 1.0; }

    public static double hpMul(Data.Profile p, ServerLevel level) {
        return p.id.equals("dusk_wyrm") && level.getGameTime() < eclipseUntil ? 1.1 : 1.0;
    }

    public static double damageMul(Data.Profile p, ServerLevel level) {
        return (p.id.equals("tide_reaver") || p.id.equals("ash_seraph")) && level.isThundering() ? 1.1 : 1.0;
    }

    /** Registers the eclipse event and listens for blood moons; does nothing when diaithir-worldevents is absent. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void register() {
        if (!ModList.get().isLoaded("diaithirworldevents")) return;
        try {
            Class<?> worldEvent = Class.forName("net.diaithir.worldevents.WorldEvent");
            Class<?> worldEvents = Class.forName("net.diaithir.worldevents.WorldEvents");
            Class<?> started = Class.forName("net.diaithir.worldevents.WorldEventStartedEvent");
            Object eclipse = Proxy.newProxyInstance(worldEvent.getClassLoader(), new Class<?>[] {worldEvent}, (proxy, m, args) -> switch (m.getName()) {
                case "id" -> "ascendant_eclipse";
                case "title" -> "Eclipse";
                case "weight" -> 2;
                case "possible" -> Ages.current(((ServerLevel) args[0]).getServer()) >= 7;
                case "trigger" -> {
                    ServerLevel l = (ServerLevel) args[0];
                    eclipseUntil = l.getGameTime() + ECLIPSE_TICKS;
                    worldEvents.getMethod("announce", net.minecraft.server.MinecraftServer.class, String.class, ChatFormatting.class)
                        .invoke(null, l.getServer(), "The sun goes dark. Something old stirs in the dusk.", ChatFormatting.DARK_PURPLE);
                    yield "Eclipse until " + eclipseUntil;
                }
                case "toString" -> "ascendant_eclipse";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> null;
            });
            worldEvents.getMethod("register", worldEvent).invoke(null, eclipse);
            Method event = started.getMethod("event"), level = started.getMethod("level"), id = worldEvent.getMethod("id");
            NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, (Class) started, (java.util.function.Consumer) (Object e) -> {
                try {
                    if ("blood_moon".equals(id.invoke(event.invoke(e)))) bloodMoonUntil = ((ServerLevel) level.invoke(e)).getGameTime() + BLOOD_MOON_TICKS;
                } catch (ReflectiveOperationException ex) {
                    // the other mod changed shape; the bosses carry on without the hook
                }
            });
        } catch (ReflectiveOperationException | RuntimeException e) {
            DiaithirBosses.LOG.warn("Global Events hooks not installed: {}", e.toString());
        }
    }

    public static List<String> activeNames(ServerLevel l) {
        List<String> out = new java.util.ArrayList<>();
        if (l.getGameTime() < bloodMoonUntil) out.add("blood_moon");
        if (l.getGameTime() < eclipseUntil) out.add("ascendant_eclipse");
        if (l.isThundering()) out.add("storm");
        return out;
    }

    private EventHooks() {}
}
