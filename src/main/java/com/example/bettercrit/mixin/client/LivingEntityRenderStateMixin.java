package com.example.bettercrit.mixin.client;

import com.example.bettercrit.CritRenderData;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateMixin implements CritRenderData {
	@Unique
	private float bettercrit$stun;
	@Unique
	private int bettercrit$hits;

	@Override
	public float getCritStun() {
		return this.bettercrit$stun;
	}

	@Override
	public int getCritHits() {
		return this.bettercrit$hits;
	}

	@Override
	public void setCritData(float stun, int hits) {
		this.bettercrit$stun = stun;
		this.bettercrit$hits = hits;
	}
}
