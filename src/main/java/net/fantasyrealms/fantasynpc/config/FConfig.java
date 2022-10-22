package net.fantasyrealms.fantasynpc.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter @Setter
public class FConfig {

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
