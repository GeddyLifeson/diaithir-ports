package net.diaithir.druidry.ars;

import net.diaithir.druidry.ars.common.entity.Starbuncle;
import net.diaithir.druidry.ars.setup.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Server-side selftest for the restored mobs: spawns a Starbuncle (and every other restored mob), ticks them and fails on any exception. */
final class ArsMobsSelfTest {
    private static final Logger LOG = LoggerFactory.getLogger("ArsMobsSelfTest");

    static void run(ServerStartedEvent event) {
        ServerLevel level = event.getServer().overworld();
        try {
            // a floor to stand on, high above the world so nothing else interferes
            BlockPos floor = new BlockPos(64, 200, 64);
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    level.setBlock(floor.offset(x, 0, z), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);
                }
            }
            Starbuncle starbuncle = ModEntities.STARBUNCLE_TYPE.get().create(level, EntitySpawnReason.COMMAND);
            starbuncle.setPos(floor.getX() + 0.5, floor.getY() + 1, floor.getZ() + 0.5);
            level.addFreshEntity(starbuncle);
            for (int i = 0; i < 100; i++) {
                starbuncle.tick();
            }
            boolean starbuncleOk = starbuncle.isAlive() && !starbuncle.isRemoved();
            starbuncle.discard();

            List<String> failed = new ArrayList<>();
            List<EntityType<? extends Mob>> mobs = new ArrayList<>();
            for (Supplier<? extends EntityType<?>> s : List.<Supplier<? extends EntityType<?>>>of(
                    ModEntities.WHIRLISPRIG_TYPE, ModEntities.ENTITY_WIXIE_TYPE, ModEntities.ENTITY_DRYGMY, ModEntities.AMETHYST_GOLEM,
                    ModEntities.ENTITY_CASCADING_WEALD, ModEntities.WILDEN_HUNTER, ModEntities.WILDEN_STALKER, ModEntities.WILDEN_GUARDIAN,
                    ModEntities.WILDEN_BOSS, ModEntities.GIFT_STARBY, ModEntities.ENTITY_DUMMY, ModEntities.ENTITY_FAMILIAR_STARBUNCLE,
                    ModEntities.ENTITY_FAMILIAR_WIXIE, ModEntities.ENTITY_FAMILIAR_DRYGMY, ModEntities.ENTITY_FAMILIAR_SYLPH, ModEntities.FAMILIAR_AMETHYST_GOLEM)) {
                EntityType<?> type = s.get();
                String name = EntityType.getKey(type).toString();
                try {
                    Entity e = type.create(level, EntitySpawnReason.COMMAND);
                    e.setPos(floor.getX() + 0.5, floor.getY() + 1, floor.getZ() + 0.5);
                    level.addFreshEntity(e);
                    for (int i = 0; i < 20; i++) {
                        e.tick();
                    }
                    e.discard();
                } catch (Throwable t) {
                    failed.add(name);
                    LOG.error("ARS_MOBS_SELFTEST {} threw", name, t);
                }
            }
            if (starbuncleOk && failed.isEmpty()) {
                LOG.info("ARS_MOBS_SELFTEST PASS (Starbuncle ticked 100 times; 16 other restored mobs spawned and ticked 20 times)");
            } else {
                LOG.error("ARS_MOBS_SELFTEST FAIL starbuncleOk={} failed={}", starbuncleOk, failed);
            }
        } catch (Throwable t) {
            LOG.error("ARS_MOBS_SELFTEST FAIL: exception", t);
        }
    }
}
