package org.valkyrienskies.mod.mixin.server.network;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Collections;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.internal.world.VsiServerShipWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.config.VSGameConfig;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.mod.mixinducks.world.entity.PlayerDuck;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerImpl extends ServerCommonPacketListenerImpl {
    public MixinServerGamePacketListenerImpl(MinecraftServer minecraftServer, Connection connection,
        CommonListenerCookie commonListenerCookie) {
        super(minecraftServer, connection, commonListenerCookie);
    }

    @Shadow
    public ServerPlayer player;

    @Shadow
    private int awaitingTeleport;

    @Shadow
    private int tickCount;

    @Shadow
    private Vec3 awaitingPositionFromClient;

    @Shadow
    private int awaitingTeleportTime;

    @ModifyExpressionValue(
        at = @At(value = "FIELD",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;aboveGroundTickCount:I", ordinal = 0),
        method = "tickPlayer"
    )
    private int noFlyKick(final int original) {
        if (VSGameConfig.SERVER.getEnableMovementChecks()) {
            return original;
        } else {
            return 0;
        }
    }

    @WrapOperation(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/Vec3;subtract(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"
        ),
        method = "handleUseItemOn"
    )
    private Vec3 skipDistanceCheck2(final Vec3 instance, final Vec3 vec3, final Operation<Vec3> subtract) {
        return VSGameUtilsKt.toWorldCoordinates(player.level(), subtract.call(instance, vec3));
    }

    /*
    @WrapOperation(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/ChunkPos;getChessboardDistance(Lnet/minecraft/world/level/ChunkPos;)I"
        ),
        method = "handleUseItemOn"
    )
    private int skipDistanceCheck(final ChunkPos instance, final ChunkPos chunkPos, final Operation<Integer> getChessboardDistance) {
        return 0;
    }

     */

    @WrapOperation(
        method = "shouldCheckPlayerMovement",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;isSingleplayerOwner()Z"
        )
    )
    private boolean shouldSkipMoveCheck1(final ServerGamePacketListenerImpl instance,
        final Operation<Boolean> isSinglePlayerOwner) {
        return !VSGameConfig.SERVER.getEnableMovementChecks();
    }

    @WrapOperation(
        method = "handleMoveVehicle",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;isSingleplayerOwner()Z"
        ),
        require = 0
    )
    private boolean shouldSkipMoveCheck2(final ServerGamePacketListenerImpl instance,
        final Operation<Boolean> isSinglePlayerOwner) {
        return !VSGameConfig.SERVER.getEnableMovementChecks();
    }

    @WrapOperation(
        method = "handleMovePlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;isCreative()Z"
        ),
        require = 0
    )
    private boolean shouldSkipMoveCheck(final ServerPlayer instance,
        final Operation<Boolean> isSinglePlayerOwner) {
        return !VSGameConfig.SERVER.getEnableMovementChecks();
    }

    // Fixes:
    // https://github.com/ValkyrienSkies/Valkyrien-Skies-2/issues/87
    // Bed Bug
    @Shadow
    public abstract void teleport(PositionMoveRotation destination, Set<Relative> relatives);

    @Inject(
        method = "teleport(Lnet/minecraft/world/entity/PositionMoveRotation;Ljava/util/Set;)V",
        at = @At(value = "HEAD"),
        cancellable = true
    )
    private void transformTeleport(final PositionMoveRotation destination, final Set<Relative> relatives,
        final CallbackInfo ci) {

        if (!VSGameConfig.SERVER.getTransformTeleports()) {
            return;
        }
        if (relatives.contains(Relative.X) || relatives.contains(Relative.Y) || relatives.contains(Relative.Z)) {
            return;
        }

        final Vec3 position = destination.position();
        final BlockPos blockPos = BlockPos.containing(position.x, position.y, position.z);
        final ServerShip ship = VSGameUtilsKt.getShipManagingPos((ServerLevel) player.level(), blockPos);

        // TODO add flag to disable this https://github.com/ValkyrienSkies/Valkyrien-Skies-2/issues/30
        if (ship != null) {
            final Vector3d pos = new Vector3d(position.x, position.y, position.z);
            ship.getShipToWorld().transformPosition(pos);

            ci.cancel();
            this.teleport(
                new PositionMoveRotation(VectorConversionsMCKt.toMinecraft(pos), destination.deltaMovement(),
                    destination.yRot(), destination.xRot()),
                relatives
            );
        }
    }

    @Inject(
        method = "onDisconnect",
        at = @At("HEAD")
    )
    void onDisconnect(DisconnectionDetails disconnectionDetails, CallbackInfo ci) {
        final VsiServerShipWorld world = VSGameUtilsKt.getShipObjectWorld(this.server);
        if (world != null) {
            world.onDisconnect(VSGameUtilsKt.getPlayerWrapper(this.player));
        }
    }

    @Inject(
        method = "handleMovePlayer",
        at = @At("TAIL")
    )
    void afterHandleMovePlayer(final ServerboundMovePlayerPacket packet, final CallbackInfo ci) {
        if (this.player instanceof final PlayerDuck duck) {
            duck.vs_setHandledMovePacket(true);
            if (duck.vs_getQueuedPositionUpdate() != null) {
                this.player.setPos(duck.vs_getQueuedPositionUpdate());
                duck.vs_setQueuedPositionUpdate(null);
            }
        }
    }

}
