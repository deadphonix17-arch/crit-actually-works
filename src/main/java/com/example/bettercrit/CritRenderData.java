package com.example.bettercrit;

/** Implemented on LivingEntityRenderState by a client mixin. */
public interface CritRenderData {
	float getCritStun();
	int getCritHits();
	void setCritData(float stun, int hits);
}
