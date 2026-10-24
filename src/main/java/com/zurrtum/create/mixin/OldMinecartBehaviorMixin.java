package com.zurrtum.create.mixin;

import com.zurrtum.create.foundation.block.MinecartPassBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OldMinecartBehavior.class)
public abstract class OldMinecartBehaviorMixin extends MinecartBehavior {
    protected OldMinecartBehaviorMixin(AbstractMinecart minecart) {
        super(minecart);
    }

    // Anchored at HEAD (not at a call inside the method) so other mods that also rewrite moveAlongTrack, such as Railcraft, cannot break the injection.
    @Inject(method = "moveAlongTrack(Lnet/minecraft/server/level/ServerLevel;)V", at = @At("HEAD"))
    private void onMinecartPass(ServerLevel world, CallbackInfo ci) {
        BlockPos pos = this.minecart.getCurrentBlockPosOrRailBelow();
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof MinecartPassBlock block) {
            block.onMinecartPass(state, world, pos, this.minecart);
        }
    }
}
