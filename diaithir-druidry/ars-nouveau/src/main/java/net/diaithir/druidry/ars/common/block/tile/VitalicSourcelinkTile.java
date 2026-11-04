package net.diaithir.druidry.ars.common.block.tile;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.entity.IDispellable;
import net.diaithir.druidry.ars.api.entity.ISummon;
import net.diaithir.druidry.ars.api.source.SourcelinkEventQueue;
import net.diaithir.druidry.ars.client.particle.ParticleUtil;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = ArsNouveau.HOST)
public class VitalicSourcelinkTile extends SourcelinkTile {
    public VitalicSourcelinkTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    // Test for a quark tag that has disabled baby growth.
    private static final String TAG_POISONED = "quark:poison_potato_applied";

    public VitalicSourcelinkTile(BlockPos pos, BlockState state) {
        super(BlockRegistry.VITALIC_TILE.get(), pos, state);
    }

    @Override
    public int getMaxSource() {
        return 2500;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide() && level.getGameTime() % 60 == 0) {
            for (Animal entity : level.getEntitiesOfClass(Animal.class, new AABB(worldPosition).inflate(6))) {
                if (entity.isBaby()) {
                    if (entity.getAge() < 0) {
                        if (ModList.get().isLoaded("quark") && entity.getPersistentData().contains(TAG_POISONED)) {
                            return;
                        }
                        entity.setAge(Math.min(0, entity.getAge() + 500));
                        this.addSource(10);
                        ParticleUtil.spawnFollowProjectile(level, entity.blockPosition(), this.worldPosition, this.getColor());
                    }
                }

            }
        }
    }

    @SubscribeEvent
    public static void babySpawnEvent(BabyEntitySpawnEvent e) {
        int mana = 600;
        SourcelinkEventQueue.addManaEvent(e.getParentA().level(), VitalicSourcelinkTile.class, mana, e, e.getParentA().blockPosition());
    }

    @SubscribeEvent
    public static void livingDeath(LivingDeathEvent e) {
        if (e.getEntity().level().isClientSide() || e.getEntity() instanceof IDispellable || e.getEntity() instanceof ISummon)
            return;
        int mana = 200;
        SourcelinkEventQueue.addManaEvent(e.getEntity().level(), VitalicSourcelinkTile.class, mana, e, e.getEntity().blockPosition());
    }

    @Override
    public boolean usesEventQueue() {
        return true;
    }
}
