package net.id.paradise_lost.mixin.client.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.id.paradise_lost.registry.BlockRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Uses the blue portal texture for the portal screen overlay when travelling through a Paradise Lost portal.
 */
@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @WrapOperation(
            method = "extractPortalOverlay",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/block/BlockStateModelSet;"
                            + "getParticleMaterial(Lnet/minecraft/world/level/block/state/BlockState;)"
                            + "Lnet/minecraft/client/resources/model/sprite/Material$Baked;"
            ),
            require = 0
    )
    private Material.Baked paradiseLost$bluePortalSprite(BlockStateModelSet set, BlockState state, Operation<Material.Baked> original) {
        if (paradiseLost$isBluePortal()) {
            return original.call(set, BlockRegistry.BLUE_PORTAL.get().defaultBlockState());
        }
        return original.call(set, state);
    }

    @Unique
    private boolean paradiseLost$isBluePortal() {
        if (this.minecraft.player == null) {
            return false;
        }
        if (this.minecraft.player.portalProcess != null) {
            return this.minecraft.player.portalProcess.isSamePortal(BlockRegistry.BLUE_PORTAL.get());
        }
        return this.minecraft.player.level()
                .getBlockState(this.minecraft.player.blockPosition())
                .is(BlockRegistry.BLUE_PORTAL.get());
    }
}
