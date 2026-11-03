package net.diaithir.druidry.ars.common.mob_jar;

import net.diaithir.druidry.ars.api.mob_jar.JarBehavior;
import net.diaithir.druidry.ars.common.block.tile.MobJarTile;
import net.minecraft.world.entity.npc.villager.Villager;

public class VillagerBehavior extends JarBehavior<Villager> {
    @Override
    public void tick(MobJarTile tile) {
        if(tile.getLevel().isClientSide())
            return;
        Villager villager = entityFromJar(tile);
        if (!villager.isTrading() && villager.updateMerchantTimer > 0) {
            --villager.updateMerchantTimer;
            if (villager.updateMerchantTimer <= 0) {
                if (villager.increaseProfessionLevelOnUpdate) {
                    villager.increaseMerchantCareer();
                    villager.increaseProfessionLevelOnUpdate = false;
                }
            }
        }
        if(villager.shouldRestock()){
            villager.restock();
        }
    }
}
