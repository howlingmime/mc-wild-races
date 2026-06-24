package com.wildraces;

import com.wildraces.command.RaceCommand;
import com.wildraces.race.Race;
import com.wildraces.race.RaceManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WildRacesMod implements ModInitializer {

    public static final String MOD_ID = "wildraces";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        RaceCommand.register();

        // On join: re-apply attribute modifiers (transient, not saved with entity)
        // and prompt new players who haven't chosen a race.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.player;
            Race race = ((PlayerRaceAccess) player).wildraces$getRace();
            if (race == Race.NONE) {
                player.sendSystemMessage(Component.literal("§6Welcome! You haven't chosen a race yet."));
                player.sendSystemMessage(Component.literal("§7Use §e/race list §7to see options, then §e/race set <race>§7."));
                player.sendSystemMessage(Component.literal("§7Races: §aarachnid§7, §aminotaur§7, §alionbear§7, §atroll§7, §acentaur§7."));
            } else {
                RaceManager.applyRaceModifiers(player, race);
            }
        });

        // After respawn: race is re-read from NBT on the new player entity; re-apply modifiers.
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            Race race = ((PlayerRaceAccess) newPlayer).wildraces$getRace();
            if (race != Race.NONE) {
                RaceManager.applyRaceModifiers(newPlayer, race);
            }
        });

        LOGGER.info("Wild Races loaded — 5 races ready.");
    }
}
