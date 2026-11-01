package org.valkyrienskies.mod.mixin.feature.teleport_reconnected_player_to_ship;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.config.VSGameConfig;
import org.valkyrienskies.mod.common.util.EntityDraggingInformation;
import org.valkyrienskies.mod.common.util.IEntityDraggingInformationProvider;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player {

    // 26.2: Entity.serverLevel() was removed; cast level() instead.
    @org.spongepowered.asm.mixin.Unique
    private ServerLevel vs$serverLevel() {
        return (ServerLevel) this.level();
    }

    public MixinServerPlayer(final Level level, final GameProfile gameProfile) {
        super(level, gameProfile);
        throw new IllegalStateException("Unreachable");
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    void teleportToShip(final ValueInput input, final CallbackInfo ci) {
        if (!VSGameConfig.SERVER.getTeleportReconnectedPlayers())
            return;

        if (input.read("LastShipId", Codec.LONG).isEmpty())
            return; // Player did not disconnect off of any ship

        final long lastShipId = input.getLongOr("LastShipId", 0L);

        final Ship ship = VSGameUtilsKt.getShipObjectWorld(vs$serverLevel()).getAllShips().getById(lastShipId);
        // Don't teleport if the ship doesn't exist anymore
        if (ship == null)
            return;

        // Translate ship coords to world coords
        final double x = input.getDoubleOr("RelativeShipX", 0.0);
        final double y = input.getDoubleOr("RelativeShipY", 0.0);
        final double z = input.getDoubleOr("RelativeShipZ", 0.0);

        final Vector3d playerShipPosition = new Vector3d(x, y, z);
        final Vector3d playerWorldPosition = ship.getShipToWorld().transformPosition(playerShipPosition);

        setPos(playerWorldPosition.x, playerWorldPosition.y, playerWorldPosition.z);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    void rememberLastShip(final ValueOutput output, final CallbackInfo ci) {
        final EntityDraggingInformation draggingInformation = ((IEntityDraggingInformationProvider) this).getDraggingInformation();

        if (!draggingInformation.isEntityBeingDraggedByAShip())
            return;

        @Nullable final Long lastShipId = draggingInformation.getLastShipStoodOn();
        if (lastShipId == null)
            return;

        final Ship ship = VSGameUtilsKt.getShipObjectWorld(vs$serverLevel()).getAllShips().getById(lastShipId);
        if (ship == null)
            return;

        output.putLong("LastShipId", lastShipId);

        // Get position relative to ship
        // (Technically, this grabs the position in the shipyard, but it works well enough...)
        final Vector3d playerWorldPosition = new Vector3d(getX(), getY(), getZ());
        final Vector3d playerShipPosition = ship.getWorldToShip().transformPosition(playerWorldPosition);

        output.putDouble("RelativeShipX", playerShipPosition.x);
        output.putDouble("RelativeShipY", playerShipPosition.y);
        output.putDouble("RelativeShipZ", playerShipPosition.z);
    }
}
