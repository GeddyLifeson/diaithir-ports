package net.diaithir.hostile.hordes.init;

import net.diaithir.hostile.hordes.platform.Services;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

/**
 * 1.20.1 → 26.1.2 GameRules port. Two API changes from the source:
 *
 * 1) Type shape: GameRules.Key<GameRules.BooleanValue> / GameRules.IntegerValue have
 *    been replaced by GameRule<Boolean> / GameRule<Integer> (top-level types in
 *    net.minecraft.world.level.gamerules).
 *
 * 2) Registration timing: BuiltInRegistries.GAME_RULE is frozen before mod
 *    construction in 26.1.2. The vanilla GameRules.registerBoolean/registerInteger
 *    are private and only callable during vanilla static init. We delegate to
 *    Services.PLATFORM, which uses Fabric's GameRuleBuilder.buildAndRegister at
 *    ModInitializer time on Fabric, and the AT'd GameRules.registerBoolean from a
 *    RegisterEvent listener on NeoForge.
 *
 *    Fields are non-final by necessity — they're populated by {@link #register()}
 *    which is called from each loader's entry point at the right phase.
 *
 * 3) ID format: Identifier.assertValidPath in 26.1.2 only permits [a-z0-9/._-], so
 *    the source's camelCase IDs (hordeStacking/hordeMultiplying/ironGolemRegenPower)
 *    became snake_case (horde_stacking/horde_multiplying/iron_golem_regen_power).
 *    Lang keys in en_us.json updated to match.
 *
 * 4) IRONGOLEMREGENPOWER min set to 0 — source's IntegerValue.create(4) defaulted min
 *    to Integer.MIN_VALUE, but 0 is the practical floor (the procedure already guards
 *    `< 1` to skip regen, so values < 1 mean "off").
 *
 * NEW (post-port, beyond the original mod):
 * 5) HORDE_SPAWNER_CHANCE — 1-in-N nightly chance (at midnight) to spawn a giant
 *    horde of zombie-tribe mobs around a random player in the overworld. 0 disables;
 *    1 means every night; default 20 ≈ once every 20 in-game nights. See
 *    HordeSpawnerProcedure for the spawn logic.
 */
public class HordeHoardModGameRules {

    public static GameRule<Boolean> HORDESTACKING;
    public static GameRule<Boolean> HORDEMULTIPLYING;
    public static GameRule<Integer> IRONGOLEMREGENPOWER;
    public static GameRule<Integer> HORDE_SPAWNER_CHANCE;

    public static void register() {
        HORDESTACKING = Services.PLATFORM.registerBooleanGameRule("horde_stacking", GameRuleCategory.MOBS, true);
        HORDEMULTIPLYING = Services.PLATFORM.registerBooleanGameRule("horde_multiplying", GameRuleCategory.MOBS, true);
        IRONGOLEMREGENPOWER = Services.PLATFORM.registerIntegerGameRule("iron_golem_regen_power", GameRuleCategory.MOBS, 4, 0);
        HORDE_SPAWNER_CHANCE = Services.PLATFORM.registerIntegerGameRule("horde_spawner_chance", GameRuleCategory.MOBS, 20, 0);
    }
}
