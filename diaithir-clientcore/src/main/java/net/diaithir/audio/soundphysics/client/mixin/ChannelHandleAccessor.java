// Ported from Sound Physics Perfected (MIT) https://github.com/FalseMSP/SoundPhysicsPerfected
// Modifications: renamed from SourceManagerAccessor, prefixed accessor method.
package net.diaithir.audio.soundphysics.client.mixin;

import com.mojang.blaze3d.audio.Channel;
import net.minecraft.client.sounds.ChannelAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChannelAccess.ChannelHandle.class)
public interface ChannelHandleAccessor {
    @Accessor("channel")
    Channel spp$getChannel();
}
