package com.wildraces.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Server → client packet that syncs the local player's race. */
public record RacePacket(String raceName) implements CustomPacketPayload {

    public static final Type<RacePacket> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath("wildraces", "race_sync"));

    public static final StreamCodec<FriendlyByteBuf, RacePacket> CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            RacePacket::raceName,
            RacePacket::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
