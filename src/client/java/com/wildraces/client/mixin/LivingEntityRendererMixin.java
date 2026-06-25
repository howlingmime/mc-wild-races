package com.wildraces.client.mixin;

import com.wildraces.client.WildRacesClient;
import com.wildraces.race.Race;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    /**
     * Apply a race-specific ARGB skin tint to the player model.
     * Only fires when the model would otherwise be white (no hurt flash active).
     * The tint is a multiplicative blend: 0xFFFFFFFF = no change, 0xFF88FF88 = green-ish, etc.
     */
    @Inject(method = "getModelTint", at = @At("RETURN"), cancellable = true)
    private void applyRaceSkinTint(LivingEntityRenderState state, CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValue() != -1) return; // -1 = 0xFFFFFFFF (white), skip if hurt flash
        if (!(state instanceof AvatarRenderState avatarState)) return;

        Race race = WildRacesClient.RACE_BY_ENTITY_ID.getOrDefault(avatarState.id, Race.NONE);
        int tint = switch (race) {
            case TROLL    -> 0xFF88FF88;  // green hue
            case MINOTAUR -> 0xFFFF9999;  // rusty red
            case LIONBEAR -> 0xFFFFCC66;  // amber/gold
            case ARACHNID -> 0xFFCCCCDD;  // blue-silver
            case CENTAUR  -> 0xFFDDCC99;  // tan
            case SPRITE   -> 0xFFCC88FF;  // lavender
            default -> -1;
        };
        if (tint != -1) cir.setReturnValue(tint);
    }
}
