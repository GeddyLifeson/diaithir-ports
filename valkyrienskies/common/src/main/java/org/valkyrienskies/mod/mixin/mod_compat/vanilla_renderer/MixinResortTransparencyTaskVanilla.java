package org.valkyrienskies.mod.mixin.mod_compat.vanilla_renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.chunk.TranslucencyPointOfView;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.mod.util.ShipTranslucencyHelper;

/**
 * Corrects translucent-face sorting for ship render sections, whose vertices live in shipyard space
 * but which vanilla sorts against the raw world camera (see {@link ShipTranslucencyHelper}).
 *
 * <p>{@code ResortTransparencyTask.doTask} runs on a chunk-build worker thread. It (1) builds the
 * sort order via {@code RenderSection.createVertexSorting(pos, camera)} and (2) decides whether the
 * rebuild is still needed via {@code TranslucencyPointOfView.of(camera, node)}. Both are fed the
 * world camera; for a ship section that makes the sort wrong and the point-of-view gate stuck. We
 * substitute the ship-space camera in both.
 */
@Mixin(targets =
    "net.minecraft.client.renderer.chunk.SectionRenderDispatcher$RenderSection$ResortTransparencyTask")
public class MixinResortTransparencyTaskVanilla {

    @WrapOperation(
        method = "doTask",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection;"
                + "createVertexSorting(Lnet/minecraft/core/SectionPos;Lnet/minecraft/world/phys/Vec3;)"
                + "Lcom/mojang/blaze3d/vertex/VertexSorting;")
    )
    private VertexSorting vs$shipVertexSorting(final SectionRenderDispatcher.RenderSection section,
        final SectionPos pos, final Vec3 camera, final Operation<VertexSorting> original) {
        final Vec3 shipCamera = ShipTranslucencyHelper.shipSpaceCamera(section.getSectionNode(), camera);
        return original.call(section, pos, shipCamera != null ? shipCamera : camera);
    }

    @WrapOperation(
        method = "doTask",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/chunk/TranslucencyPointOfView;"
                + "of(Lnet/minecraft/world/phys/Vec3;J)"
                + "Lnet/minecraft/client/renderer/chunk/TranslucencyPointOfView;")
    )
    private TranslucencyPointOfView vs$shipTaskPointOfView(final Vec3 camera, final long node,
        final Operation<TranslucencyPointOfView> original) {
        final Vec3 shipCamera = ShipTranslucencyHelper.shipSpaceCamera(node, camera);
        return original.call(shipCamera != null ? shipCamera : camera, node);
    }
}
