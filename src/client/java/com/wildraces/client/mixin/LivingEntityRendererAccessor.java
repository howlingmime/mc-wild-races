package com.wildraces.client.mixin;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Exposes the protected `layers` list on LivingEntityRenderer so other mixins
 * (notably {@link AvatarRendererMixin}) can attach a custom RenderLayer without
 * needing to subclass.
 */
@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererAccessor<S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    @Accessor("layers")
    List<RenderLayer<S, M>> wildraces$getLayers();
}
