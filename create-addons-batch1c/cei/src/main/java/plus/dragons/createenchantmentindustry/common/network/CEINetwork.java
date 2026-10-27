/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createenchantmentindustry.common.network;

import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import plus.dragons.createenchantmentindustry.common.CEICommon;

/** Fabric play channels used by CEI. */
public final class CEINetwork {
    public static final Identifier DATA_MAP_SYNC = CEICommon.asResource("data_map_sync");
    public static final Identifier CONFIG_SYNC = CEICommon.asResource("config_sync");
    public static final CustomPacketPayload.Type<DataMapSyncPayload> DATA_MAP_SYNC_TYPE = new CustomPacketPayload.Type<>(DATA_MAP_SYNC);
    public static final StreamCodec<RegistryFriendlyByteBuf, DataMapSyncPayload> DATA_MAP_SYNC_CODEC = StreamCodec.of(
            (buffer, payload) -> CEIDataMapSyncPacket.encode(payload.packet(), buffer),
            buffer -> new DataMapSyncPayload(CEIDataMapSyncPacket.decode(buffer)));
    public static final CustomPacketPayload.Type<ConfigSyncPayload> CONFIG_SYNC_TYPE = new CustomPacketPayload.Type<>(CONFIG_SYNC);
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPayload> CONFIG_SYNC_CODEC = StreamCodec.of(
            (buffer, payload) -> CEIConfigSyncPacket.encode(payload.packet(), buffer),
            buffer -> new ConfigSyncPayload(CEIConfigSyncPacket.decode(buffer)));

    private CEINetwork() {}

    public static void register(net.neoforged.bus.api.IEventBus modBus) {
        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> {
            var registrar = event.registrar("1").optional();
            registrar.playToClient(DATA_MAP_SYNC_TYPE, DATA_MAP_SYNC_CODEC,
                    (payload, ctx) -> ctx.enqueueWork(() -> CEIDataMaps.applyClientSnapshot(payload.packet().values())));
            registrar.playToClient(CONFIG_SYNC_TYPE, CONFIG_SYNC_CODEC,
                    (payload, ctx) -> ctx.enqueueWork(() -> CEIConfig.applyServerSnapshot(payload.packet().snapshot())));
        });
    }

    public static void sendStateSnapshot(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new DataMapSyncPayload(CEIDataMapSyncPacket.create()));
        PacketDistributor.sendToPlayer(player, new ConfigSyncPayload(CEIConfigSyncPacket.create()));
    }

    public record DataMapSyncPayload(CEIDataMapSyncPacket packet) implements CustomPacketPayload {
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return DATA_MAP_SYNC_TYPE;
        }
    }

    public record ConfigSyncPayload(CEIConfigSyncPacket packet) implements CustomPacketPayload {
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return CONFIG_SYNC_TYPE;
        }
    }
}
