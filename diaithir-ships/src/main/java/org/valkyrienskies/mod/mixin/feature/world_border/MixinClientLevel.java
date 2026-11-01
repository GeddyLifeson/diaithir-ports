package org.valkyrienskies.mod.mixin.feature.world_border;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.mod.mixinducks.world.OfLevel;

@Mixin(ClientLevel.class)
public class MixinClientLevel {

    // In 26.1 the border is no longer a field of Level (ClientLevel loads it from saved data on each call),
    // so attach the level every time the border is handed out.
    @ModifyReturnValue(method = "getWorldBorder", at = @At("RETURN"))
    private WorldBorder vs$attachLevelToBorder(final WorldBorder border) {
        if (border != null) {
            ((OfLevel) (Object) border).setLevel(Level.class.cast(this));
        }
        return border;
    }

}
