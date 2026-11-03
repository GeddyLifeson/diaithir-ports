package net.diaithir.animation;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/** Server API. Other modules call these (diaithir-firearms through a soft MethodHandle); everything is visual only. */
public final class Anim {
    /** Plays a named clip (see clips.json) on the player for everyone who sees them. */
    public static void play(ServerPlayer p, String clip) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(p, new Net.Play(p.getId(), clip));
    }

    /** Gun action on the main-hand gun: fire, dry, reload, inspect. Does nothing when the main hand is not a gun. */
    public static void gun(ServerPlayer p, String action) {
        Kind k = Kind.of(p.getMainHandItem());
        if (k != null && k.gun()) play(p, k.clip(p.getMainHandItem().getItem(), action));
    }

    /** Caster skill pose (windup into release) for class skills and spells. */
    public static void cast(ServerPlayer p) { play(p, "cast.skill"); }

    private Anim() {}
}
