package org.valkyrienskies.mod.mixin.feature.ship_debug_overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugEntryPosition;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.world.ServerShipWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.EntityDraggingInformation;
import org.valkyrienskies.mod.common.util.IEntityDraggingInformationProvider;

/**
 * Adds the loaded ship count and the ship dragging the player to the position part of the debug screen.
 */
@Mixin(DebugEntryPosition.class)
public class MixinDebugEntryPosition {
    @Inject(method = "display", at = @At("TAIL"))
    private void addShipInformation(final DebugScreenDisplayer displayer, final Level serverOrClientLevel,
        final LevelChunk clientChunk, final LevelChunk serverChunk, final CallbackInfo ci) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (serverOrClientLevel instanceof ServerLevel serverLevel) {
            final ServerShipWorld world = VSGameUtilsKt.getShipObjectWorld(serverLevel);
            displayer.addToGroup(DebugEntryPosition.GROUP,
                "Ships Loaded: " + world.getLoadedShips().size() + "/" + world.getAllShips().size());
        }

        final Level level = serverOrClientLevel != null ? serverOrClientLevel : minecraft.level;
        if (level == null || minecraft.player == null) {
            return;
        }
        final EntityDraggingInformation info =
            ((IEntityDraggingInformationProvider) minecraft.player).getDraggingInformation();
        if (info != null && info.isEntityBeingDraggedByAShip()) {
            final Long shipId = info.getLastShipStoodOn();
            if (shipId != null) {
                final Ship ship = VSGameUtilsKt.getAllShips(level).getById(shipId);
                if (ship != null) {
                    displayer.addToGroup(DebugEntryPosition.GROUP, "Dragged by: " + ship.getSlug());
                }
            }
        }
    }
}
