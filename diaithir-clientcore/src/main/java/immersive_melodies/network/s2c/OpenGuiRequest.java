package immersive_melodies.network.s2c;

import immersive_melodies.Common;
import immersive_melodies.network.ImmersivePayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

public record OpenGuiRequest() implements ImmersivePayload {
    public static final Type<OpenGuiRequest> TYPE = new OpenGuiRequest.Type<>(Common.locate("open_gui_request"));
    public static final StreamCodec<FriendlyByteBuf, OpenGuiRequest> STREAM_CODEC = StreamCodec.of(
            (b, c) -> {
            },
            (b) -> new OpenGuiRequest()
    );


    @Override
    public void handle(Player e) {
        Common.networkManager.handleOpenGuiRequest(this);
    }

    @Override
    public Type<? extends ImmersivePayload> type() {
        return TYPE;
    }
}
