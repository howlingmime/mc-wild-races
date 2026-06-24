package com.wildraces.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.wildraces.PlayerRaceAccess;
import com.wildraces.race.Race;
import com.wildraces.race.RaceManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.stream.Collectors;

import net.minecraft.commands.Commands;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class RaceCommand {

    private static final SuggestionProvider<CommandSourceStack> RACE_SUGGESTIONS =
        (ctx, builder) -> {
            Arrays.stream(Race.values())
                .filter(r -> r != Race.NONE)
                .map(r -> r.name().toLowerCase())
                .forEach(builder::suggest);
            return builder.buildFuture();
        };

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(literal("race")
                .executes(ctx -> showInfo(ctx.getSource()))
                .then(literal("list")
                    .executes(ctx -> listRaces(ctx.getSource())))
                .then(literal("info")
                    .executes(ctx -> showInfo(ctx.getSource())))
                .then(literal("set")
                    .then(argument("race", StringArgumentType.word())
                        .suggests(RACE_SUGGESTIONS)
                        .executes(ctx -> setRace(
                            ctx.getSource(),
                            StringArgumentType.getString(ctx, "race")))))
                .then(literal("reset")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .executes(ctx -> setRace(ctx.getSource(), "none")))
            )
        );
    }

    private static int setRace(CommandSourceStack source, String raceName) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("This command can only be used by players."));
            return 0;
        }
        Race race;
        try {
            race = Race.valueOf(raceName.toUpperCase());
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal(
                "Unknown race: §e" + raceName + "§c. Use §e/race list§c to see options."));
            return 0;
        }
        RaceManager.setRace(player, race);
        return 1;
    }

    private static int showInfo(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        Race race = ((PlayerRaceAccess) player).wildraces$getRace();
        if (race == Race.NONE) {
            source.sendSuccess(() -> Component.literal(
                "§7You haven't chosen a race yet. Use §e/race set <race>§7."), false);
        } else {
            source.sendSuccess(() -> Component.literal(
                "§6Race: §e" + race.displayName + "\n§7" + race.description), false);
        }
        return 1;
    }

    private static int listRaces(CommandSourceStack source) {
        String list = Arrays.stream(Race.values())
            .filter(r -> r != Race.NONE)
            .map(r -> "§e" + r.name().toLowerCase() + "§7 — " + r.displayName + ": " + r.description)
            .collect(Collectors.joining("\n"));
        source.sendSuccess(() -> Component.literal("§6Available races:\n" + list), false);
        return 1;
    }
}
