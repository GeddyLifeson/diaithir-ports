package net.diaithir.druidry.ars.common.mob_jar;

import net.diaithir.druidry.ars.api.mob_jar.JarBehavior;
import net.diaithir.druidry.ars.common.block.tile.MobJarTile;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.phys.Vec3;

public class SquidBehavior extends JarBehavior<Squid> {
    public static Vec3 TRANSLATE = new Vec3(0.0D, 0.42D, 0.0D);
    public static Vec3 SCALE = new Vec3(-.4f, -.4f, -.4f);
    @Override
    public Vec3 scaleOffset(MobJarTile pBlockEntity) {
        return SCALE;
    }

    @Override
    public Vec3 translate(MobJarTile pBlockEntity) {
        return TRANSLATE;
    }
}
