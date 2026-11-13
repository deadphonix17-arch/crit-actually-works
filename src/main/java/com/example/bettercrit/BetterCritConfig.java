package com.example.bettercrit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Simple JSON config at config/bettercrit.json (restart the game to apply changes). */
public final class BetterCritConfig {
	/** Master switch for the whole crit-stun system. */
	public static boolean enableCrits = true;
	/** Max knockback multiplier of the released hit. 1.0 - 32767.0 */
	public static double critMultiplier = 2.0;
	/** Number of crits needed before the stored knockback is released. 1 - 32767 */
	public static int maxHits = 3;
	/** Ticks a mob stays frozen after each crit. 1 - 32767 */
	public static int stunTicks = 20;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static class Data {
		boolean enableCrits = true;
		double critMultiplier = 2.0;
		int maxHits = 3;
		int stunTicks = 20;
	}

	private BetterCritConfig() {}

	public static void load() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve("bettercrit.json");
		Data data = new Data();
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				Data read = GSON.fromJson(reader, Data.class);
				if (read != null) data = read;
			} catch (Exception e) {
				BetterCrit.LOGGER.warn("Could not read bettercrit.json, using defaults", e);
			}
		}
		data.critMultiplier = Math.max(1.0, Math.min(32767.0, data.critMultiplier));
		data.maxHits = Math.max(1, Math.min(32767, data.maxHits));
		data.stunTicks = Math.max(1, Math.min(32767, data.stunTicks));

		enableCrits = data.enableCrits;
		critMultiplier = data.critMultiplier;
		maxHits = data.maxHits;
		stunTicks = data.stunTicks;

		try (Writer writer = Files.newBufferedWriter(path)) {
			GSON.toJson(data, writer);
		} catch (Exception e) {
			BetterCrit.LOGGER.warn("Could not write bettercrit.json", e);
		}
	}
}
