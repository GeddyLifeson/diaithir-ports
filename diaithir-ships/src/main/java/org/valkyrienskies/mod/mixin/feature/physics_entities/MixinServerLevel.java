package org.valkyrienskies.mod.mixin.feature.physics_entities;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.api.EntityPhysicsListener;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

@Mixin(targets = "net.minecraft.server.level.ServerLevel$EntityCallbacks")
public class MixinServerLevel {

    @Shadow
    @Final
    ServerLevel this$0;

    @Inject(method = "onTickingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
    private void postAddEntity(Entity entity, CallbackInfo ci) {
        if (entity instanceof EntityPhysicsListener listener) {
            String dimid = VSGameUtilsKt.getDimensionId(this.this$0);
            ValkyrienSkiesMod.INSTANCE.addEntityPhysTicker(dimid, entity);
        }
    }

    @Inject(method = "onTickingEnd(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
    private void postRemoveEntity(Entity entity, CallbackInfo ci) {
        if (entity instanceof EntityPhysicsListener listener) {
            String dimid = VSGameUtilsKt.getDimensionId(this.this$0);
            ValkyrienSkiesMod.INSTANCE.removeEntityPhysTicker(entity, dimid);
        }
    }
}
