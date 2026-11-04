package net.diaithir.druidry.ars.api;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.util.UUID;

/** Fake player used for spells cast by non-player sources (turrets, dispensers, ...). */
public class ANFakePlayer extends FakePlayer {
    public static final GameProfile PROFILE = new GameProfile(UUID.fromString("5b7a7a9e-9c52-4a7e-8d8f-2f6f0c8f5a11"), "[Ars Nouveau]");

    public ANFakePlayer(ServerLevel level, GameProfile name) {
        super(level, name);
    }

    public static ANFakePlayer getPlayer(ServerLevel level) {
        return new ANFakePlayer(level, PROFILE);
    }

    public static ANFakePlayer getPlayer(ServerLevel level, UUID uuid) {
        return new ANFakePlayer(level, new GameProfile(uuid, PROFILE.name()));
    }

    public static Player getOrFakePlayer(ServerLevel level, LivingEntity caster) {
        if (caster instanceof Player player) {
            return player;
        }
        return getPlayer(level);
    }
}
