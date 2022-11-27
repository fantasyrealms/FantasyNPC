package net.fantasyrealms.fantasynpc.util;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.fantasyrealms.fantasynpc.FantasyNPC;
import net.fantasyrealms.fantasynpc.objects.FNPC;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;

public class PlayerUtils {

	public static void setNPCScoreboard(Player player) {
		// create a new scoreboard for the player if the player uses the main scoreboard
		var manager = player.getServer().getScoreboardManager();
		if (manager != null && player.getScoreboard().equals(manager.getMainScoreboard())) {
			player.setScoreboard(manager.getNewScoreboard());
		}

		Scoreboard scoreboard = player.getScoreboard();
		NPCUtils.getNPCs().stream().filter(npc -> !npc.isShowNameTag()).map(FNPC::getName).forEach(n -> {
			// check if a team for this entity is already created
			var team = scoreboard.getTeam(n);
			if (team == null) {
				team = scoreboard.registerNewTeam(n);
			}
			// set the name tag visibility of the team
			team.setNameTagVisibility(NameTagVisibility.NEVER);
			// register the spawned entity to the team
			team.addEntry(n);
		});
	}

	public static void sendServer(Player player, String server) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Connect");
		out.writeUTF(server);

		player.sendPluginMessage(FantasyNPC.getInstance(), "BungeeCord", out.toByteArray());
	}

}
