// Ported from Packet Fixer (MIT) https://github.com/TonimatasDEV/PacketFixer
// Modifications: Uses PacketFixerSettings instead of upstream Config.
package net.diaithir.networkcompat.packetfixer.util;

public final class Messages {
    private Messages() {}

    public static String getPayloadMessage() {
        return "Payload may not be larger than " + PacketFixerSettings.getPacketSize() + " bytes. You can modify it in the Packet Fixer config.";
    }
}
