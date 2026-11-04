package net.diaithir.druidry.ars.common.mob_jar;

import net.diaithir.druidry.ars.api.mob_jar.JarBehavior;
import net.diaithir.druidry.ars.common.block.tile.MobJarTile;
import net.minecraft.world.entity.monster.Blaze;

public class BlazeBehavior extends JarBehavior<Blaze> {

    @Override
    public int lightLevel(MobJarTile pBlockEntity) {
        return 15;
    }
}
