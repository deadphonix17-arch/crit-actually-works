package com.example.bettercrit.mixin;

import com.example.bettercrit.BetterCritConfig;
import com.example.bettercrit.CritAttacker;
import com.example.bettercrit.CritStunHolder;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements CritStunHolder {
	@Unique
	private static final EntityDataAccessor<Float> BETTERCRIT_STUN =
		SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.FLOAT);
	@Unique
	private static final EntityDataAccessor<Integer> BETTERCRIT_HITS =
		SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);

	@Unique
	private int bettercrit$stunTicks;
	@Unique
	private float bettercrit$accKnockback;

	// ---- synced data -------------------------------------------------------------------

	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	private void bettercrit$define(SynchedEntityData.Builder builder, CallbackInfo ci) {
		builder.define(BETTERCRIT_STUN, 0.0F);
		builder.define(BETTERCRIT_HITS, 0);
	}

	@Override
	public float getCritStunned() {
		return ((LivingEntity) (Object) this).getEntityData().get(BETTERCRIT_STUN);
	}

	@Override
	public void setCritStunned(float value) {
		((LivingEntity) (Object) this).getEntityData().set(BETTERCRIT_STUN, Mth.clamp(value, 0.0F, 1.0F));
	}

	@Override
	public void addCritStunned(float value) {
		setCritStunned(getCritStunned() + value);
	}

	@Override
	public int getAccumulatedHits() {
		return ((LivingEntity) (Object) this).getEntityData().get(BETTERCRIT_HITS);
	}

	@Override
	public void setAccumulatedHits(int value) {
		((LivingEntity) (Object) this).getEntityData().set(BETTERCRIT_HITS, value);
	}

	@Override
	public int getStunTicks() {
		return this.bettercrit$stunTicks;
	}

	@Override
	public void setStunTicks(int ticks) {
		this.bettercrit$stunTicks = ticks;
	}

	@Override
	public float getAccumulatedKnockback() {
		return this.bettercrit$accKnockback;
	}

	@Override
	public void setAccumulatedKnockback(float value) {
		this.bettercrit$accKnockback = value;
	}

	// ---- crit handling (replaces NeoForge's CriticalHitEvent) --------------------------

	@Inject(method = "hurtServer", at = @At("HEAD"))
	private void bettercrit$onHurt(ServerLevel level, DamageSource source, float amount,
								   CallbackInfoReturnable<Boolean> cir) {
		if (!BetterCritConfig.enableCrits) return;
		if (!(source.getDirectEntity() instanceof Player player)) return;

		LivingEntity self = (LivingEntity) (Object) this;
		if (((CritAttacker) player).getCritTarget() != self) return;
		if (self instanceof Player || self instanceof EnderDragon || self instanceof WitherBoss) return;
		if (getCritStunned() >= 1.0F) return;

		float maxHealth = Math.max(self.getMaxHealth(), 1.0F);
		int maxHits = Math.max(1, BetterCritConfig.maxHits);

		if (getAccumulatedHits() >= maxHits - 1) {
			setCritStunned(1.0F);
		} else {
			addCritStunned(amount / maxHealth);
		}
		setStunTicks(getCritStunned() < 1.0F ? BetterCritConfig.stunTicks : 0);
	}

	// ---- knockback (replaces NeoForge's LivingKnockBackEvent) --------------------------

	@WrapMethod(method = "knockback(DDD)V")
	private void bettercrit$knockback(double strength, double x, double z, Operation<Void> original) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!BetterCritConfig.enableCrits || self.level().isClientSide()) {
			original.call(strength, x, z);
			return;
		}

		float stun = getCritStunned();
		if (stun <= 0.0F) {
			original.call(strength, x, z);
			return;
		}

		this.bettercrit$accKnockback += (float) strength;

		if (stun < 1.0F) {
			// still charging: swallow the knockback and store it
			setAccumulatedHits(getAccumulatedHits() + 1);
			return;
		}

		// fully charged: release everything in one hit
		double mult = BetterCritConfig.critMultiplier;
		double hitRatio = (double) getAccumulatedHits() / (double) Math.max(1, BetterCritConfig.maxHits);
		double released = Mth.clamp(this.bettercrit$accKnockback * (1.0 + mult * (1.0 - hitRatio)), 0.0, mult);

		bettercrit$reset();
		original.call(released, x, z);
	}

	@Unique
	private void bettercrit$reset() {
		setCritStunned(0.0F);
		setAccumulatedHits(0);
		this.bettercrit$accKnockback = 0.0F;
		this.bettercrit$stunTicks = 0;
	}

	// ---- stun timer (replaces NeoForge's EntityTickEvent.Pre) --------------------------

	@Inject(method = "tick", at = @At("HEAD"))
	private void bettercrit$tick(CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self.level().isClientSide()) return;

		float stun = getCritStunned();
		if (stun == 1.0F) return; // primed: waiting for the next knockback

		if (this.bettercrit$stunTicks > 0) {
			this.bettercrit$stunTicks--;
			self.setYRot(self.yRotO);
			self.setXRot(self.xRotO);
			if (self instanceof Mob mob) {
				mob.getNavigation().stop();
				mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0.0);
			}
		} else if (stun > 0.0F) {
			bettercrit$reset(); // stun window ran out
		}
	}
}
