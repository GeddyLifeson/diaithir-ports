package net.diaithir.bosses;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/** The altar's moving picture: a particle and sound flourish for each rite step, so a rite reads as a ceremony and not as chat lines. */
final class RiteFx {
    private RiteFx() {}

    /** The rite is opened. */
    static void start(ServerLevel sl, BlockPos altar) {
        ring(sl, altar, ParticleTypes.ENCHANT, 2.2, 0.4, 32);
        rise(sl, altar, ParticleTypes.END_ROD, 12);
        sound(sl, altar, SoundEvents.BEACON_ACTIVATE, 0.9F, 0.7F);
    }

    /** A step was completed; {@code type} is the step just finished. */
    static void stepDone(ServerLevel sl, BlockPos altar, String type) {
        switch (type == null ? "" : type) {
            case "feast" -> { rise(sl, altar, ParticleTypes.CAMPFIRE_COSY_SMOKE, 10); ring(sl, altar, ParticleTypes.HAPPY_VILLAGER, 1.5, 0.6, 16); }
            case "offer" -> { rise(sl, altar, ParticleTypes.SOUL_FIRE_FLAME, 14); sound(sl, altar, SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 0.8F); }
            case "hunt" -> { ring(sl, altar, ParticleTypes.CRIMSON_SPORE, 3.0, 0.2, 40); sound(sl, altar, SoundEvents.RAVAGER_ROAR, 0.5F, 1.4F); }
            case "stones" -> { ring(sl, altar, ParticleTypes.GLOW, 4.0, 0.3, 48); sound(sl, altar, SoundEvents.AMETHYST_CLUSTER_BREAK, 1.0F, 0.6F); }
            case "hour" -> { rise(sl, altar, ParticleTypes.END_ROD, 20); sound(sl, altar, SoundEvents.BELL_BLOCK, 1.2F, 0.5F); }
            case "coop" -> { ring(sl, altar, ParticleTypes.ELECTRIC_SPARK, 2.0, 0.8, 30); sound(sl, altar, SoundEvents.TRIDENT_THUNDER.value(), 0.6F, 1.2F); }
            default -> ring(sl, altar, ParticleTypes.ENCHANT, 2.5, 0.5, 24);
        }
        sound(sl, altar, SoundEvents.PLAYER_LEVELUP, 0.8F, 0.6F);
    }

    /** One second of a defend step: a slow spiral of flame round the altar. */
    static void defendPulse(ServerLevel sl, BlockPos altar, long secs, double radius) {
        double a0 = secs * 0.9;
        for (int i = 0; i < 6; i++) {
            double a = a0 + i * Math.PI / 3, h = 0.3 + 0.35 * i;
            Vec3 c = Vec3.atCenterOf(altar);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, c.x + Math.cos(a) * radius, c.y + h, c.z + Math.sin(a) * radius, 1, 0, 0, 0, 0);
        }
    }

    /** The rite failed: the light gutters out. */
    static void fail(ServerLevel sl, BlockPos altar) {
        rise(sl, altar, ParticleTypes.LARGE_SMOKE, 16);
        sound(sl, altar, SoundEvents.BEACON_DEACTIVATE, 1.0F, 0.8F);
    }

    /** The last step: the boss is called up. */
    static void wake(ServerLevel sl, BlockPos altar) {
        ring(sl, altar, ParticleTypes.PORTAL, 3.0, 0.2, 64);
        rise(sl, altar, ParticleTypes.REVERSE_PORTAL, 24);
        sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, altar.getX() + 0.5, altar.getY() + 1.5, altar.getZ() + 0.5, 1, 0, 0, 0, 0);
        sound(sl, altar, SoundEvents.WITHER_SPAWN, 0.8F, 1.1F);
    }

    private static void ring(ServerLevel sl, BlockPos altar, ParticleOptions p, double r, double y, int n) {
        Vec3 c = Vec3.atCenterOf(altar);
        for (int i = 0; i < n; i++) {
            double a = i * 2 * Math.PI / n;
            sl.sendParticles(p, c.x + Math.cos(a) * r, c.y + y, c.z + Math.sin(a) * r, 1, 0, 0.02, 0, 0.01);
        }
    }

    private static void rise(ServerLevel sl, BlockPos altar, ParticleOptions p, int n) {
        Vec3 c = Vec3.atCenterOf(altar);
        sl.sendParticles(p, c.x, c.y + 0.6, c.z, n, 0.25, 0.9, 0.25, 0.04);
    }

    private static void sound(ServerLevel sl, BlockPos altar, SoundEvent s, float vol, float pitch) {
        sl.playSound(null, altar, s, SoundSource.BLOCKS, vol, pitch);
    }
}
