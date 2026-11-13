package com.example.bettercrit;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterCrit implements ModInitializer {
	public static final String MOD_ID = "bettercrit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		BetterCritConfig.load();
	}
}
