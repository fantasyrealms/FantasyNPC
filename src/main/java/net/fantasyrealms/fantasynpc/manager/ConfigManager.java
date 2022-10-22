package net.fantasyrealms.fantasynpc.manager;

import de.exlll.configlib.YamlConfigurations;
import net.fantasyrealms.fantasynpc.FantasyNPC;
import net.fantasyrealms.fantasynpc.config.NPCData;
import net.fantasyrealms.fantasynpc.constants.ConfigProperties;

import java.io.File;

public class ConfigManager {

	public static NPCData loadNPCData() {
		return YamlConfigurations.update(new File(FantasyNPC.getInstance().getDataFolder(), "npcs.yml").toPath(), NPCData.class, ConfigProperties.NPC_DATA);
	}

	public static void reloadNPCData() {
		YamlConfigurations.save(new File(FantasyNPC.getInstance().getDataFolder(), "npcs.yml").toPath(), NPCData.class, FantasyNPC.getInstance().getNpcData(), ConfigProperties.NPC_DATA);
		YamlConfigurations.update(new File(FantasyNPC.getInstance().getDataFolder(), "npcs.yml").toPath(), NPCData.class, ConfigProperties.NPC_DATA);
	}
}
