package com.example.bettercrit.mixin.client;

import com.example.bettercrit.BetterCritConfig;
import com.example.bettercrit.CritRenderData;
import com.example.bettercrit.CritStunHolder;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

	/** Copy the synced crit data from the entity into the render state. */
	@Inject(
		method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
		at = @At("TAIL")
	)
	private void bettercrit$extract(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
		if (state instanceof CritRenderData data) {
			if (entity instanceof CritStunHolder holder) {
				data.setCritData(holder.getCritStunned(), holder.getAccumulatedHits());
			} else {
				data.setCritData(0.0F, 0);
			}
		}
	}

	/** White flash that gets stronger with every absorbed hit. */
	@Inject(method = "getOverlayCoords", at = @At("RETURN"), cancellable = true)
	private static void bettercrit$overlay(LivingEntityRenderState state, float whiteOverlayProgress,
										   CallbackInfoReturnable<Integer> cir) {
		if (state instanceof CritRenderData data && data.getCritStun() > 0.0F) {
			float progress = (float) data.getCritHits() / (float) Math.max(1, BetterCritConfig.maxHits);
			int u = Math.min(15, Math.round(15.0F * progress));
			cir.setReturnValue(OverlayTexture.pack(u, 10));
		}
	}
}
