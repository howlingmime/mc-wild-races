package com.wildraces.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildraces.client.WildRacesClient;
import com.wildraces.race.Race;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

import java.util.EnumMap;
import java.util.Map;

/**
 * Renders the race-specific 3D overlay parts (horns, mane, wings, etc.) on top
 * of the player model. Head-attached parts follow head rotation; body-attached
 * parts follow body rotation (e.g. crouching).
 */
public class RaceOverlayLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

    /** Tint color (ARGB) applied to each race's overlay parts. */
    private static final Map<Race, Integer> COLOR = new EnumMap<>(Race.class);
    static {
        COLOR.put(Race.MINOTAUR, 0xFFEEE7D5); // bone for horns
        COLOR.put(Race.LIONBEAR, 0xFF8B5A2B); // brown mane + tail
        COLOR.put(Race.ARACHNID, 0xFF1A1A1A); // black legs
        COLOR.put(Race.TROLL,    0xFFF2EBC9); // ivory tusks
        COLOR.put(Race.SPRITE,   0xFFD8E8FF); // pale blue wings
        COLOR.put(Race.CENTAUR,  0xFF5C3A1A); // dark brown tail
    }

    private final Map<Race, RaceOverlayModel> headModels;
    private final Map<Race, RaceOverlayModel> bodyModels;

    public RaceOverlayLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent,
                            Map<Race, RaceOverlayModel> headModels,
                            Map<Race, RaceOverlayModel> bodyModels) {
        super(parent);
        this.headModels = headModels;
        this.bodyModels = bodyModels;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, int packedLight,
                       AvatarRenderState state, float yaw, float pitch) {
        Race race = WildRacesClient.RACE_BY_ENTITY_ID.getOrDefault(state.id, Race.NONE);
        if (race == Race.NONE) return;
        int color = COLOR.getOrDefault(race, 0xFFFFFFFF);

        PlayerModel parent = getParentModel();

        RaceOverlayModel headModel = headModels.get(race);
        if (headModel != null) {
            pose.pushPose();
            parent.head.translateAndRotate(pose);
            renderColoredCutoutModel(headModel, RaceOverlayModels.OVERLAY_TEXTURE,
                pose, collector, packedLight, state, OverlayTexture.NO_OVERLAY, color);
            pose.popPose();
        }

        RaceOverlayModel bodyModel = bodyModels.get(race);
        if (bodyModel != null) {
            pose.pushPose();
            parent.body.translateAndRotate(pose);
            renderColoredCutoutModel(bodyModel, RaceOverlayModels.OVERLAY_TEXTURE,
                pose, collector, packedLight, state, OverlayTexture.NO_OVERLAY, color);
            pose.popPose();
        }
    }
}
