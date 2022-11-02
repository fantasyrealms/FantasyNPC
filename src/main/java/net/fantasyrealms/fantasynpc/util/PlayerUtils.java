package net.fantasyrealms.fantasynpc.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class PlayerUtils {

	public static void setNPCScoreboard(Player player) {
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard scoreboard = manager.getNewScoreboard();
		Team team = scoreboard.getTeam(player.getName()) == null ? scoreboard.registerNewTeam(player.getName()) : scoreboard.getTeam(player.getName());
		team.setNameTagVisibility(NameTagVisibility.NEVER);
		team.getEntries().forEach(team::removeEntry);
		team.addEntry(player.getName());
		NPCUtils.getNPCNames().forEach(team::addEntry);
		player.setScoreboard(scoreboard);
	}

}
