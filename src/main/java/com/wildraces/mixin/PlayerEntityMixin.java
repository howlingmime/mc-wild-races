package com.wildraces.mixin;

import com.wildraces.PlayerRaceAccess;
import com.wildraces.race.Race;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerEntityMixin implements PlayerRaceAccess {

    @Unique private Race  wildRaces$race        = Race.NONE;
    @Unique private float wildRaces$spriteMeter = 1.0f;

    // ── PlayerRaceAccess ────────────────────────────────────────────────────

    @Override public Race  wildraces$getRace()              { return wildRaces$race; }
    @Override public void  wildraces$setRace(Race r)        { this.wildRaces$race = r; }
    @Override public float wildraces$getSpriteMeter()       { return wildRaces$spriteMeter; }
    @Override public void  wildraces$setSpriteMeter(float m){ this.wildRaces$spriteMeter = m; }

    // ── NBT persistence ─────────────────────────────────────────────────────

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readRace(ValueInput input, CallbackInfo ci) {
        input.getString("WildRacesRace").ifPresent(name -> {
            try { wildRaces$race = Race.valueOf(name); }
            catch (IllegalArgumentException ignored) { wildRaces$race = Race.NONE; }
        });
        input.getString("WildRacesSpriteMeter").ifPresent(s -> {
            try { wildRaces$spriteMeter = Float.parseFloat(s); }
            catch (NumberFormatException ignored) {}
        });
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeRace(ValueOutput output, CallbackInfo ci) {
        output.putString("WildRacesRace", wildRaces$race.name());
        output.putString("WildRacesSpriteMeter", String.valueOf(wildRaces$spriteMeter));
    }

    // ── Melee: Arachnid web + Minotaur sprint charge ─────────────────────────

    @Inject(method = "attack", at = @At("TAIL"))
    private void onMeleeAttack(Entity target, CallbackInfo ci) {
        if (!(target instanceof LivingEntity living)) return;
        Player self = (Player)(Object)this;

        switch (wildRaces$race) {
            case ARACHNID ->
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1, false, true));
            case MINOTAUR -> {
                if (self.isSprinting()) {
                    double yawRad = self.getYRot() * (Math.PI / 180.0);
                    living.knockback(3.0, Math.sin(yawRad), -Math.cos(yawRad));
                    living.hurt(self.damageSources().playerAttack(self), 6.0f);
                }
            }
            default -> {}
        }
    }
}
