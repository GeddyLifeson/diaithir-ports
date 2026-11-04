package org.valkyrienskies.mod.mixin.feature.save_mob_location_on_ship;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.entity.Mob;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.config.VSGameConfig;
import org.valkyrienskies.mod.common.entity.ShipyardPosSavable;

@Mixin(Mob.class)
public class MixinMob implements ShipyardPosSavable {

    @Unique
    public Vector3d valkyrienskies$unloadedShipyardPos = null;

    @Override
    public Vector3d valkyrienskies$getUnloadedShipyardPos() {
        return valkyrienskies$unloadedShipyardPos;
    }

    @Override
    public void valkyrienskies$setUnloadedShipyardPos(Vector3d vector3d) {
        this.valkyrienskies$unloadedShipyardPos = vector3d;
    }


    /**
     * Save mob's shipyard position to nbt, or clear it if null
     *
     * @author G_Mungus
     */
    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    public void addAdditionalSaveDataMixin(ValueOutput nbt, CallbackInfo info) {
        Vector3d position = this.valkyrienskies$getUnloadedShipyardPos();
        if (position != null && VSGameConfig.SERVER.getSaveMobsPositionOnShip()) {
            nbt.putDouble("valkyrienskies$unloadedX",position.x);
            nbt.putDouble("valkyrienskies$unloadedY",position.y);
            nbt.putDouble("valkyrienskies$unloadedZ", position.z);
        } else {
            nbt.discard("valkyrienskies$unloadedX");
            nbt.discard("valkyrienskies$unloadedY");
            nbt.discard("valkyrienskies$unloadedZ");
        }
    }


    /**
     * Read mob's shipyard position from nbt
     *
     * @author G_Mungus
     */
    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    public void readAdditionalSaveData(ValueInput nbt, CallbackInfo info) {
        if (!Double.isNaN(nbt.getDoubleOr("valkyrienskies$unloadedX", Double.NaN)) && !Double.isNaN(nbt.getDoubleOr("valkyrienskies$unloadedY", Double.NaN)) && !Double.isNaN(nbt.getDoubleOr("valkyrienskies$unloadedZ", Double.NaN))) {
            double[] xyz = {nbt.getDoubleOr("valkyrienskies$unloadedX", 0.0), nbt.getDoubleOr("valkyrienskies$unloadedY", 0.0), nbt.getDoubleOr("valkyrienskies$unloadedZ", 0.0)};
            this.valkyrienskies$setUnloadedShipyardPos(new Vector3d(xyz));
        }
    }



}

