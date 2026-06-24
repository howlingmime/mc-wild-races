package com.wildraces.mixin;

import com.wildraces.PlayerRaceAccess;
import com.wildraces.race.Race;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void applyRacePassives(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer)(Object)this;
        if (player.tickCount % 100 != 0) return;

        Race race = ((PlayerRaceAccess) player).wildraces$getRace();
        switch (race) {
            case TROLL ->
                // Regeneration passive — fire weakness is handled in LivingEntityMixin.hurtServer
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 0, false, false));
            case LIONBEAR ->
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, false, false));
            case CENTAUR ->
                player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 300, 0, false, false));
            default -> {}
        }
    }
}
