// Ported from Packet Fixer (MIT) https://github.com/TonimatasDEV/PacketFixer
// Modifications: Unchanged logic; moved into the feature's mixin package.
package net.diaithir.networkcompat.packetfixer.mixins;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import net.minecraft.network.VarInt;
import net.minecraft.network.Varint21FrameDecoder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Varint21FrameDecoder.class)
public abstract class Varint21FrameDecoderMixin {
    @Mutable @Shadow @Final private ByteBuf helperBuf;

    @Inject(method = "decode", at = @At("HEAD"))
    private void packetfixer$checkSize(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list, CallbackInfo ci) {
        this.helperBuf = Unpooled.directBuffer(VarInt.getByteSize(byteBuf.readableBytes()) + 2);
    }

    @ModifyConstant(method = "copyVarint", constant = @Constant(intValue = 3))
    private static int packetfixer$newSize(int value, ByteBuf byteBuf, ByteBuf byteBuf1) {
        return VarInt.getByteSize(byteBuf1.readableBytes()) + 2;
    }
}
