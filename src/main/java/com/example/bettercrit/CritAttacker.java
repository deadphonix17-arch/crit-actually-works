package com.example.bettercrit;

import net.minecraft.world.entity.Entity;

/** Implemented on Player by PlayerMixin. */
public interface CritAttacker {
	/** The entity currently being hit with a crit by this player, or null. */
	Entity getCritTarget();
}
