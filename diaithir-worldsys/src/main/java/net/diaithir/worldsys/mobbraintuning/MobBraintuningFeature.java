package net.diaithir.worldsys.mobbraintuning;

import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/** Freezes far-away mobs to save AI time and lets ops disable chosen vanilla AI behaviours. */
public final class MobBraintuningFeature {
    private static final String TAG = "diaithirworldsys_frozen";
    private static final double FREEZE_SQ = 48.0 * 48.0;
    private static final double THAW_SQ = 40.0 * 40.0;
    private static final Set<String> DISABLED = ConcurrentHashMap.newKeySet();
    private static final Map<String, Predicate<Goal>> BEHAVIOURS = Map.of(
            "panic", g -> g instanceof PanicGoal,
            "wander", g -> g instanceof RandomStrollGoal,
            "look_around", g -> g instanceof RandomLookAroundGoal || g instanceof LookAtPlayerGoal,
            "avoid_players", g -> g instanceof AvoidEntityGoal<?>,
            "mob_attacks", g -> g instanceof NearestAttackableTargetGoal<?>);

    private MobBraintuningFeature() {}

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(EntityTickEvent.Pre.class, MobBraintuningFeature::tick);
        NeoForge.EVENT_BUS.addListener(EntityJoinLevelEvent.class, MobBraintuningFeature::join);
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, MobBraintuningFeature::commands);
    }

    private static void tick(EntityTickEvent.Pre e) {
        if (!(e.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)) return;
        if ((mob.tickCount + mob.getId()) % 20 != 0) return;
        boolean frozen = mob.entityTags().contains(TAG);
        if (!frozen && !eligible(mob)) return;
        ServerPlayer nearest = null;
        double best = Double.MAX_VALUE;
        for (ServerPlayer p : level.players()) {
            double d = p.distanceToSqr(mob);
            if (d < best) {
                best = d;
                nearest = p;
            }
        }
        if (frozen && (nearest != null && best < THAW_SQ)) {
            mob.setNoAi(false);
            mob.removeTag(TAG);
        } else if (!frozen && (nearest == null || best > FREEZE_SQ) && !mob.isNoAi()) {
            mob.setNoAi(true);
            mob.addTag(TAG);
        }
    }

    private static boolean eligible(Mob mob) {
        EntityType<?> type = mob.getType();
        boolean boss = type == EntityType.WITHER
                || type == EntityType.ENDER_DRAGON
                || type == EntityType.WARDEN
                || type == EntityType.ELDER_GUARDIAN;
        return !(mob instanceof TamableAnimal t && t.isTame())
                && !boss
                && !mob.isPersistenceRequired()
                && !mob.hasCustomName()
                && !mob.isPassenger()
                && !mob.isVehicle();
    }

    private static void join(EntityJoinLevelEvent e) {
        if (e.getLevel().isClientSide() || DISABLED.isEmpty() || !(e.getEntity() instanceof Mob mob)) return;
        for (String name : DISABLED) {
            Predicate<Goal> match = BEHAVIOURS.get(name);
            mob.goalSelector.removeAllGoals(match);
            if (name.equals("mob_attacks")) mob.targetSelector.removeAllGoals(match);
        }
    }

    private static void commands(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("mobbrain")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("disable").then(Commands.argument("behaviour", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(BEHAVIOURS.keySet(), b))
                        .executes(c -> set(c.getSource(), StringArgumentType.getString(c, "behaviour"), true))))
                .then(Commands.literal("enable").then(Commands.argument("behaviour", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(BEHAVIOURS.keySet(), b))
                        .executes(c -> set(c.getSource(), StringArgumentType.getString(c, "behaviour"), false)))));
    }

    private static int set(net.minecraft.commands.CommandSourceStack src, String name, boolean disable) {
        if (!BEHAVIOURS.containsKey(name)) {
            src.sendFailure(Component.literal("Unknown behaviour: " + name));
            return 0;
        }
        if (disable) DISABLED.add(name);
        else DISABLED.remove(name);
        src.sendSuccess(() -> Component.literal((disable ? "Disabled " : "Enabled ") + name + ". Applies to mobs loaded from now on."), true);
        return 1;
    }
}
