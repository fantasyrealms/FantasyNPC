package net.fantasyrealms.fantasynpc.manager;

import com.github.juliarn.npc.NPC;
import com.github.juliarn.npc.NPCPool;
import com.github.juliarn.npc.profile.Profile;
import net.fantasyrealms.fantasynpc.FantasyNPC;
import net.fantasyrealms.fantasynpc.constants.UpdateType;
import net.fantasyrealms.fantasynpc.objects.FAction;
import net.fantasyrealms.fantasynpc.objects.FNPC;
import net.fantasyrealms.fantasynpc.util.PlayerUtils;
import org.bukkit.Bukkit;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
		newNPCs.put(npc.getProfile().getUniqueId().toString().replace("-", "").substring(0, 8), FNPC.fromNPC(npc));
		FantasyNPC.getInstance().getNpcData().setNpcs(newNPCs);
		ConfigManager.saveNPCData();
		updateAllPlayerScoreboard();
	}

	public static void addNPCActions(FNPC fNpc, FAction action) {
		Map<String, FNPC> newNPCs = new LinkedHashMap<>(FantasyNPC.getInstance().getNpcData().getNpcs());
		fNpc.getActions().add(action);

		for (Map.Entry<String, FNPC> npcEntry : FantasyNPC.getInstance().getNpcData().getNpcs().entrySet()) {
			FNPC oldNPC = npcEntry.getValue();
			if (oldNPC.getUuid() == fNpc.getUuid()) {
				newNPCs.replace(npcEntry.getKey(), fNpc);
			}
		}

		FantasyNPC.getInstance().getNpcData().setNpcs(newNPCs);
		ConfigManager.saveNPCData();
	}

	public static FAction removeNPCAction(FNPC fNpc, int slot) {
		Map<String, FNPC> newNPCs = new LinkedHashMap<>(FantasyNPC.getInstance().getNpcData().getNpcs());
		List<FAction> actions = fNpc.getActions();
		if (slot > actions.size()) return null;
		FAction removedAction = actions.remove(slot);

		for (Map.Entry<String, FNPC> npcEntry : FantasyNPC.getInstance().getNpcData().getNpcs().entrySet()) {
			FNPC oldNPC = npcEntry.getValue();
			if (oldNPC.getUuid() == fNpc.getUuid()) {
				newNPCs.replace(npcEntry.getKey(), fNpc);
			}
		}

		FantasyNPC.getInstance().getNpcData().setNpcs(newNPCs);
		ConfigManager.saveNPCData();
		return removedAction;
	}

	public static FNPC updateNPC(FNPC fNpc, UpdateType updateType) {
		NPCPool npcPool = FantasyNPC.getInstance().getNpcPool();
		Map<String, FNPC> newNPCs = new LinkedHashMap<>(FantasyNPC.getInstance().getNpcData().getNpcs());
		NPC.Builder npcBuilder = FNPC.toNPC(fNpc);

		switch(updateType) {
			case LOOK_AT_PLAYER -> npcBuilder.lookAtPlayer(!fNpc.isLookAtPlayer());
			case IMITATE_PLAYER -> npcBuilder.imitatePlayer(!fNpc.isImitatePlayer());
			case LOCATION -> npcBuilder.location(fNpc.getLocation());
		}

		removeFromPool(fNpc.getUuid());
		NPC npc = npcBuilder.build(npcPool);
		FNPC newFNPC = FNPC.fromExist(fNpc, npc);

		for (Map.Entry<String, FNPC> npcEntry : FantasyNPC.getInstance().getNpcData().getNpcs().entrySet()) {
			FNPC oldNPC = npcEntry.getValue();
			if (oldNPC.getName().equalsIgnoreCase(fNpc.getName())) {
				newNPCs.replace(npcEntry.getKey(), newFNPC);
			}
		}

		FantasyNPC.getInstance().getNpcData().setNpcs(newNPCs);
		ConfigManager.saveNPCData();

		return newFNPC;
	}

	public static void updateProfile(FNPC fNpc, Profile profile) {
		NPCPool npcPool = FantasyNPC.getInstance().getNpcPool();
		Map<String, FNPC> newNPCs = new LinkedHashMap<>(FantasyNPC.getInstance().getNpcData().getNpcs());
		NPC npc = FNPC.toNPC(fNpc).profile(profile).build(npcPool);

		npcPool.getNpc(fNpc.getUuid()).ifPresent((npcP) -> {
			npcPool.removeNPC(npcP.getEntityId());
			FantasyNPC.debug("[NPC UPDATE] Removed NPC [EID: %s]: %s (%s)".formatted(npcP.getEntityId(), fNpc.getName(), fNpc.getUuid()));
		});

		for (Map.Entry<String, FNPC> npcEntry : FantasyNPC.getInstance().getNpcData().getNpcs().entrySet()) {
			FNPC oldNPC = npcEntry.getValue();
			if (oldNPC.getName().equalsIgnoreCase(fNpc.getName())) {
				newNPCs.replace(npcEntry.getKey(), FNPC.fromExist(fNpc, npc));
			}
		}
		FantasyNPC.getInstance().getNpcData().setNpcs(newNPCs);
		ConfigManager.saveNPCData();
	}

	public static void clear() {
		for (NPC npc : FantasyNPC.getInstance().getNpcPool().getNPCs()) {
			FantasyNPC.getInstance().getNpcPool().removeNPC(npc.getEntityId());
		}
		FantasyNPC.getInstance().getNpcData().setNpcs(Collections.emptyMap());
		ConfigManager.saveNPCData();
		updateAllPlayerScoreboard();
	}

	public static void reload(NPCPool npcPool) {
		for (NPC npc : FantasyNPC.getInstance().getNpcPool().getNPCs()) {
			FantasyNPC.getInstance().getNpcPool().removeNPC(npc.getEntityId());
		}
		ConfigManager.reloadNPCData();
		loadNPC(npcPool);
	}

	public static void removeFromPool(UUID uuid) {
		FantasyNPC.getInstance().getNpcPool().getNpc(uuid).ifPresent((npcP) -> {
			FantasyNPC.getInstance().getNpcPool().removeNPC(npcP.getEntityId());
			FantasyNPC.debug("Removed NPC [EID: %s]: %s (%s)".formatted(npcP.getEntityId(), npcP.getProfile().getName(), npcP.getProfile().getUniqueId()));
		});
	}

	public static boolean removeAndClearData(String name) {
		try {
			Map<String, FNPC> newNPCs = new HashMap<>(FantasyNPC.getInstance().getNpcData().getNpcs());
			for (Map.Entry<String, FNPC> npcEntry : FantasyNPC.getInstance().getNpcData().getNpcs().entrySet()) {
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
			ConfigManager.saveNPCData();
			updateAllPlayerScoreboard();
			return true;
		} catch (Throwable ex) {
			FantasyNPC.getInstance().getLogger().warning("Error occurred when deleting NPC: [Please directly report this problem to author with stacktrace]");
			ex.printStackTrace();
			return false;
		}
	}

	public static void updateAllPlayerScoreboard() {
		Bukkit.getScheduler().runTask(FantasyNPC.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(PlayerUtils::setNPCScoreboard));
	}
}
