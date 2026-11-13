package com.example.bettercrit.mixin;

import com.example.bettercrit.CritAttacker;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric has no crit event, so we evaluate the vanilla crit conditions at the very start of
 * Player.attack (before the attack cooldown is reset) and remember which entity is being crit.
 */
@Mixin(Player.class)
public abstract class PlayerMixin implements CritAttacker {
	@Unique
	private Entity bettercrit$critTarget;

	@Inject(method = "attack(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"))
	private void bettercrit$begin(Entity target, CallbackInfo ci) {
		Player self = (Player) (Object) this;
		this.bettercrit$critTarget = null;
		if (self.level().isClientSide() || !(target instanceof LivingEntity)) {
			return;
		}
		boolean fullyCharged = self.getAttackStrengthScale(0.5F) > 0.9F;
		boolean crit = fullyCharged
			&& self.fallDistance > 0.0
			&& !self.onGround()
			&& !self.onClimbable()
			&& !self.isInWater()
			&& !self.hasEffect(MobEffects.BLINDNESS)
			&& !self.isPassenger()
			&& !self.isSprinting();
		if (crit) {
			this.bettercrit$critTarget = target;
		}
	}

	@Inject(method = "attack(Lnet/minecraft/world/entity/Entity;)V", at = @At("RETURN"))
	private void bettercrit$end(Entity target, CallbackInfo ci) {
		this.bettercrit$critTarget = null;
	}

	@Override
	public Entity getCritTarget() {
		return this.bettercrit$critTarget;
	}
}
