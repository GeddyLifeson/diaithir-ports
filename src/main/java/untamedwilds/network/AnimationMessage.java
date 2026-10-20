package untamedwilds.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import untamedwilds.UntamedWilds;
import untamedwilds.citadel.animation.IAnimatedEntity;

/**
 * Tells clients which animation an entity is playing. Replaces Citadel's AnimationMessage
 * (Citadel's networking is not vendored).
 */
public record AnimationMessage(int entityId, int index) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<AnimationMessage> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(UntamedWilds.MOD_ID, "animation"));

    public static final StreamCodec<ByteBuf, AnimationMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        AnimationMessage::entityId,
        ByteBufCodecs.VAR_INT,
        AnimationMessage::index,
        AnimationMessage::new
    );

    public static void handleClient(AnimationMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(message.entityId);
            if (entity instanceof IAnimatedEntity animated) {
                if (message.index < 0) {
                    animated.setAnimation(IAnimatedEntity.NO_ANIMATION);
                } else {
                    animated.setAnimation(animated.getAnimations()[message.index]);
                }
                animated.setAnimationTick(0);
            }
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
