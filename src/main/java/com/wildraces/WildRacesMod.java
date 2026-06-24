package com.wildraces;

import com.wildraces.command.RaceCommand;
import com.wildraces.network.RacePacket;
import com.wildraces.race.Race;
import com.wildraces.race.RaceManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WildRacesMod implements ModInitializer {

    public static final String MOD_ID = "wildraces";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Register the S→C race-sync packet so clients know which race they are.
        // onClimbable() and other client physics checks read the race from the
        // ClientPlayer, which is only populated via this packet (not from NBT).
        PayloadTypeRegistry.clientboundPlay().register(RacePacket.TYPE, RacePacket.CODEC);

        RaceCommand.register();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.player;
            Race race = ((PlayerRaceAccess) player).wildraces$getRace();
            if (race == Race.NONE) {
                player.sendSystemMessage(Component.literal("§6Welcome! You haven't chosen a race yet."));
                player.sendSystemMessage(Component.literal("§7Use §e/race list §7to see options, then §e/race set <race>§7."));
                player.sendSystemMessage(Component.literal("§7Races: §aarachnid§7, §aminotaur§7, §alionbear§7, §atroll§7, §acentaur§7."));
            } else {
                RaceManager.applyRaceModifiers(player, race);
                // Sync race to the client so client-side physics (climbing etc.) work.
                ServerPlayNetworking.send(player, new RacePacket(race.name()));
            }
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            Race race = ((PlayerRaceAccess) newPlayer).wildraces$getRace();
            if (race != Race.NONE) {
                RaceManager.applyRaceModifiers(newPlayer, race);
                ServerPlayNetworking.send(newPlayer, new RacePacket(race.name()));
            }
        });

        LOGGER.info("Wild Races loaded — 5 races ready.");
    }
}
