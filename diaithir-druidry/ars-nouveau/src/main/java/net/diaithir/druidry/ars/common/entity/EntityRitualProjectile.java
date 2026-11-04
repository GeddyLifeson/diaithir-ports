package net.diaithir.druidry.ars.common.entity;

import net.diaithir.druidry.ars.client.particle.ParticleColor;
import net.diaithir.druidry.ars.client.particle.ParticleSparkleData;
import net.diaithir.druidry.ars.common.block.tile.RitualBrazierTile;
import net.diaithir.druidry.ars.setup.registry.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class EntityRitualProjectile extends ColoredProjectile {

    public BlockPos tilePos;

    public EntityRitualProjectile(Level worldIn, double x, double y, double z) {
        super(ModEntities.ENTITY_RITUAL.get(), worldIn, x, y, z);
    }

    public EntityRitualProjectile(Level worldIn, BlockPos pos) {
        super(ModEntities.ENTITY_RITUAL.get(), worldIn, pos.getX(), pos.getY(), pos.getZ());
    }

    public EntityRitualProjectile(EntityType<EntityRitualProjectile> entityAOEProjectileEntityType, Level world) {
        super(entityAOEProjectileEntityType, world);
    }

    private static int particleId() {
        return Minecraft.getInstance().options.particles().get().ordinal();
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && (tilePos == null || !(level().getBlockEntity(tilePos) instanceof RitualBrazierTile tile) || tile.ritual == null)) {
            this.remove(RemovalReason.DISCARDED);
            return;
        }

        xOld = getX();
        yOld = getY();
        zOld = getZ();

        this.setPos(getX(), getY() + Math.sin(level().getGameTime() / 10D) / 10, getZ());
        xo = getX();
        yo = getY();
        zo = getZ();

        if (level().isClientSide()) {
            int counter = 0;
            for (double j = 0; j < 3; j++) {

                counter += level().getRandom().nextInt(3);
                if (counter % (particleId() == 0 ? 1 : 2 * particleId()) == 0) {
                    level().addParticle(ParticleSparkleData.createData(getParticleColor()),
                            (float) (position().x()) + Math.sin(level().getGameTime() / 3D),
                            (float) (position().y()),
                            (float) (position().z()) + Math.cos(level().getGameTime() / 3D),
                            0.0225f * (random.nextFloat()), 0.0225f * (random.nextFloat()), 0.0225f * (random.nextFloat()));
                }
            }

            for (double j = 0; j < 3; j++) {

                counter += level().getRandom().nextInt(3);
                if (counter % (particleId() == 0 ? 1 : 2 * particleId()) == 0) {
                    level().addParticle(ParticleSparkleData.createData(new ParticleColor(2, 0, 144)),
                            (float) (position().x()) - Math.sin(level().getGameTime() / 3D),
                            (float) (position().y()),
                            (float) (position().z()) - Math.cos(level().getGameTime() / 3D),
                            0.0225f * (random.nextFloat()), 0.0225f * (random.nextFloat()), 0.0225f * (random.nextFloat()));
                }
            }
        }
    }

    @Override
    public EntityType<?> getType() {
        return ModEntities.ENTITY_RITUAL.get();
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        if (tilePos != null)
            tag.store("ritpos", BlockPos.CODEC, tilePos);
    }

    @Override
    public void readAdditionalSaveData(ValueInput compound) {
        super.readAdditionalSaveData(compound);
        tilePos = compound.read("ritpos", BlockPos.CODEC).orElse(null);
    }
}
