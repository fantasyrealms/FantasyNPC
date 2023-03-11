package net.fantasyrealms.fantasynpc.util;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static revxrsal.commands.util.Strings.colorize;

public class Common {

	public static void sendMessage(Player player, String message) {
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			player.sendMessage(colorize(PlaceholderAPI.setPlaceholders(player, message)));
		} else {
			player.sendMessage(colorize(message));
		}
	}
}
