package com.wildraces.client.render;

import com.wildraces.race.Race;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;

import java.util.EnumMap;
import java.util.Map;

/**
 * Defines the ModelLayerLocation and LayerDefinition for each race's overlay parts.
 * For every race we have two model layers: one for parts that attach to the head
 * (and follow head rotation) and one for parts that attach to the body.
 *
 * All cubes UV-map to (0,0) on a 16x16 solid-white texture; tinting is applied
 * via the color argument in RaceOverlayLayer.
 */
public final class RaceOverlayModels {

    public static final Identifier OVERLAY_TEXTURE =
        Identifier.fromNamespaceAndPath("wildraces", "textures/entity/overlay.png");

    public static final Map<Race, ModelLayerLocation> HEAD_LAYERS = new EnumMap<>(Race.class);
    public static final Map<Race, ModelLayerLocation> BODY_LAYERS = new EnumMap<>(Race.class);

    static {
        for (Race r : Race.values()) {
            if (r == Race.NONE) continue;
            String name = r.name().toLowerCase();
            HEAD_LAYERS.put(r, new ModelLayerLocation(
                Identifier.fromNamespaceAndPath("wildraces", name + "_head"), "main"));
            BODY_LAYERS.put(r, new ModelLayerLocation(
                Identifier.fromNamespaceAndPath("wildraces", name + "_body"), "main"));
        }
    }

    /** Empty layer used when a race has no parts at a given attachment. */
    public static LayerDefinition empty() {
        return LayerDefinition.create(new MeshDefinition(), 16, 16);
    }

    // ── Per-race HEAD overlays (rotate with head) ────────────────────────────

    /** Minotaur: two curved horns rising from the top of the head. */
    public static LayerDefinition minotaurHead() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // Head bone origin is at the neck; head cube extends y=-8 to 0.
        // Horns sit on top (y ≈ -8) and angle outward and slightly forward.
        root.addOrReplaceChild("left_horn",
            CubeListBuilder.create().texOffs(0, 0).addBox(-1f, -6f, -1f, 2, 6, 2),
            PartPose.offsetAndRotation(3.5f, -8f, 0f, -0.3f, 0f, 0.6f));
        root.addOrReplaceChild("right_horn",
            CubeListBuilder.create().texOffs(0, 0).addBox(-1f, -6f, -1f, 2, 6, 2),
            PartPose.offsetAndRotation(-3.5f, -8f, 0f, -0.3f, 0f, -0.6f));
        return LayerDefinition.create(mesh, 16, 16);
    }

    /** Lionbear: a thick mane ring around the head. */
    public static LayerDefinition lionbearHead() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // Bulk around the head: a ring of cubes attached to head bone.
        // Four cubes around the perimeter, slightly fluffy (1.5 wider than head).
        root.addOrReplaceChild("mane_back",
            CubeListBuilder.create().texOffs(0, 0).addBox(-5f, -7f, 4f, 10, 7, 2),
            PartPose.ZERO);
        root.addOrReplaceChild("mane_left",
            CubeListBuilder.create().texOffs(0, 0).addBox(4f, -7f, -4f, 2, 7, 8),
            PartPose.ZERO);
        root.addOrReplaceChild("mane_right",
            CubeListBuilder.create().texOffs(0, 0).addBox(-6f, -7f, -4f, 2, 7, 8),
            PartPose.ZERO);
        root.addOrReplaceChild("mane_top",
            CubeListBuilder.create().texOffs(0, 0).addBox(-5f, -10f, -5f, 10, 2, 10),
            PartPose.ZERO);
        return LayerDefinition.create(mesh, 16, 16);
    }

    /** Troll: two short tusks jutting from the lower jaw. */
    public static LayerDefinition trollHead() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("left_tusk",
            CubeListBuilder.create().texOffs(0, 0).addBox(-0.5f, -1f, -0.5f, 1, 3, 1),
            PartPose.offsetAndRotation(2f, -1.5f, -4.1f, 0.4f, 0f, 0f));
        root.addOrReplaceChild("right_tusk",
            CubeListBuilder.create().texOffs(0, 0).addBox(-0.5f, -1f, -0.5f, 1, 3, 1),
            PartPose.offsetAndRotation(-2f, -1.5f, -4.1f, 0.4f, 0f, 0f));
        return LayerDefinition.create(mesh, 16, 16);
    }

    // ── Per-race BODY overlays (follow body rotation) ────────────────────────

    /** Arachnid: four extra spider legs sticking out from the body sides. */
    public static LayerDefinition arachnidBody() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // Body coordinates: body spans (-4..4, 0..12, -2..2). Legs angle outward and down.
        // Upper pair (higher on body)
        root.addOrReplaceChild("upper_left_leg",
            CubeListBuilder.create().texOffs(0, 0).addBox(0f, -1f, -1f, 8, 2, 2),
            PartPose.offsetAndRotation(4f, 3f, 0f, 0f, -0.3f, 0.6f));
        root.addOrReplaceChild("upper_right_leg",
            CubeListBuilder.create().texOffs(0, 0).addBox(-8f, -1f, -1f, 8, 2, 2),
            PartPose.offsetAndRotation(-4f, 3f, 0f, 0f, 0.3f, -0.6f));
        // Lower pair (mid body)
        root.addOrReplaceChild("lower_left_leg",
            CubeListBuilder.create().texOffs(0, 0).addBox(0f, -1f, -1f, 8, 2, 2),
            PartPose.offsetAndRotation(4f, 7f, 0f, 0f, -0.2f, 0.8f));
        root.addOrReplaceChild("lower_right_leg",
            CubeListBuilder.create().texOffs(0, 0).addBox(-8f, -1f, -1f, 8, 2, 2),
            PartPose.offsetAndRotation(-4f, 7f, 0f, 0f, 0.2f, -0.8f));
        return LayerDefinition.create(mesh, 16, 16);
    }

    /** Lionbear: a tail trailing behind the body. */
    public static LayerDefinition lionbearBody() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("tail",
            CubeListBuilder.create().texOffs(0, 0).addBox(-1f, 0f, 0f, 2, 8, 2),
            PartPose.offsetAndRotation(0f, 10f, 2f, -0.4f, 0f, 0f));
        return LayerDefinition.create(mesh, 16, 16);
    }

    /** Sprite: two thin wings on the upper back. */
    public static LayerDefinition spriteBody() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // Wings as thin flat planes attached to the upper back, swept backward.
        root.addOrReplaceChild("left_wing",
            CubeListBuilder.create().texOffs(0, 0).addBox(0f, -3f, 0f, 6, 6, 0),
            PartPose.offsetAndRotation(0.5f, 2f, 2.1f, 0f, -0.5f, 0f));
        root.addOrReplaceChild("right_wing",
            CubeListBuilder.create().texOffs(0, 0).addBox(-6f, -3f, 0f, 6, 6, 0),
            PartPose.offsetAndRotation(-0.5f, 2f, 2.1f, 0f, 0.5f, 0f));
        return LayerDefinition.create(mesh, 16, 16);
    }

    /** Centaur: a small horsetail tuft at the lower back. */
    public static LayerDefinition centaurBody() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("tail",
            CubeListBuilder.create().texOffs(0, 0).addBox(-1f, 0f, 0f, 2, 6, 1),
            PartPose.offsetAndRotation(0f, 10f, 2.1f, -0.2f, 0f, 0f));
        return LayerDefinition.create(mesh, 16, 16);
    }

    private RaceOverlayModels() {}
}
