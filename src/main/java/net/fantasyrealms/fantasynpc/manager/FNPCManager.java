package net.fantasyrealms.fantasynpc.manager;

import com.github.juliarn.npc.NPC;
import net.fantasyrealms.fantasynpc.FantasyNPC;
import net.fantasyrealms.fantasynpc.objects.FNPC;
import org.bukkit.Bukkit;

import java.util.LinkedHashMap;
import java.util.Map;

public class FNPCManager {

	public static void save(NPC npc) {
		Map<String, FNPC> newNPCs = new LinkedHashMap<>(FantasyNPC.getInstance().getNpcData().getNpcs());
		Bukkit.broadcastMessage(npc.getProfile().getProperties().toString());
		newNPCs.put(npc.getProfile().getUniqueId().toString(), FNPC.fromNPC(npc));
		FantasyNPC.getInstance().getNpcData().setNpcs(newNPCs);
		ConfigManager.reloadNPCData();
	}
}
