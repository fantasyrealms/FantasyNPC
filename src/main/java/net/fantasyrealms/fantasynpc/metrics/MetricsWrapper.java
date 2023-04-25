package net.fantasyrealms.fantasynpc.metrics;

import net.fantasyrealms.fantasynpc.FantasyNPC;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MetricsWrapper {

	private final FantasyNPC fantasyNPC;
	private final Metrics metrics;

	public MetricsWrapper(JavaPlugin plugin, int serviceId) {
		this.fantasyNPC = (FantasyNPC) Bukkit.getPluginManager().getPlugin("FantasyNPC");
		this.metrics = new Metrics(plugin, serviceId);

		addNPCCountChart();
	}

	public void addNPCCountChart() {
		metrics.addCustomChart(new SingleLineChart("npcCount", () -> fantasyNPC.getNpcData().getNpcs().size()));
	}
}