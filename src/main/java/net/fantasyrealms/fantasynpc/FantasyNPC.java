package net.fantasyrealms.fantasynpc;

import com.github.juliarn.npclib.api.NpcActionController;
import com.github.juliarn.npclib.api.Platform;
import com.github.juliarn.npclib.api.event.InteractNpcEvent;
import com.github.juliarn.npclib.api.event.ShowNpcEvent;
import com.github.juliarn.npclib.bukkit.BukkitPlatform;
import com.github.juliarn.npclib.bukkit.protocol.BukkitProtocolAdapter;
import de.exlll.configlib.YamlConfigurations;
import gg.optimalgames.hologrambridge.HologramAPI;
import gg.optimalgames.hologrambridge.HologramBridge;
import lombok.Getter;
import lombok.Setter;
import net.fantasyrealms.fantasynpc.commands.FantasyNPCCommand;
import net.fantasyrealms.fantasynpc.config.FConfig;
import net.fantasyrealms.fantasynpc.config.NPCData;
import net.fantasyrealms.fantasynpc.constants.ConfigProperties;
import net.fantasyrealms.fantasynpc.constants.Constants;
import net.fantasyrealms.fantasynpc.conversion.ConversionManager;
import net.fantasyrealms.fantasynpc.conversion.ConversionPlugin;
import net.fantasyrealms.fantasynpc.event.NPCInteractNpcListener;
import net.fantasyrealms.fantasynpc.event.NPCShowListener;
import net.fantasyrealms.fantasynpc.event.PlayerJoinListener;
import net.fantasyrealms.fantasynpc.manager.ConfigManager;
import net.fantasyrealms.fantasynpc.manager.FNPCManager;
import net.fantasyrealms.fantasynpc.metrics.MetricsWrapper;
import net.fantasyrealms.fantasynpc.objects.FNPC;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.exception.CommandErrorException;

import java.io.File;

public class FantasyNPC extends JavaPlugin {

	@Getter private static FantasyNPC instance;
	@Getter private Platform<World, Player, ItemStack, Plugin> npcPlatform;
	@Getter private BukkitCommandHandler commandHandler;
	@Getter private FConfig pluginConfig;
	@Setter @Getter private NPCData npcData;
	public static BukkitAudiences ADVENTURE;
	public static final MiniMessage MINIMESSAGE = MiniMessage.miniMessage();
	@Getter private MetricsWrapper metricsWrapper;

	@Override
	public void onEnable() {
		instance = this;
		ADVENTURE = BukkitAudiences.create(this);

		Constants.LOGO.forEach(getLogger()::info);

		getLogger().info("Loading config...");
		pluginConfig = YamlConfigurations.update(new File(this.getDataFolder(), "config.yml").toPath(), FConfig.class, ConfigProperties.CONFIG);
		npcData = ConfigManager.loadNPCData();

		getLogger().info("Loading commands...");
		commandHandler = BukkitCommandHandler.create(this);

		commandHandler.getAutoCompleter().registerParameterSuggestions(FNPC.class, (args, sender, command) -> npcData.getNpcs().keySet());
		commandHandler.getAutoCompleter().registerSuggestion("npcNames", (args, sender, command) -> npcData.getNpcs().keySet());
		commandHandler.registerValueResolver(FNPC.class, context -> {
			String value = context.pop();
			if (npcData.getNpcs().keySet().stream().noneMatch(npc -> npc.equalsIgnoreCase(value))) {
				if (npcData.getNpcs().values().stream().noneMatch(npc -> npc.getName().equalsIgnoreCase(value))) {
					throw new CommandErrorException("Invalid NPC: &e" + value);
				}
			}
			if (npcData.getNpcs().values().stream().anyMatch(npc -> npc.getName().equalsIgnoreCase(value))) {
				long size = npcData.getNpcs().values().stream()
						.filter(npc -> npc.getName().equalsIgnoreCase(value))
						.count();

				if (size > 1) {
					throw new CommandErrorException("Multiple NPC with the same name '%s', please use ID instead!".formatted(value));
				}

				return npcData.getNpcs().values().stream()
						.filter(npc -> npc.getName().equalsIgnoreCase(value))
						.findFirst().orElseThrow();
			}
			return npcData.getNpcs().get(value);
		});
		commandHandler.registerValueResolver(ConversionPlugin.class, context -> {
			String value = context.pop();
			if (ConversionManager.SupportedPlugin.match(value).isEmpty()) {
				throw new CommandErrorException("Invalid Plugin: &e" + value);
			}
			return ConversionManager.SupportedPlugin.match(value).get();
		});

		commandHandler.setHelpWriter((command, actor) -> String.format(" &8• &e/%s %s &7- &f%s", command.getPath().toRealString(), command.getUsage(), command.getDescription()));
		commandHandler.register(new FantasyNPCCommand());
		commandHandler.enableAdventure(ADVENTURE);

		getLogger().info("Loading Holograms...");
		new HologramBridge(this, true);
		if (!isHologramEnabled()) getLogger().warning("No hologram connector were found, hologram feature will be disable.");

		getLogger().info("Loading NPCs...");
		this.npcPlatform = BukkitPlatform.bukkitNpcPlatformBuilder()
				.debug(pluginConfig.isDebug())
				.extension(this)
				.packetFactory(BukkitProtocolAdapter.packetEvents())
				.actionController(builder -> {
					builder.flag(NpcActionController.SPAWN_DISTANCE, pluginConfig.getNpc().getSpawnDistance());
					builder.flag(NpcActionController.IMITATE_DISTANCE, pluginConfig.getNpc().getActionDistance());
					builder.flag(NpcActionController.TAB_REMOVAL_TICKS, pluginConfig.getNpc().getTabListRemoveTicks());
				})
				.build();
		var eventManager = npcPlatform.eventManager();
		eventManager.registerEventHandler(ShowNpcEvent.class, new NPCShowListener());
		eventManager.registerEventHandler(InteractNpcEvent.class, new NPCInteractNpcListener());
		FNPCManager.loadNPC(npcPlatform);

		Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);

		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

		metricsWrapper = new MetricsWrapper(this, 18297);

		getLogger().info("Thank you for using FantasyNPC!");
	}

	@Override
	public void onDisable() {
		FNPCManager.disable(npcPlatform);
		getServer().getMessenger().unregisterOutgoingPluginChannel(this);
	}

	public static void debug(String message) {
		if (FantasyNPC.getInstance().getPluginConfig().isDebug())
			FantasyNPC.getInstance().getLogger().info("[DEBUG] %s".formatted(message));
	}

	public boolean isHologramEnabled() {
		return HologramAPI.getConnector() != null;
	}
}
