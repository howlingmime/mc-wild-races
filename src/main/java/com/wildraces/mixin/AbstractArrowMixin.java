package com.wildraces.mixin;

import com.wildraces.PlayerRaceAccess;
import com.wildraces.race.Race;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {

    // ── Centaur: +50% arrow/bolt damage ─────────────────────────────────────
    // Intercepts the float damage value passed to LivingEntity.hurt() inside
    // AbstractArrow.onHitEntity and multiplies it when the owner is a centaur.

    @ModifyArg(
        method = "onHitEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
        ),
        index = 1
    )
    private float boostCentaurArrowDamage(float damage) {
        AbstractArrow self = (AbstractArrow)(Object)this;
        Entity owner = self.getOwner();
        if (owner instanceof Player player
                && ((PlayerRaceAccess) player).wildraces$getRace() == Race.CENTAUR) {
            return damage * 1.5f;
        }
        return damage;
    }
}
