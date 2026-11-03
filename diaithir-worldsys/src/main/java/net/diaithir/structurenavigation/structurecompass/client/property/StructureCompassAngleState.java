// Ported from Structure Compass (MIT) https://github.com/Mrbysco/StructureCompass
// Modifications: Null owner guard, registry names changed.
package net.diaithir.structurenavigation.structurecompass.client.property;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.diaithir.structurenavigation.structurecompass.component.StructureInfo;
import net.diaithir.structurenavigation.structurecompass.registry.CompassComponents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.NeedleDirectionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class StructureCompassAngleState extends NeedleDirectionHelper {
    public static final MapCodec<StructureCompassAngleState> MAP_CODEC = RecordCodecBuilder.mapCodec(
            wobble -> wobble.group(
                            Codec.BOOL.optionalFieldOf("wobble", Boolean.FALSE).forGetter(StructureCompassAngleState::wobble)
                    )
                    .apply(wobble, StructureCompassAngleState::new)
    );
    private final Wobbler wobbler;

    public StructureCompassAngleState(boolean wobble) {
        super(wobble);
        this.wobbler = this.newWobbler(0.8F);
    }

    @Override
    protected float calculate(ItemStack stack, ClientLevel level, int seed, ItemOwner owner) {
        if (owner == null) {
            return 0.0F;
        }
        StructureInfo compassData = stack.get(CompassComponents.STRUCTURE_INFO.get());
        GlobalPos globalpos = compassData != null ? compassData.globalPos() : null;
        long gameTime = level.getGameTime();
        boolean valid = isValidCompassTargetPos(owner, globalpos);
        return !valid
                ? 0.0F
                : this.getRotationTowardsCompassTarget(owner, gameTime, globalpos.pos());
    }

    private float getRotationTowardsCompassTarget(ItemOwner itemOwner, long gameTime, BlockPos targetPos) {
        float f = (float) getAngleFromEntityToPos(itemOwner, targetPos);
        float f1 = getWrappedVisualRotationY(itemOwner);
        if (itemOwner.asLivingEntity() instanceof Player player && player.isLocalPlayer() && player.level().tickRateManager().runsNormally()) {
            if (this.wobbler.shouldUpdate(gameTime)) {
                this.wobbler.update(gameTime, 0.5F - (f1 - 0.25F));
            }

            float f3 = f + this.wobbler.rotation();
            return Mth.positiveModulo(f3, 1.0F);
        }

        float f2 = 0.5F - (f1 - 0.25F - f);
        return Mth.positiveModulo(f2, 1.0F);
    }

    private static boolean isValidCompassTargetPos(ItemOwner itemOwner, GlobalPos pos) {
        return pos != null
                && pos.dimension() == itemOwner.level().dimension()
                && !(pos.pos().distToCenterSqr(itemOwner.position()) < 1.0E-5F);
    }

    private static double getAngleFromEntityToPos(ItemOwner itemOwner, BlockPos pos) {
        Vec3 vec3 = Vec3.atCenterOf(pos);
        return Math.atan2(vec3.z() - itemOwner.position().z(), vec3.x() - itemOwner.position().x()) / (float) (Math.PI * 2);
    }

    private static float getWrappedVisualRotationY(ItemOwner itemOwner) {
        return Mth.positiveModulo(itemOwner.getVisualRotationYInDegrees() / 360.0F, 1.0F);
    }
}
