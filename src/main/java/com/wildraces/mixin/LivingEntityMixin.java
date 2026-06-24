package com.wildraces.mixin;

import com.wildraces.PlayerRaceAccess;
import com.wildraces.WildRacesMod;
import com.wildraces.race.Race;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    private Optional<BlockPos> lastClimbablePos;

    // ── Arachnid: wall climbing ──────────────────────────────────────────────
    // Scan all four cardinal directions for any solid (non-air) block adjacent
    // to the player. This is more reliable than horizontalCollision, which may
    // not be set in the same tick that onClimbable() is evaluated.

    @Inject(method = "onClimbable", at = @At("RETURN"), cancellable = true)
    private void allowArachnidWallClimb(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;
        if (!((Object) this instanceof Player player)) return;
        if (((PlayerRaceAccess) player).wildraces$getRace() != Race.ARACHNID) return;

        BlockPos foot = player.blockPosition();
        Level level = player.level();
        BlockPos wallBlock = null;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = foot.relative(dir);
            if (!level.getBlockState(neighbor).isAir()) {
                wallBlock = neighbor;
                break;
            }
        }
        if (wallBlock == null) return;

        WildRacesMod.LOGGER.debug("[WildRaces] Arachnid climbing at {}", foot);
        this.lastClimbablePos = Optional.of(foot);
        cir.setReturnValue(true);
    }

    // ── Arachnid: step over ledges up to 1.25 blocks tall ───────────────────
    // Default maxUpStep for players is 0.6. Raising it to 1.25 lets the arachnid
    // walk over full 1-block ledges while scaling walls.

    @Inject(method = "maxUpStep", at = @At("RETURN"), cancellable = true)
    private void arachnidStepHeight(CallbackInfoReturnable<Float> cir) {
        if (!((Object) this instanceof Player player)) return;
        if (((PlayerRaceAccess) player).wildraces$getRace() == Race.ARACHNID) {
            cir.setReturnValue(Math.max(cir.getReturnValue(), 1.25f));
        }
    }

    // ── Lionbear: landing shockwave ──────────────────────────────────────────
    // Inject at HEAD so we read entity.fallDistance before checkFallDamage resets it.
    // The first double param (y) is the raw Y velocity delta, NOT the fall height.

    @Inject(method = "checkFallDamage", at = @At("HEAD"))
    private void lionbearPounce(double y, boolean onGround,
                                BlockState state, BlockPos pos, CallbackInfo ci) {
        if (!onGround) return;
        LivingEntity self = (LivingEntity)(Object)this;
        float fallDist = (float) self.fallDistance;
        if (fallDist < 3.0f) return;
        if (!(self instanceof Player player)) return;
        if (((PlayerRaceAccess) player).wildraces$getRace() != Race.LIONBEAR) return;

        double radius = Math.min(fallDist, 8.0);
        float damage = (float) Math.min(fallDist * 1.5, 12.0);

        AABB area = player.getBoundingBox().inflate(radius);
        List<LivingEntity> nearby = player.level().getEntitiesOfClass(
            LivingEntity.class, area, e -> e != player);
        for (LivingEntity e : nearby) {
            e.hurt(player.damageSources().playerAttack(player), damage);
        }
    }
}
