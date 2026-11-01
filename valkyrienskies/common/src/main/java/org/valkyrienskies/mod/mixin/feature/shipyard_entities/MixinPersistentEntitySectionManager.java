package org.valkyrienskies.mod.mixin.feature.shipyard_entities;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.entity.handling.VSEntityManager;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.mod.mixinducks.world.OfLevel;

@Mixin(PersistentEntitySectionManager.class)
public abstract class MixinPersistentEntitySectionManager implements OfLevel {
    @Shadow
    @Final
    EntitySectionStorage<Entity> sectionStorage;

    @Unique
    private Level level;

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public void setLevel(final Level level) {
        this.level = level;
        ((OfLevel) this.sectionStorage).setLevel(level);
    }

    @Inject(
        method = "addEntity",
        at = @At(
            value = "HEAD"
        )
    )
    <T extends EntityAccess> void preAddEntity(T entityAccess, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        if (entityAccess instanceof final Entity entity) {
            final LoadedShip ship =
                VSGameUtilsKt.getLoadedShipManagingPos(entity.level(), VectorConversionsMCKt.toJOML(entity.position()));
            if (ship != null) {
                VSEntityManager.INSTANCE.getHandler(entity).freshEntityInShipyard(entity, ship);
            }
        }
    }
}
