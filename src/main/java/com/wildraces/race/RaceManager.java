package com.wildraces.race;

import com.wildraces.PlayerRaceAccess;
import com.wildraces.network.RacePacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class RaceManager {

    private static final Identifier HEALTH_ID = Identifier.fromNamespaceAndPath("wildraces", "race_health");
    private static final Identifier DAMAGE_ID = Identifier.fromNamespaceAndPath("wildraces", "race_damage");
    private static final Identifier SPEED_ID  = Identifier.fromNamespaceAndPath("wildraces", "race_speed");

    public static void setRace(ServerPlayer player, Race race) {
        removeRaceModifiers(player);
        ((PlayerRaceAccess) player).wildraces$setRace(race);
        applyRaceModifiers(player, race);

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }

        // Sync to client so client-side physics (climbing, step height) use the correct race.
        ServerPlayNetworking.send(player, new RacePacket(race.name()));

        if (race == Race.NONE) {
            player.sendSystemMessage(Component.literal("You have returned to your human form."));
        } else {
            player.sendSystemMessage(Component.literal("§6You are now a §e" + race.displayName + "§6!"));
            player.sendSystemMessage(Component.literal("§7" + race.description));
        }
    }

    public static void applyRaceModifiers(Player player, Race race) {
        if (race.bonusHealth != 0)
            addModifier(player, Attributes.MAX_HEALTH, HEALTH_ID, race.bonusHealth);
        if (race.bonusDamage != 0)
            addModifier(player, Attributes.ATTACK_DAMAGE, DAMAGE_ID, race.bonusDamage);
        if (race.speedModifier != 0)
            addModifier(player, Attributes.MOVEMENT_SPEED, SPEED_ID, race.speedModifier);
    }

    public static void removeRaceModifiers(Player player) {
        removeModifier(player, Attributes.MAX_HEALTH, HEALTH_ID);
        removeModifier(player, Attributes.ATTACK_DAMAGE, DAMAGE_ID);
        removeModifier(player, Attributes.MOVEMENT_SPEED, SPEED_ID);
    }

    private static void addModifier(Player player,
            net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
            Identifier id, double value) {
        AttributeInstance inst = player.getAttribute(attribute);
        if (inst == null) return;
        inst.removeModifier(id);
        inst.addTransientModifier(
            new AttributeModifier(id, value, AttributeModifier.Operation.ADD_VALUE));
    }

    private static void removeModifier(Player player,
            net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
            Identifier id) {
        AttributeInstance inst = player.getAttribute(attribute);
        if (inst != null) inst.removeModifier(id);
    }
}
