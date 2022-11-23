package net.fantasyrealms.fantasynpc.conversion;

import net.fantasyrealms.fantasynpc.conversion.impl.ServerNPCConversion;
import net.fantasyrealms.fantasynpc.objects.FNPC;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ConversionManager {

	public static CompletableFuture<List<FNPC>> conversion(ConversionPlugin plugin) {
		if (plugin == null) {
			return CompletableFuture.completedFuture(Collections.emptyList());
		}

		return CompletableFuture.supplyAsync(plugin::getNPCs);
	}

	public static class SupportedPlugin {
		public static final Map<String, ConversionPlugin> PLUGINS = new HashMap<>();

		static {
			register("ServerNPC", new ServerNPCConversion());
		}

		/**
		 * Register a plugin to be converted.
		 *
		 * @param name   The name of the plugin.
		 * @param plugin The plugin instance.
		 */
		public static void register(String name, ConversionPlugin plugin) {
			PLUGINS.put(name.toLowerCase(), plugin);
		}

		/**
		 * Match a plugin name to a plugin converter
		 *
		 * @param name The name of the plugin.
		 * @return The plugin converter.
		 */
		public static Optional<ConversionPlugin> match(String name) {
			return Optional.ofNullable(PLUGINS.get(name.toLowerCase()));
		}

	}
}
