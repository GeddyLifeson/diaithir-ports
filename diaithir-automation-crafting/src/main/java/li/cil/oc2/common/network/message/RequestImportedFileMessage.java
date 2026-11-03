/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.network.message;

import li.cil.oc2.common.network.Network;
import net.minecraft.network.chat.MutableComponent;

import io.netty.buffer.ByteBuf;
import li.cil.oc2.api.API;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static li.cil.oc2.common.util.TranslationUtils.text;

public record RequestImportedFileMessage(int id) implements AbstractMessage {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final MutableComponent FILE_TOO_LARGE_TEXT = text("message.{mod}.import_file.file_too_large");

    ///////////////////////////////////////////////////////////////////

    public static final StreamCodec<ByteBuf, RequestImportedFileMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        RequestImportedFileMessage::id,
        RequestImportedFileMessage::new
    );

    public static final CustomPacketPayload.Type<RequestImportedFileMessage> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(API.MOD_ID, "request_imported_file_message"));

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    ///////////////////////////////////////////////////////////////////

    public void handleMessage(IPayloadContext context) {
        // TODO(oc2-restore): FileChooserScreen is part of li.cil.oc2.client.gui which is excluded from this
        //  compile stage. Until it is restored, the import request is simply canceled.
        Network.sendToServer(new ClientCanceledImportFileMessage(id));
    }
}
