package com.wildraces.client;

import com.wildraces.PlayerRaceAccess;
import com.wildraces.network.RacePacket;
import com.wildraces.race.Race;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WildRacesClient implements ClientModInitializer {

    /**
     * Maps entity ID → Race for use in client-side rendering (skin tints, model overlays).
     * Populated when a RacePacket arrives from the server.
     */
    public static final Map<Integer, Race> RACE_BY_ENTITY_ID = new ConcurrentHashMap<>();

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(RacePacket.TYPE, (payload, context) -> {
            Race race;
            try {
                race = Race.valueOf(payload.raceName());
            } catch (IllegalArgumentException e) {
                race = Race.NONE;
            }
            final Race finalRace = race;
            context.client().execute(() -> {
                var player = context.client().player;
                if (player == null) return;
                // Update the mixin-injected race field so client-side physics
                // (onClimbable, travel, maxUpStep) use the correct race.
                ((PlayerRaceAccess) player).wildraces$setRace(finalRace);
                // Also store by entity ID for the skin-tint renderer.
                RACE_BY_ENTITY_ID.put(player.getId(), finalRace);
            });
        });
    }
}
