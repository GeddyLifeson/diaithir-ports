package net.diaithir.furnishings;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client to server: the words the player wrote on one arm. */
public record SetArmText(BlockPos pos, int arm, String line1, String line2) implements CustomPacketPayload {
    public static final Type<SetArmText> TYPE = new Type<>(Identifier.fromNamespaceAndPath(DiaithirFurnishings.ID, "set_arm_text"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetArmText> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, SetArmText::pos,
        ByteBufCodecs.VAR_INT, SetArmText::arm,
        ByteBufCodecs.stringUtf8(64), SetArmText::line1,
        ByteBufCodecs.stringUtf8(64), SetArmText::line2,
        SetArmText::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, CODEC, SetArmText::handle);
    }

    private static void handle(SetArmText msg, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        if (!player.level().isLoaded(msg.pos) || player.distanceToSqr(msg.pos.getCenter()) > 64.0) {
            return;
        }
        BlockEntity be = player.level().getBlockEntity(msg.pos);
        if (be instanceof SignpostEntity post && post.arm(msg.arm) != null) {
            post.setArm(msg.arm, post.arm(msg.arm).withText(msg.line1, msg.line2));
        }
    }
}
