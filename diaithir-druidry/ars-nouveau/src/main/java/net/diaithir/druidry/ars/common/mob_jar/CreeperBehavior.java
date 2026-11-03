package net.diaithir.druidry.ars.common.mob_jar;

import net.diaithir.druidry.ars.api.mob_jar.JarBehavior;
import net.diaithir.druidry.ars.common.block.tile.MobJarTile;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.monster.Creeper;

public class CreeperBehavior extends JarBehavior<Creeper> {
    @Override
    public void onRedstonePower(MobJarTile tile) {
        entityFromJar(tile).playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
    }
}
