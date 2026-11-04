package net.diaithir.worldsys.admincommands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Utility commands for admins and players, including home, spawn, back and teleport requests. */
public final class AdminCommandsFeature {
    private record Spot(ServerLevel level, double x, double y, double z, float yRot, float xRot) {
        static Spot of(ServerPlayer p) {
            return new Spot((ServerLevel) p.level(), p.getX(), p.getY(), p.getZ(), p.getYRot(), p.getXRot());
        }
    }

    private record Request(UUID from, long expires) {}

    private static final long REQUEST_TICKS = 1200L;
    private static final Map<UUID, Spot> HOMES = new HashMap<>();
    private static final Map<UUID, Spot> BACK = new HashMap<>();
    private static final Map<UUID, Request> REQUESTS = new HashMap<>();

    private AdminCommandsFeature() {}

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> commands(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(LivingDeathEvent.class, e -> {
            if (e.getEntity() instanceof ServerPlayer p) BACK.put(p.getUUID(), Spot.of(p));
        });
    }

    private static boolean isAdmin(CommandSourceStack s) {
        return s.getServer() != null && s.getEntity() instanceof ServerPlayer p
                && s.getServer().getPlayerList().isOp(p.nameAndId());
    }

    private static void commands(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("sethome").executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            HOMES.put(p.getUUID(), Spot.of(p));
            return say(c, "Home set.");
        }));
        d.register(Commands.literal("home").executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            Spot s = HOMES.get(p.getUUID());
            if (s == null) return say(c, "You have no home. Use /sethome.");
            go(p, s);
            return 1;
        }));
        d.register(Commands.literal("spawn").executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            ServerLevel w = p.level().getServer().overworld();
            var pos = w.getRespawnData().pos();
            go(p, new Spot(w, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, p.getYRot(), 0F));
            return 1;
        }));
        d.register(Commands.literal("back").executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            Spot s = BACK.get(p.getUUID());
            if (s == null) return say(c, "Nowhere to go back to.");
            go(p, s);
            return 1;
        }));
        d.register(Commands.literal("tpa").then(Commands.argument("player", EntityArgument.player()).executes(c -> {
            ServerPlayer from = c.getSource().getPlayerOrException();
            ServerPlayer to = EntityArgument.getPlayer(c, "player");
            if (from == to) return say(c, "You cannot teleport to yourself.");
            REQUESTS.put(to.getUUID(), new Request(from.getUUID(), from.level().getGameTime() + REQUEST_TICKS));
            to.sendSystemMessage(Component.literal(from.getGameProfile().name() + " wants to teleport to you. /tpaccept or /tpdeny"));
            return say(c, "Request sent.");
        })));
        d.register(Commands.literal("tpaccept").executes(c -> {
            ServerPlayer to = c.getSource().getPlayerOrException();
            ServerPlayer from = pending(to);
            if (from == null) return say(c, "No pending request.");
            REQUESTS.remove(to.getUUID());
            go(from, Spot.of(to));
            return say(c, "Request accepted.");
        }));
        d.register(Commands.literal("tpdeny").executes(c -> {
            ServerPlayer to = c.getSource().getPlayerOrException();
            ServerPlayer from = pending(to);
            if (from == null) return say(c, "No pending request.");
            REQUESTS.remove(to.getUUID());
            from.sendSystemMessage(Component.literal("Your teleport request was denied."));
            return say(c, "Request denied.");
        }));
        d.register(Commands.literal("tphere").requires(s -> isAdmin(s))
                .then(Commands.argument("player", EntityArgument.player()).executes(c -> {
                    ServerPlayer admin = c.getSource().getPlayerOrException();
                    go(EntityArgument.getPlayer(c, "player"), Spot.of(admin));
                    return 1;
                })));
        d.register(admin("heal", p -> {
            p.setHealth(p.getMaxHealth());
            p.removeAllEffects();
            return "Healed.";
        }));
        d.register(admin("feed", p -> {
            p.getFoodData().setFoodLevel(20);
            p.getFoodData().setSaturation(20F);
            return "Fed.";
        }));
        d.register(admin("fly", p -> {
            boolean on = !p.getAbilities().mayfly;
            p.getAbilities().mayfly = on;
            if (!on) p.getAbilities().flying = false;
            p.onUpdateAbilities();
            return on ? "Flight enabled." : "Flight disabled.";
        }));
        d.register(admin("vanishlite", p -> {
            if (p.hasEffect(MobEffects.INVISIBILITY)) {
                p.removeEffect(MobEffects.INVISIBILITY);
                return "Visible again.";
            }
            p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, MobEffectInstance.INFINITE_DURATION, 0, false, false));
            return "Invisible.";
        }));
    }

    private interface Action {
        String apply(ServerPlayer p);
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> admin(String name, Action a) {
        return Commands.literal(name).requires(s -> isAdmin(s))
                .executes(c -> say(c, a.apply(c.getSource().getPlayerOrException())))
                .then(Commands.argument("player", EntityArgument.player()).executes(c -> say(c, a.apply(EntityArgument.getPlayer(c, "player")))));
    }

    private static ServerPlayer pending(ServerPlayer to) {
        Request r = REQUESTS.get(to.getUUID());
        if (r == null || r.expires() < to.level().getGameTime()) {
            REQUESTS.remove(to.getUUID());
            return null;
        }
        return to.level().getServer().getPlayerList().getPlayer(r.from());
    }

    private static void go(ServerPlayer p, Spot s) {
        BACK.put(p.getUUID(), Spot.of(p));
        p.teleportTo(s.level(), s.x(), s.y(), s.z(), Set.of(), s.yRot(), s.xRot(), true);
    }

    private static int say(CommandContext<CommandSourceStack> c, String msg) {
        c.getSource().sendSuccess(() -> Component.literal(msg), false);
        return 1;
    }
}
