package net.diaithir.druidry.ars.common.mob_jar;

import net.diaithir.druidry.ars.api.mob_jar.JarBehavior;
import net.diaithir.druidry.ars.common.block.tile.MobJarTile;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gameevent.GameEvent;

public class ChickenBehavior extends JarBehavior<Chicken> {
    @Override
    public void tick(MobJarTile tile) {
        if(tile.getLevel().isClientSide())
            return;
        Chicken chicken = this.entityFromJar(tile);
        if(isEntityBaby(chicken)){
            return;
        }
        chicken.eggTime -= 1;
        if(chicken.eggTime <= 0){
            chicken.playSound(SoundEvents.CHICKEN_EGG, 1.0F, (chicken.getRandom().nextFloat() - chicken.getRandom().nextFloat()) * 0.2F + 1.0F);
            if (chicken.level() instanceof net.minecraft.server.level.ServerLevel sl) chicken.spawnAtLocation(sl, Items.EGG);
            chicken.gameEvent(GameEvent.ENTITY_PLACE);
            chicken.eggTime = chicken.getRandom().nextInt(6000) + 6000;
        }
    }
}
