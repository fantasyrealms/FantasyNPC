package net.fantasyrealms.fantasynpc.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter @Setter
public class FConfig {

	@Comment("Debug mode? It will show more details on the console if enabled.")
	private boolean debug = false;

	@Comment("All the settings for NPCs")
	private NPC npc = new NPC();

	@Configuration
	@Getter @Setter
	public static class NPC {
		@Comment("The distance in which NPCs are spawned for players.")
		private int spawnDistance = 60;

		@Comment("The distance in which NPC actions are displayed for players.")
		private int actionDistance = 60;

		@Comment("The time in ticks after which the NPC will be removed from the players tab.")
		private int tabListRemoveTicks = 60;
	}

	@Comment("Hologram settings for NPCs")
	private Hologram hologram = new Hologram();

	@Configuration
	@Getter @Setter
	public static class Hologram {
		@Comment("The distance in which Hologram are visible for players.")
		private int spawnDistance = 60;

		@Comment("The minimum distance in which will trigger hit actions.")
		private float minHitDistance = 0.5f;

		@Comment("The maximum distance in which will trigger hit actions.")
		private float maxHitDistance = 5f;
	}
}
