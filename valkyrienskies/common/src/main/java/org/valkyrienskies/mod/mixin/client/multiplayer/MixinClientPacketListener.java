package org.valkyrienskies.mod.mixin.client.multiplayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.IShipObjectWorldClientCreator;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {

    @Shadow
    private ClientLevel level;

    // 26.2: PacketUtils.ensureRunningOnSameThread lost its BlockableEventLoop overload; the
    // client-side call site (handleLogin) now uses the PacketProcessor overload.
    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V",
            shift = Shift.AFTER
        ),
        method = "handleLogin"
    )
    private void beforeHandleLogin(final ClientboundLoginPacket packet, final CallbackInfo ci) {
        ((IShipObjectWorldClientCreator) Minecraft.getInstance()).createShipObjectWorldClient();
    }

    /**
     * Spawn [ShipMountingEntity] on client side
     */
    // 26.2: same ensureRunningOnSameThread overload change as beforeHandleLogin above.
    @Inject(method = "handleAddEntity",
        at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V"),
        cancellable = true)
    private void handleShipMountingEntity(final ClientboundAddEntityPacket packet, final CallbackInfo ci) {
        if (packet.getType().equals(ValkyrienSkiesMod.SHIP_MOUNTING_ENTITY_TYPE)) {
            ci.cancel();
            final double d = packet.getX();
            final double e = packet.getY();
            final double f = packet.getZ();
            final Entity entity = ValkyrienSkiesMod.SHIP_MOUNTING_ENTITY_TYPE.create(level, EntitySpawnReason.LOAD);
            final int i = packet.getId();
            entity.syncPacketPositionCodec(d, e, f);
            entity.snapTo(d, e, f);
            entity.setXRot((float) (packet.getXRot() * 360) / 256.0f);
            entity.setYRot((float) (packet.getYRot() * 360) / 256.0f);
            entity.setId(i);
            entity.setUUID(packet.getUUID());
            this.level.addEntity(entity);
        }
    }

    /**
     * 26.2 replacement for the old "collapse the teleport lerp to 1 step for ship-mounted entities"
     * trick. {@code Entity.lerpTo(..., steps)} is gone; teleports now go through the entity's
     * {@link net.minecraft.world.entity.InterpolationHandler}. Interpolating a ship-mounted entity
     * across a teleport is wrong here because the intermediate positions are in world space while the
     * entity actually lives in shipyard space - it would render strung out between the two (often
     * through unloaded chunks) until the lerp finished. Cancelling the interpolation snaps it straight
     * to the target, which is what the old 1-step lerp effectively did.
     */
    @Inject(method = "handleTeleportEntity", at = @At("TAIL"))
    private void afterHandleTeleportEntity(final ClientboundTeleportEntityPacket packet, final CallbackInfo ci) {
        if (this.level == null) {
            return;
        }
        final Entity entity = this.level.getEntity(packet.id());
        if (entity != null && VSGameUtilsKt.getShipMountedToData(entity, null) != null) {
            entity.getInterpolation().cancel();
        }
    }
}
