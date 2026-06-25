package com.wildraces.client.render;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/** Thin Model wrapper around a baked overlay ModelPart, drawn as a cutout. */
public class RaceOverlayModel extends Model<AvatarRenderState> {
    public RaceOverlayModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
    }
}
