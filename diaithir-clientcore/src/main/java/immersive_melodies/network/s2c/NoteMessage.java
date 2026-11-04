package immersive_melodies.network.s2c;

import immersive_melodies.Common;
import immersive_melodies.network.ImmersivePayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

public record NoteMessage(int entity, int tone, int velocity) implements ImmersivePayload {
    public static final Type<NoteMessage> TYPE = new NoteMessage.Type<>(Common.locate("note_message"));
    public static final StreamCodec<FriendlyByteBuf, NoteMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, NoteMessage::entity,
            ByteBufCodecs.INT, NoteMessage::tone,
            ByteBufCodecs.INT, NoteMessage::velocity,
            NoteMessage::new
    );

    @Override
    public void handle(Player e) {
        Common.networkManager.handleNoteMessage(this);
    }

    @Override
    public Type<? extends ImmersivePayload> type() {
        return TYPE;
    }
}