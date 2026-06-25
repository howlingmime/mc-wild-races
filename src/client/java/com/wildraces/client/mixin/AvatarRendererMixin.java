package com.wildraces.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildraces.client.WildRacesClient;
import com.wildraces.client.render.RaceOverlayLayer;
import com.wildraces.client.render.RaceOverlayModel;
import com.wildraces.client.render.RaceOverlayModels;
import com.wildraces.race.Race;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;
import java.util.Map;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {

    /** Scale Sprite players to roughly 1 block tall. */
    @Inject(method = "scale", at = @At("HEAD"))
    private void spriteModelScale(AvatarRenderState state, PoseStack poseStack, CallbackInfo ci) {
        Race race = WildRacesClient.RACE_BY_ENTITY_ID.getOrDefault(state.id, Race.NONE);
        if (race == Race.SPRITE) {
            poseStack.scale(0.55f, 0.55f, 0.55f);
        }
    }

    /** Bake all race overlay models on construction and attach the render layer. */
    @Inject(method = "<init>", at = @At("TAIL"))
    private void wildraces$installOverlay(EntityRendererProvider.Context ctx,
                                          boolean slim, CallbackInfo ci) {
        Map<Race, RaceOverlayModel> headModels = new EnumMap<>(Race.class);
        Map<Race, RaceOverlayModel> bodyModels = new EnumMap<>(Race.class);

        for (Race race : Race.values()) {
            if (race == Race.NONE) continue;
            ModelLayerLocation headLoc = RaceOverlayModels.HEAD_LAYERS.get(race);
            ModelLayerLocation bodyLoc = RaceOverlayModels.BODY_LAYERS.get(race);
            ModelPart headRoot = ctx.getModelSet().bakeLayer(headLoc);
            ModelPart bodyRoot = ctx.getModelSet().bakeLayer(bodyLoc);
            headModels.put(race, new RaceOverlayModel(headRoot));
            bodyModels.put(race, new RaceOverlayModel(bodyRoot));
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        RaceOverlayLayer overlay = new RaceOverlayLayer(
            (RenderLayerParent<AvatarRenderState, PlayerModel>) (Object) this,
            headModels, bodyModels);
        @SuppressWarnings({"unchecked", "rawtypes"})
        LivingEntityRendererAccessor<AvatarRenderState, PlayerModel> access =
            (LivingEntityRendererAccessor) this;
        access.wildraces$getLayers().add(overlay);
    }
}
