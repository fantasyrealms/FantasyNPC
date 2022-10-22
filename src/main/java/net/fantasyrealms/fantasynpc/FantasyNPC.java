package net.fantasyrealms.fantasynpc;

import com.github.juliarn.npc.NPCPool;
import com.github.unldenis.hologram.HologramPool;
import de.exlll.configlib.YamlConfigurations;
import lombok.Getter;
import net.fantasyrealms.fantasynpc.commands.FantasyNPCCommand;
import net.fantasyrealms.fantasynpc.config.FConfig;
import net.fantasyrealms.fantasynpc.config.NPCData;
import net.fantasyrealms.fantasynpc.constants.ConfigProperties;
import net.fantasyrealms.fantasynpc.constants.Constants;
import net.fantasyrealms.fantasynpc.manager.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.bukkit.BukkitCommandHandler;

import java.io.File;

public class FantasyNPC extends JavaPlugin {

	@Getter private static FantasyNPC instance;
	@Getter private NPCPool npcPool;
	@Getter private CommandHandler commandHandler;
	@Getter private HologramPool hologramPool;
	@Getter private FConfig pluginConfig;
	@Getter private NPCData npcData;

	@Override
	public void onEnable() {
		instance = this;

		Constants.LOGO.forEach(getLogger()::info);

		getLogger().info("Loading config...");
		pluginConfig = YamlConfigurations.update(new File(this.getDataFolder(), "config.yml").toPath(), FConfig.class, ConfigProperties.CONFIG);
		npcData = ConfigManager.loadNPCData();

		getLogger().info("Loading commands...");
		commandHandler = BukkitCommandHandler.create(this);
		commandHandler.setHelpWriter((command, actor) -> String.format("&8• &e/%s %s &7- &f%s", command.getPath().toRealString(), command.getUsage(), command.getDescription()));
		commandHandler.register(new FantasyNPCCommand());

		getLogger().info("Loading Holograms...");
		this.hologramPool = new HologramPool(this, pluginConfig.getHologram().getSpawnDistance(),
				pluginConfig.getHologram().getMinHitDistance(), pluginConfig.getHologram().getMaxHitDistance());

		getLogger().info("Loading NPCs...");
		this.npcPool = NPCPool.builder(this)
				.spawnDistance(pluginConfig.getNpc().getSpawnDistance())
				.actionDistance(pluginConfig.getNpc().getActionDistance())
				.tabListRemoveTicks(pluginConfig.getNpc().getTabListRemoveTicks())
				.build();


		getLogger().info("Thank you for using FantasyNPC!");
	}
}
