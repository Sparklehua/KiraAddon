package org.agmas.kiraaddon.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.agmas.kiraaddon.KiraAddon;

public record RecallSheerHeartPacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RecallSheerHeartPacket> ID = new CustomPacketPayload.Type<>(KiraAddon.id("recall_sheer_heart"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RecallSheerHeartPacket> CODEC = StreamCodec.of(
        (packet, buf) -> {},
        buf -> new RecallSheerHeartPacket()
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}