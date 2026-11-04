package net.diaithir.worldvisuals.soundphysics;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

final class Occlusion {
    private Occlusion() {}

    static void onSound(PlaySoundEvent e) {
        SoundInstance s = e.getSound();
        Minecraft mc = Minecraft.getInstance();
        if (s == null || mc.level == null || mc.player == null || !Feature.ENABLED.get()
            || s.isRelative() || s.isLooping() || s.getAttenuation() == SoundInstance.Attenuation.NONE) {
            return;
        }
        Vec3 from = mc.player.getEyePosition(), to = new Vec3(s.getX(), s.getY(), s.getZ());
        double len = from.distanceTo(to);
        if (len < 1.5 || len > 64) {
            return;
        }
        int solid = 0;
        BlockPos last = null;
        for (double t = 0.5; t < len - 0.5; t += 0.5) {
            Vec3 p = from.lerp(to, t / len);
            BlockPos bp = BlockPos.containing(p);
            if (bp.equals(last)) continue;
            last = bp;
            if (mc.level.getBlockState(bp).isSolidRender()) solid++;
        }
        if (solid == 0) {
            return;
        }
        float f = Muffle.factor(solid, Feature.PER_BLOCK.get(), Feature.MAX_BLOCKS.get());
        e.setSound(new SimpleSoundInstance(SoundEvent.createVariableRangeEvent(s.getIdentifier()), s.getSource(),
            s.getVolume() * f, s.getPitch(), RandomSource.create(), s.getX(), s.getY(), s.getZ()));
    }
}
