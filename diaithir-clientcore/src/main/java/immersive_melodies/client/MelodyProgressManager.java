package immersive_melodies.client;

import immersive_melodies.Config;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MelodyProgressManager {
    public static final MelodyProgressManager INSTANCE = new MelodyProgressManager();

    Map<Entity, MelodyProgress> progress = new ConcurrentHashMap<>();

    public MelodyProgress getProgress(Entity entity) {
        return progress.computeIfAbsent(entity, a -> new MelodyProgress());
    }

    public void setLastIndex(Entity entity, int track, int index) {
        getProgress(entity).setLastIndex(track, index);
    }

    public void setLastNote(Entity entity, float volume, float pitch, long length) {
        MelodyProgress progress = getProgress(entity);

        progress.lastNoteLongTime = System.currentTimeMillis();
        progress.lastNoteTime = entity.tickCount;
        progress.lastVolume = volume;
        progress.lastPitch = pitch;
        progress.lastLength = length;

        progress.decayTime = Math.clamp(length / 50.0f / 2.0f, 4.0f, 30.0f);
        progress.attackTime = Math.min(5.0f, progress.decayTime / 2.0f);
    }

    public boolean isClose(Vec3 pos, float distance) {
        for (Map.Entry<Entity, MelodyProgress> entry : progress.entrySet()) {
            if (entry.getValue().isPlaying() && entry.getKey().distanceToSqr(pos) < distance * distance) {
                return true;
            }
        }
        return false;
    }

    private long lastTime;

    public void sync(long time) {
        if (!Config.getInstance().autoSynchronize) return;

        if (lastTime == time) return;

        lastTime = time;
        progress.entrySet().removeIf(entry -> entry.getKey().isRemoved());

        List<Entity> list = MelodyProgressManager.INSTANCE.progress.entrySet().stream()
                .filter(m -> m.getValue().isPlaying())
                .map(Map.Entry::getKey)
                .sorted((a, b) -> {
                    boolean b1 = a instanceof Player;
                    boolean b2 = b instanceof Player;
                    if (b1 && !b2) {
                        return 1;
                    } else if (!b1 && b2) {
                        return -1;
                    } else {
                        int comparison = Long.compare(getProgress(a).worldTime, getProgress(b).worldTime);
                        return comparison != 0 ? comparison : Integer.compare(a.getId(), b.getId());
                    }
                })
                .toList();

        for (int i0 = 0; i0 < list.size(); i0++) {
            Entity entity0 = list.get(i0);
            for (int i1 = list.size() - 1; i1 > i0; i1--) {
                Entity entity1 = list.get(i1);
                if (entity0.distanceTo(entity1) < Config.getInstance().maxAudibleDistance) {
                    // Two entities are close, entity 0 will try to mimic entity 1
                    // Thus, the higher in the order the higher the priority
                    MelodyProgress progress0 = getProgress(entity0);
                    MelodyProgress progress1 = getProgress(entity1);

                    progress0.overwrite(progress1.getCurrentlyPlaying(), progress1.getStartTime(), time);

                    break;
                }
            }
        }
    }
}
