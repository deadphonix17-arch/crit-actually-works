package com.example.bettercrit.mixin.client;

import com.example.bettercrit.CritStunHolder;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

	/** Stunned entities glow brighter the closer they are to being fully charged. */
	@Inject(method = "getBlockLightLevel", at = @At("RETURN"), cancellable = true)
	private void bettercrit$light(Entity entity, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
		if (entity instanceof CritStunHolder holder && holder.getCritStunned() > 0.0F) {
			int glow = Math.round(15.0F * holder.getCritStunned());
			cir.setReturnValue(Math.max(cir.getReturnValueI(), glow));
		}
	}
}
