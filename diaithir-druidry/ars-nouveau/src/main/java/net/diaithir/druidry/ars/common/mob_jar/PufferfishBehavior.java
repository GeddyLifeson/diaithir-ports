package net.diaithir.druidry.ars.common.mob_jar;

import net.diaithir.druidry.ars.api.mob_jar.JarBehavior;
import net.diaithir.druidry.ars.common.block.tile.MobJarTile;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.fish.Pufferfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class PufferfishBehavior extends JarBehavior<Pufferfish> {

    // index 0: inflate counter, index 1: deflate timer (replaces the vanilla private fields)
    private final Map<Pufferfish, int[]> counters = new WeakHashMap<>();

    private int[] getCounters(Pufferfish pufferfish) {
        return counters.computeIfAbsent(pufferfish, p -> new int[2]);
    }

    @Override
    public void tick(MobJarTile tile) {
        super.tick(tile);
        Level level = tile.getLevel();
        Pufferfish pufferfish = (Pufferfish) tile.getEntity();
        if(level == null || level.isClientSide() || pufferfish == null){
            return;
        }
        int[] c = getCounters(pufferfish);
        boolean playerNearby = mobsNearby(tile, pufferfish);
        BlockPos worldPosition = tile.getBlockPos();
        if (playerNearby) {
            if (pufferfish.getPuffState() == 0) {
                pufferfish.playSound(SoundEvents.PUFFER_FISH_BLOW_UP, 1.0f, pufferfish.getVoicePitch());
                pufferfish.setPuffState(1);
                level.updateNeighborsAt(worldPosition, BlockRegistry.ITEM_DETECTOR.get());
                syncClient(tile);
            } else if (c[0] > 40 && pufferfish.getPuffState() == 1) {
                pufferfish.playSound(SoundEvents.PUFFER_FISH_BLOW_UP, 1.0f, pufferfish.getVoicePitch());
                pufferfish.setPuffState(2);
                level.updateNeighborsAt(worldPosition, BlockRegistry.ITEM_DETECTOR.get());
                syncClient(tile);
            }
            c[0] = c[0] + 1;
        } else if (pufferfish.getPuffState() != 0) {
            if (c[1] > 60 && pufferfish.getPuffState() == 2) {
                pufferfish.playSound(SoundEvents.PUFFER_FISH_BLOW_OUT, 1.0f, pufferfish.getVoicePitch());
                pufferfish.setPuffState(1);
                level.updateNeighborsAt(worldPosition, BlockRegistry.ITEM_DETECTOR.get());
                syncClient(tile);
            } else if (c[1] > 100 && pufferfish.getPuffState() == 1) {
                pufferfish.playSound(SoundEvents.PUFFER_FISH_BLOW_OUT, 1.0f, pufferfish.getVoicePitch());
                pufferfish.setPuffState(0);
                level.updateNeighborsAt(worldPosition, BlockRegistry.ITEM_DETECTOR.get());
                syncClient(tile);
            }
            c[1] = c[1] + 1;
        }
        if(!playerNearby){
            c[0] = 0;
            if(pufferfish.getPuffState() == 0) {
                c[1] = 0;
            }
        }
    }

    private static boolean isScary(Pufferfish pufferfish, LivingEntity target) {
        if (target == pufferfish || !target.isAlive() || target.isSpectator()) {
            return false;
        }
        if (target instanceof Player player && player.isCreative()) {
            return false;
        }
        return !target.getType().builtInRegistryHolder().is(EntityTypeTags.NOT_SCARY_FOR_PUFFERFISH);
    }

    public boolean mobsNearby(MobJarTile tile, Pufferfish pufferfish){
        List<LivingEntity> list = pufferfish.level().getEntitiesOfClass(LivingEntity.class, new AABB(tile.getBlockPos()).inflate(2.4D), (p_149015_) -> {
            return isScary(pufferfish, p_149015_);
        });
        return !list.isEmpty();
    }

    @Override
    public int getSignalPower(MobJarTile tile) {
        Pufferfish pufferfish = (Pufferfish) tile.getEntity();
        if(pufferfish.getPuffState() == 1){
            return 8;
        }else if(pufferfish.getPuffState() == 2){
            return 15;
        }
        return 0;
    }
}
