package com.wildraces.mixin;

import com.wildraces.PlayerRaceAccess;
import com.wildraces.race.Race;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin {

    // Drain/charge rates for the Sprite wing meter.
    // Full drain (1.0 → 0.0) in 200 ticks = 10 seconds.
    // Full charge (0.0 → 1.0) in 1200 ticks = 60 seconds (10% per 6 sec = 0.1/120).
    private static final float DRAIN_RATE  = 1.0f / 200f;
    private static final float CHARGE_RATE = 0.1f / 120f;

    @Inject(method = "tick", at = @At("TAIL"))
    private void applyRacePassives(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer)(Object)this;
        Race race = ((PlayerRaceAccess) player).wildraces$getRace();

        // Per-tick: Sprite wing meter management
        if (race == Race.SPRITE) {
            manageSpriteWings(player);
        }

        // Every 5 sec: lingering status effects
        if (player.tickCount % 100 != 0) return;
        switch (race) {
            case TROLL ->
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 0, false, false));
            case LIONBEAR ->
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, false, false));
            case CENTAUR ->
                player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 300, 0, false, false));
            default -> {}
        }
    }

    // ── Sprite wing meter ────────────────────────────────────────────────────

    private static void manageSpriteWings(ServerPlayer player) {
        PlayerRaceAccess access = (PlayerRaceAccess) player;
        Abilities abilities = player.getAbilities();
        float meter = access.wildraces$getSpriteMeter();

        if (abilities.flying) {
            // Drain while airborne
            meter = Math.max(0f, meter - DRAIN_RATE);
            access.wildraces$setSpriteMeter(meter);

            if (meter <= 0f) {
                abilities.flying = false;
                abilities.mayfly = false;
                player.onUpdateAbilities();
                player.sendSystemMessage(
                    Component.literal("§c✦ Wings exhausted — recharging (10% per 6 sec)"), true);
            } else if (player.tickCount % 20 == 0) {
                // Update HUD once per second while flying
                player.sendSystemMessage(buildHud(meter, true), true);
            }

        } else {
            // Recharge while grounded / gliding / not flying
            if (meter < 1.0f) {
                float prev = meter;
                meter = Math.min(1.0f, meter + CHARGE_RATE);
                access.wildraces$setSpriteMeter(meter);

                // Re-enable mayfly once there's enough charge to leave the ground
                if (!abilities.mayfly && meter > 0.05f) {
                    abilities.mayfly = true;
                    player.onUpdateAbilities();
                }

                // Show recharge progress at each 10% milestone
                int prevPct = (int)(prev * 10);
                int newPct  = (int)(meter * 10);
                if (newPct > prevPct || meter >= 1.0f) {
                    String suffix = (meter >= 1.0f) ? " §b— Ready!" : "";
                    player.sendSystemMessage(buildHud(meter, false).copy()
                        .append(Component.literal(suffix)), true);
                }
            }
        }
    }

    /** Builds an action-bar HUD component: ✦ [▓▓▓▓▓░░░░░] 50% */
    private static Component buildHud(float meter, boolean flying) {
        int filled = Math.round(meter * 10);
        String bar = "§b" + "▓".repeat(filled) + "§8" + "░".repeat(10 - filled);
        int pct = Math.round(meter * 100);
        String label = flying ? " §7flying" : " §7recharging";
        return Component.literal("§b✦ [" + bar + "§b] §f" + pct + "%" + label);
    }
}
