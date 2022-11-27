package net.fantasyrealms.fantasynpc.event;

import net.fantasyrealms.fantasynpc.util.PlayerUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void oJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		PlayerUtils.setNPCScoreboard(player);
	}
}
