package com.wildraces.client;

import com.wildraces.PlayerRaceAccess;
import com.wildraces.client.render.RaceOverlayModels;
import com.wildraces.network.RacePacket;
import com.wildraces.race.Race;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry.TexturedLayerDefinitionProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WildRacesClient implements ClientModInitializer {

    /**
     * Maps entity ID → Race for client-side rendering (skin tints, model overlays).
     * Populated when a RacePacket arrives from the server.
     */
    public static final Map<Integer, Race> RACE_BY_ENTITY_ID = new ConcurrentHashMap<>();

    @Override
    public void onInitializeClient() {
        // ── Register all the race-overlay model layers ──────────────────────
        // Each race has up to two attachment points (head & body). Empty layers
        // are registered for races that have no parts at a given attachment so
        // bakeLayer() never fails in the renderer mixin.

        registerHead(Race.MINOTAUR, RaceOverlayModels::minotaurHead);
        registerHead(Race.LIONBEAR, RaceOverlayModels::lionbearHead);
        registerHead(Race.TROLL,    RaceOverlayModels::trollHead);
        registerHead(Race.ARACHNID, RaceOverlayModels::empty);
        registerHead(Race.SPRITE,   RaceOverlayModels::empty);
        registerHead(Race.CENTAUR,  RaceOverlayModels::empty);

        registerBody(Race.ARACHNID, RaceOverlayModels::arachnidBody);
        registerBody(Race.LIONBEAR, RaceOverlayModels::lionbearBody);
        registerBody(Race.SPRITE,   RaceOverlayModels::spriteBody);
        registerBody(Race.CENTAUR,  RaceOverlayModels::centaurBody);
        registerBody(Race.MINOTAUR, RaceOverlayModels::empty);
        registerBody(Race.TROLL,    RaceOverlayModels::empty);

        // ── Race sync packet handler ────────────────────────────────────────
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
                ((PlayerRaceAccess) player).wildraces$setRace(finalRace);
                RACE_BY_ENTITY_ID.put(player.getId(), finalRace);
            });
        });
    }

    private static void registerHead(Race race, TexturedLayerDefinitionProvider provider) {
        ModelLayerRegistry.registerModelLayer(
            RaceOverlayModels.HEAD_LAYERS.get(race), provider);
    }

    private static void registerBody(Race race, TexturedLayerDefinitionProvider provider) {
        ModelLayerRegistry.registerModelLayer(
            RaceOverlayModels.BODY_LAYERS.get(race), provider);
    }
}
