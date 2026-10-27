package net.id.paradise_lost.world;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import net.id.paradise_lost.ModConstants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;

public final class ParadiseLostGameRules {
    private static final net.id.paradise_lost.registration.RegistrationProvider<GameRule<?>> GAME_RULES =
            net.id.paradise_lost.registration.RegistrationProvider.get(net.minecraft.core.registries.Registries.GAME_RULE, ModConstants.MOD_ID);
    public static final GameRule<Boolean> PARADISE_VOID_KILLS = bool("paradise_void_kills", GameRuleCategory.PLAYER, false);
    public static final GameRule<Boolean> PARADISE_PORTAL_ENABLED = bool("paradise_portal_enabled", GameRuleCategory.UPDATES, true);

    private static GameRule<Boolean> bool(String id, GameRuleCategory category, boolean defaultValue) {
        GameRule<Boolean> rule = new GameRule<>(category, GameRuleType.BOOL, BoolArgumentType.bool(), GameRuleTypeVisitor::visitBoolean, Codec.BOOL, b -> b ? 1 : 0, defaultValue, FeatureFlagSet.of());
        GAME_RULES.register(id, () -> rule);
        return rule;
    }

    public static <T> T get(net.minecraft.world.level.Level level, GameRule<T> rule) {
        return level instanceof net.minecraft.server.level.ServerLevel server ? server.getGameRules().get(rule) : rule.defaultValue();
    }

    private ParadiseLostGameRules() {}

    public static void init() {}
}
