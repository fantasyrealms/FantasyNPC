package net.fantasyrealms.fantasynpc.manager;

import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import net.fantasyrealms.fantasynpc.FantasyNPC;
import net.fantasyrealms.fantasynpc.config.FConfig;
import net.fantasyrealms.fantasynpc.config.NPCData;
import net.fantasyrealms.fantasynpc.constants.ConfigProperties;

import java.io.File;

import static net.fantasyrealms.fantasynpc.constants.Constants.CONFIG_FILE_NAME;
import static net.fantasyrealms.fantasynpc.constants.Constants.NPCDATA_FILE_NAME;

public class ConfigManager {

	public static NPCData loadNPCData() {
		return update(NPCDATA_FILE_NAME, NPCData.class, ConfigProperties.NPC_DATA);
	}

	public static void saveNPCData() {
		YamlConfigurations.save(new File(FantasyNPC.getInstance().getDataFolder(), NPCDATA_FILE_NAME).toPath(), NPCData.class, FantasyNPC.getInstance().getNpcData(), ConfigProperties.NPC_DATA);
		update(NPCDATA_FILE_NAME, NPCData.class, ConfigProperties.NPC_DATA);
	}

	public static void reloadNPCData() {
		YamlConfigurations.load(new File(FantasyNPC.getInstance().getDataFolder(), NPCDATA_FILE_NAME).toPath(), NPCData.class, ConfigProperties.NPC_DATA);
		update(NPCDATA_FILE_NAME, NPCData.class, ConfigProperties.NPC_DATA);
	}

	public static void reloadConfig() {
		YamlConfigurations.save(new File(FantasyNPC.getInstance().getDataFolder(), CONFIG_FILE_NAME).toPath(), FConfig.class, FantasyNPC.getInstance().getPluginConfig(), ConfigProperties.CONFIG);
		update(CONFIG_FILE_NAME, FConfig.class, ConfigProperties.CONFIG);
	}

	private static <T> T update(String fileName, Class<T> configClass, YamlConfigurationProperties configProperties) {
		return YamlConfigurations.update(new File(FantasyNPC.getInstance().getDataFolder(), fileName).toPath(), configClass, configProperties);
	}
}
