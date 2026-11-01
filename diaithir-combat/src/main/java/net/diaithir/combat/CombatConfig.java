package net.diaithir.combat;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Common config (diaithircombat-common.toml). */
public final class CombatConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED, STAMINA_ON, PARRY_ON, DODGE_ON, RANGED_ON, VANILLA_FALLBACK;
    public static final ModConfigSpec.DoubleValue DAMAGE_MULT, RANGE_MULT, STAMINA_MAX, STAMINA_REGEN, STAMINA_COST_MULT,
            EXHAUSTED_DAMAGE, AGE_SPEED_PER_AGE, AGE_SPEED_CAP, DODGE_COST, DODGE_STRENGTH, DRAW_TIME_MULT, DROP_MULT;
    public static final ModConfigSpec.IntValue STAMINA_DELAY, DODGE_COOLDOWN, DODGE_IFRAMES, SHIELD_PARRY_WINDOW,
            DEFAULT_AGE, PARRY_STUN;
    public static final ModConfigSpec.ConfigValue<String> AGE_OBJECTIVE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("general");
        ENABLED = b.comment("Master switch for arc attacks.").define("enabled", true);
        VANILLA_FALLBACK = b.comment("Weapons with no category swing like vanilla.").define("vanillaFallback", true);
        DAMAGE_MULT = b.comment("Global multiplier on category attack damage.").defineInRange("damageMult", 1.0, 0.1, 10.0);
        RANGE_MULT = b.comment("Global multiplier on category reach.").defineInRange("rangeMult", 1.0, 0.5, 2.0);
        b.pop().push("stamina");
        STAMINA_ON = b.define("enabled", true);
        STAMINA_MAX = b.defineInRange("max", 20.0, 1.0, 200.0);
        STAMINA_REGEN = b.comment("Per tick once the delay has passed.").defineInRange("regenPerTick", 0.25, 0.0, 10.0);
        STAMINA_DELAY = b.comment("Ticks after the last spend before regen starts.").defineInRange("regenDelay", 20, 0, 400);
        STAMINA_COST_MULT = b.defineInRange("costMult", 1.0, 0.0, 10.0);
        EXHAUSTED_DAMAGE = b.comment("Damage factor when a swing is made without enough stamina.").defineInRange("exhaustedDamage", 0.6, 0.0, 1.0);
        b.pop().push("age");
        AGE_OBJECTIVE = b.comment("Scoreboard objective holding the player's Age (1 to 9).").define("objective", "age");
        DEFAULT_AGE = b.comment("Age used when the objective is missing or unset.").defineInRange("defaultAge", 1, 1, 9);
        AGE_SPEED_PER_AGE = b.comment("Combo speed gained per Age above I (0.06 = 6 percent).").defineInRange("speedPerAge", 0.06, 0.0, 1.0);
        AGE_SPEED_CAP = b.comment("Largest combo speed factor.").defineInRange("speedCap", 1.6, 1.0, 5.0);
        b.pop().push("parry");
        PARRY_ON = b.define("enabled", true);
        SHIELD_PARRY_WINDOW = b.comment("Ticks after raising a shield in which a hit is parried.").defineInRange("shieldWindow", 7, 0, 40);
        PARRY_STUN = b.comment("Slowness ticks put on the attacker.").defineInRange("stunTicks", 40, 0, 400);
        b.pop().push("dodge");
        DODGE_ON = b.define("enabled", true);
        DODGE_COOLDOWN = b.defineInRange("cooldown", 40, 0, 1200);
        DODGE_IFRAMES = b.defineInRange("iframes", 8, 0, 40);
        DODGE_COST = b.defineInRange("staminaCost", 6.0, 0.0, 100.0);
        DODGE_STRENGTH = b.comment("Horizontal launch speed.").defineInRange("strength", 0.9, 0.1, 3.0);
        b.pop().push("ranged");
        RANGED_ON = b.define("enabled", true);
        DRAW_TIME_MULT = b.comment("Global multiplier on bow draw ticks.").defineInRange("drawTimeMult", 1.0, 0.25, 4.0);
        DROP_MULT = b.comment("Global multiplier on arrow drop reduction.").defineInRange("dropMult", 1.0, 0.0, 2.0);
        b.pop();
        SPEC = b.build();
    }

    private CombatConfig() {}
}
