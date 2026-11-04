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
        if (!(tile.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel))
            return;
        // updateMerchantTimer / increaseProfessionLevelOnUpdate are private in 26.1; only restocking is kept.
        if (villager.shouldRestock(serverLevel)) {
            villager.restock();
        }
    }
}
