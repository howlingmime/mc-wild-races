package com.wildraces.mixin;

import com.wildraces.PlayerRaceAccess;
import com.wildraces.race.Race;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow private Optional<BlockPos> lastClimbablePos;

    // ── Arachnid: wall climbing (onClimbable) ───────────────────────────────
    // This mixin fires on BOTH client (LocalPlayer) and server (ServerPlayer).
    // Race is populated on the client via RacePacket, so it will be non-NONE.

    @Inject(method = "onClimbable", at = @At("RETURN"), cancellable = true)
    private void allowArachnidWallClimb(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;
        if (!((Object) this instanceof Player player)) return;
        if (((PlayerRaceAccess) player).wildraces$getRace() != Race.ARACHNID) return;

        BlockPos foot = player.blockPosition();
        Level level = player.level();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (!level.getBlockState(foot.relative(dir)).isAir()) {
                this.lastClimbablePos = Optional.of(foot);
                cir.setReturnValue(true);
                return;
            }
        }
    }

    // ── Arachnid: step height (maxUpStep on LivingEntity, not Player) ────────

    @Inject(method = "maxUpStep", at = @At("RETURN"), cancellable = true)
    private void arachnidStepHeight(CallbackInfoReturnable<Float> cir) {
        if (!((Object) this instanceof Player player)) return;
        if (((PlayerRaceAccess) player).wildraces$getRace() == Race.ARACHNID) {
            cir.setReturnValue(Math.max(cir.getReturnValue(), 1.25f));
        }
    }

    // ── Arachnid: active climbing via travel() ──────────────────────────────
    // When an arachnid is adjacent to a wall:
    //  • No movement keys held → hover (Y = 0, don't fall)
    //  • Movement keys held    → climb upward proportionally to input
    // This runs on LocalPlayer (client) once the race sync packet is received.

    @Inject(method = "travel", at = @At("TAIL"))
    private void arachnidActiveClimb(Vec3 travelVector, CallbackInfo ci) {
        if (!((Object) this instanceof Player player)) return;
        if (((PlayerRaceAccess) player).wildraces$getRace() != Race.ARACHNID) return;

        BlockPos foot = player.blockPosition();
        Level level = player.level();
        boolean nearWall = false;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (!level.getBlockState(foot.relative(dir)).isAir()) {
                nearWall = true;
                break;
            }
        }
        if (!nearWall) return;

        Vec3 movement = player.getDeltaMovement();
        if (movement.y >= 0) return; // already moving up or stationary, leave it

        double inputMag = Math.sqrt(travelVector.x * travelVector.x + travelVector.z * travelVector.z);
        double newY = (inputMag > 0.01)
            ? Math.min(inputMag * 2.5, 0.35)  // climbing: convert movement input to upward velocity
            : 0.0;                              // hovering: cancel gravity
        player.setDeltaMovement(movement.x, newY, movement.z);
        player.fallDistance = 0;
    }

    // ── Lionbear: landing shockwave ──────────────────────────────────────────
    // Inject at HEAD to read fallDistance before checkFallDamage resets it.
    // The first `double y` param is the Y velocity delta, NOT the fall height.
    // Radius is stepped by fall height; effect zone is a true horizontal circle.

    @Inject(method = "checkFallDamage", at = @At("HEAD"))
    private void lionbearPounce(double y, boolean onGround,
                                BlockState state, BlockPos pos, CallbackInfo ci) {
        if (!onGround) return;
        LivingEntity self = (LivingEntity)(Object)this;
        float fallDist = (float) self.fallDistance;
        double radius = wildraces$pounceRadius(fallDist);
        if (radius <= 0) return;
        if (!(self instanceof Player player)) return;
        if (((PlayerRaceAccess) player).wildraces$getRace() != Race.LIONBEAR) return;

        float damage = (float) Math.min(fallDist * 1.5, 12.0);

        // Particle ring — scatter explosion particles across the shockwave circle
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                player.getX(), player.getY() + 0.1, player.getZ(),
                Math.max(3, (int)(radius * 4)),
                radius * 0.6, 0.05, radius * 0.6,
                0.0);
        }

        // True circular area (horizontal distance only, ±3 blocks vertically)
        double r2 = radius * radius;
        AABB area = player.getBoundingBox().inflate(radius + 1, 3, radius + 1);
        List<LivingEntity> nearby = player.level().getEntitiesOfClass(
            LivingEntity.class, area, e -> {
                if (e == player) return false;
                double dx = e.getX() - player.getX();
                double dz = e.getZ() - player.getZ();
                return dx*dx + dz*dz <= r2;
            });
        for (LivingEntity e : nearby) {
            e.hurt(player.damageSources().playerAttack(player), damage);
        }
    }

    /** Stepped shockwave radius table (fall height in blocks → blast radius in blocks). */
    @Unique
    private static double wildraces$pounceRadius(float fallDist) {
        if (fallDist >= 50) return 8;
        if (fallDist >= 40) return 7;
        if (fallDist >= 30) return 6;
        if (fallDist >= 20) return 5;
        if (fallDist >= 10) return 4;
        if (fallDist >= 8)  return 3;
        if (fallDist >= 6)  return 2;
        if (fallDist >= 4)  return 1;
        return 0;
    }

    // ── Troll: fire damage taken is doubled ─────────────────────────────────
    // Intercept hurtServer, amplify fire damage, re-call with the guard set to
    // avoid infinite recursion.

    @Unique
    private static final ThreadLocal<Boolean> wildraces$applyingFireBoost =
        ThreadLocal.withInitial(() -> false);

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void trollFireWeakness(ServerLevel level, DamageSource source, float amount,
                                   CallbackInfoReturnable<Boolean> cir) {
        if (wildraces$applyingFireBoost.get()) return;
        if (!((Object) this instanceof Player player)) return;
        if (((PlayerRaceAccess) player).wildraces$getRace() != Race.TROLL) return;
        if (!source.is(DamageTypeTags.IS_FIRE)) return;

        wildraces$applyingFireBoost.set(true);
        try {
            LivingEntity self = (LivingEntity)(Object)this;
            boolean result = self.hurtServer(level, source, amount * 2.0f);
            cir.setReturnValue(result);
            cir.cancel();
        } finally {
            wildraces$applyingFireBoost.set(false);
        }
    }
}
