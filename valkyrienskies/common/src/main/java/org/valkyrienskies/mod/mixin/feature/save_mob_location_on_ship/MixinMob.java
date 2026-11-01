package org.valkyrienskies.mod.mixin.feature.save_mob_location_on_ship;

import com.mojang.serialization.Codec;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    public void addAdditionalSaveDataMixin(ValueOutput output, CallbackInfo info) {
        Vector3d position = this.valkyrienskies$getUnloadedShipyardPos();
        if (position != null && VSGameConfig.SERVER.getSaveMobsPositionOnShip()) {
            output.putDouble("valkyrienskies$unloadedX", position.x);
            output.putDouble("valkyrienskies$unloadedY", position.y);
            output.putDouble("valkyrienskies$unloadedZ", position.z);
        } else {
            output.discard("valkyrienskies$unloadedX");
            output.discard("valkyrienskies$unloadedY");
            output.discard("valkyrienskies$unloadedZ");
        }
    }


    /**
     * Read mob's shipyard position from nbt
     *
     * @author G_Mungus
     */
    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    public void readAdditionalSaveData(ValueInput input, CallbackInfo info) {
        if (input.read("valkyrienskies$unloadedX", Codec.DOUBLE).isPresent()
            && input.read("valkyrienskies$unloadedY", Codec.DOUBLE).isPresent()
            && input.read("valkyrienskies$unloadedZ", Codec.DOUBLE).isPresent()) {
            double[] xyz = {
                input.getDoubleOr("valkyrienskies$unloadedX", 0.0),
                input.getDoubleOr("valkyrienskies$unloadedY", 0.0),
                input.getDoubleOr("valkyrienskies$unloadedZ", 0.0)
            };
            this.valkyrienskies$setUnloadedShipyardPos(new Vector3d(xyz));
        }
    }



}
