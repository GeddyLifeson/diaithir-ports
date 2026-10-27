package me.desht.pneumaticcraft.client.sound;

/**
 * 26.1 port: moving sounds are not yet ported (client side); calls are accepted and ignored.
 */
public class MovingSounds {
    public enum Sound {
        JET_BOOTS,
        MINIGUN,
        ELEVATOR,
        AIR_LEAK,
        JACKHAMMER
    }

    public static void playMovingSound(Sound s, Object focus, Object... extraData) {
        // TODO client port
    }
}
