package org.valkyrienskies.mod.mixin.feature.ship_debug_overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugEntryLookingAt;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

/**
 * Adds information about the targeted ship to the "looking at block" part of the debug screen.
 */
@Mixin(DebugEntryLookingAt.class)
public abstract class MixinDebugScreenOverlay {
    @Shadow
    public abstract HitResult getHitResult(Entity cameraEntity);

    @Shadow
    public abstract Identifier group();

    @Inject(method = "display", at = @At("TAIL"))
    private void addShipInformation(final DebugScreenDisplayer displayer, final Level serverOrClientLevel,
        final LevelChunk clientChunk, final LevelChunk serverChunk, final CallbackInfo ci) {
        if (!DebugEntryLookingAt.BLOCK_GROUP.equals(this.group())) {
            return;
        }
        final Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        final Level l = serverOrClientLevel != null ? serverOrClientLevel : Minecraft.getInstance().level;
        if (cameraEntity == null || l == null) {
            return;
        }
        final HitResult block = this.getHitResult(cameraEntity);
        if (!(block instanceof BlockHitResult blockHitResult) || block.getType() != HitResult.Type.BLOCK) {
            return;
        }
        final BlockPos blockPos = blockHitResult.getBlockPos();
        final Ship ship = VSGameUtilsKt.getShipManagingPos(l, blockPos);
        final LoadedServerShip lsship =
            l instanceof ServerLevel ? VSGameUtilsKt.getLoadedShipManagingPos((ServerLevel) l, blockPos) : null;
        if (ship != null) {
            final List<String> list = new ArrayList<>();
            list.add("");
            list.add(ChatFormatting.UNDERLINE + "Targeted Ship: " + ship.getSlug());
            if (lsship != null) {
                // Info about physical properties is only visible in singleplayer or while hosting an integrated server.
                list.add("Static: " + lsship.isStatic());
                list.add("Mass: " + lsship.getInertiaData().getMass() + " kg");
                list.add("");
            }
            final Vector3dc scale = ship.getTransform().getShipToWorldScaling();
            list.add(String.format(
                Locale.ROOT,
                "Ship Scale: %.3f / %.3f / %.3f",
                scale.x(),
                scale.y(),
                scale.z()
            ));
            final Vector3dc linVel = ship.getVelocity();
            list.add(String.format(
                Locale.ROOT,
                "Linear Velocity: %.3f / %.3f / %.3f, total: %.3f m/s",
                linVel.x(),
                linVel.y(),
                linVel.z(),
                linVel.length()
            ));
            final Vector3dc angVel = ship.getOmega();
            list.add(String.format(
                Locale.ROOT,
                "Angular Velocity: %.3f / %.3f / %.3f",
                angVel.x(),
                angVel.y(),
                angVel.z()
            ));
            final Vec3 pos = VSGameUtilsKt.toWorldCoordinates(ship, blockPos.getCenter());
            list.add(String.format(
                Locale.ROOT,
                "Targeted World Position: %.3f / %.3f / %.3f",
                pos.x(),
                pos.y(),
                pos.z()
            ));
            displayer.addToGroup(DebugEntryLookingAt.BLOCK_GROUP, list);
        }
    }
}
