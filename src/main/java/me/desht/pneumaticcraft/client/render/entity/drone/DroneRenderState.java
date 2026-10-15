package me.desht.pneumaticcraft.client.render.entity.drone;

import me.desht.pneumaticcraft.client.util.ProgressingLine;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Everything the drone model and its layers need, copied off the entity at extraction time. */
public class DroneRenderState extends LivingEntityRenderState {
    public float propRotation;
    public int droneColor;
    public final ItemStackRenderState heldItem = new ItemStackRenderState();
    public boolean flipHeldItem;
    public float heldItemScale;
    public boolean hasMinigun;
    public @Nullable Minigun minigun;
    public @Nullable ProgressingLine targetLine;
    public @Nullable ProgressingLine oldTargetLine;
    /** Dig laser end point, relative to the drone; null when not digging */
    public @Nullable Vec3 digTarget;
    public double laserOffsetY;
    public int laserColor;
}
