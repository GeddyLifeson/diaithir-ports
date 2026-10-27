package com.otectus.immersivetanning.client;

import com.otectus.immersivetanning.network.packet.CloseSessionPacket;
import com.otectus.immersivetanning.network.packet.OpenBenchPacket;
import com.otectus.immersivetanning.network.packet.OpenFinishPacket;
import com.otectus.immersivetanning.network.packet.OpenFleshingPacket;
import com.otectus.immersivetanning.network.packet.SessionResultPacket;
import com.otectus.immersivetanning.network.packet.StationEffectPacket;
import com.otectus.immersivetanning.network.packet.StationNoticePacket;
import com.otectus.immersivetanning.network.packet.StitchResultPacket;
import com.otectus.immersivetanning.network.packet.StitchStartPacket;

/** Phase 1 stub: the screens and effects arrive with the client port. */
public final class ClientPacketHandler {
    public static void openFleshing(OpenFleshingPacket packet) {}
    public static void openFinish(OpenFinishPacket packet) {}
    public static void openBench(OpenBenchPacket packet) {}
    public static void stitchStart(StitchStartPacket packet) {}
    public static void stitchResult(StitchResultPacket packet) {}
    public static void sessionResult(SessionResultPacket packet) {}
    public static void closeSession(CloseSessionPacket packet) {}
    public static void stationNotice(StationNoticePacket packet) {}
    public static void stationEffect(StationEffectPacket packet) {}

    private ClientPacketHandler() {}
}
