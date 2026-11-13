package com.example.bettercrit;

/** Implemented on every LivingEntity by LivingEntityMixin. */
public interface CritStunHolder {
	/** 0..1, synced to clients. 1 = primed, next knockback is released. */
	float getCritStunned();
	void setCritStunned(float value);
	void addCritStunned(float value);

	/** Knockbacks absorbed so far, synced to clients. */
	int getAccumulatedHits();
	void setAccumulatedHits(int value);

	/** Server-only state. */
	int getStunTicks();
	void setStunTicks(int ticks);
	float getAccumulatedKnockback();
	void setAccumulatedKnockback(float value);
}
