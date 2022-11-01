package net.fantasyrealms.fantasynpc.manager;

import com.github.juliarn.npc.NPC;
import com.github.juliarn.npc.NPCPool;
import net.fantasyrealms.fantasynpc.FantasyNPC;
import net.fantasyrealms.fantasynpc.objects.FNPC;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class FNPCManager {

	public static void loadNPC(NPCPool npcPool) {
		FantasyNPC.getInstance().getNpcData().getNpcs().forEach((uuid, npc) -> {
			FantasyNPC.debug("Loading NPC: %s (%s)".formatted(npc.getName(), npc.getUuid()));
			FNPC.toNPC(npc).build(npcPool);
			npcPool.getNpc(npc.getUuid()).ifPresent((npcResult) -> {
				FantasyNPC.debug("NPC [%s] loaded with Entity ID: %s".formatted(npc.getName(), npcResult.getEntityId()));
			});
 		});
	}

	public static void save(NPC npc) {
		Map<String, FNPC> newNPCs = new LinkedHashMap<>(FantasyNPC.getInstance().getNpcData().getNpcs());
		newNPCs.put(npc.getProfile().getUniqueId().toString(), FNPC.fromNPC(npc));
		FantasyNPC.getInstance().getNpcData().setNpcs(newNPCs);
		ConfigManager.reloadNPCData();
	}

	public static void clear() {
		for (NPC npc : FantasyNPC.getInstance().getNpcPool().getNPCs()) {
			FantasyNPC.getInstance().getNpcPool().removeNPC(npc.getEntityId());
		}
		FantasyNPC.getInstance().getNpcData().setNpcs(Collections.emptyMap());
		ConfigManager.reloadNPCData();
	}

	public static boolean remove(String name) {
		try {
			Map<String, FNPC> newNPCs = new LinkedHashMap<>(FantasyNPC.getInstance().getNpcData().getNpcs());
			for (Map.Entry<String, FNPC> npcEntry : newNPCs.entrySet()) {
				String uuid = npcEntry.getKey();
				FNPC npc = npcEntry.getValue();
				if (npc.getName().equalsIgnoreCase(name)) {
					newNPCs.remove(uuid);
					FantasyNPC.getInstance().getNpcPool().getNpc(npc.getUuid()).ifPresent((npcP) -> {
						FantasyNPC.getInstance().getNpcPool().removeNPC(npcP.getEntityId());
						FantasyNPC.debug("Removed NPC [EID: %s]: %s (%s)".formatted(npcP.getEntityId(), npc.getName(), npc.getUuid()));
					});
				}
			}
			FantasyNPC.getInstance().getNpcData().setNpcs(newNPCs);
			ConfigManager.reloadNPCData();
			return true;
		} catch (Throwable ex) {
			FantasyNPC.getInstance().getLogger().warning("Error occurred when deleting NPC: [Please directly report this problem to author with stacktrace]");
			ex.printStackTrace();
			return false;
		}
	}
}
