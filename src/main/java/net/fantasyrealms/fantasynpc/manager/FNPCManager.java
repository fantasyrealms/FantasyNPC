package net.fantasyrealms.fantasynpc.manager;

import com.cryptomorin.xseries.XMaterial;
import com.github.juliarn.npc.NPC;
import com.github.juliarn.npc.NPCPool;
import com.github.juliarn.npc.profile.Profile;
import gg.optimalgames.hologrambridge.HologramAPI;
import gg.optimalgames.hologrambridge.hologram.Hologram;
import lombok.Getter;
import net.fantasyrealms.fantasynpc.FantasyNPC;
import net.fantasyrealms.fantasynpc.constants.UpdateType;
import net.fantasyrealms.fantasynpc.objects.FAction;
import net.fantasyrealms.fantasynpc.objects.FEquip;
import net.fantasyrealms.fantasynpc.objects.FNPC;
import net.fantasyrealms.fantasynpc.util.PlayerUtils;
import net.fantasyrealms.fantasynpc.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FNPCManager {

	@Getter
	private static Map<UUID, Hologram> holograms = new HashMap<>();

	public static void loadNPC(NPCPool npcPool) {
		FantasyNPC.getInstance().getNpcData().getNpcs().forEach((key, npc) -> {
			loadNPC(npc, npcPool);
		});
	}

	public static NPC loadNPC(FNPC fNPC, NPCPool npcPool) {
		FantasyNPC.debug("Loading NPC: %s (%s)".formatted(fNPC.getName(), fNPC.getUuid()));
		NPC npc = FNPC.toNPC(fNPC).build(npcPool);
		npcPool.getNpc(fNPC.getUuid()).ifPresent((npcResult) -> {
			FantasyNPC.debug("NPC [%s] loaded with Entity ID: %s".formatted(fNPC.getName(), npcResult.getEntityId()));
		});
		if (FantasyNPC.getInstance().isHologramEnabled() && !fNPC.getHologram().getLines().isEmpty()) {
			FantasyNPC.debug("Loading NPC Holograms: %s with %s line(s)".formatted(fNPC.getName(), fNPC.getHologram().getLines().size()));
			Hologram holo = HologramAPI.getConnector().createHologram(fNPC.getLocation().clone().add(0, fNPC.getHologram().getYHeight(), 0));
			fNPC.getHologram().getLines().forEach(l -> {
				if (l.startsWith("ICON:")) {
					ItemStack it = XMaterial.matchXMaterial(l.replace("ICON:", "")).orElse(XMaterial.BARRIER).parseItem();
					holo.appendItemLine(it);
					return;
				}
				holo.appendTextLine(l);
			});
			holograms.put(fNPC.getUuid(), holo);
			FantasyNPC.debug("NPC [%s] Hologram loaded: %s line(s) | %s".formatted(fNPC.getName(), holo.size(), Utils.pettyLocation(holo.getLocation())));
		}
		return npc;
	}

	public static void save(NPC npc) {
		Map<String, FNPC> newNPCs = new LinkedHashMap<>(FantasyNPC.getInstance().getNpcData().getNpcs());
		newNPCs.put(npc.getProfile().getUniqueId().toString().replace("-", "").substring(0, 8), FNPC.fromNPC(npc));
		FantasyNPC.getInstance().getNpcData().setNpcs(newNPCs);
		ConfigManager.saveNPCData();
		updateAllPlayerScoreboard();
	}

	public static void addNPCActions(FNPC fNpc, FAction action) {
		fNpc.getActions().add(action);
		updateNPC(fNpc);
	}

	public static FAction removeNPCAction(FNPC fNpc, int slot) {
		List<FAction> actions = fNpc.getActions();
		if (slot > actions.size()) return null;
		FAction removedAction = actions.remove(slot);

		updateNPC(fNpc);
		return removedAction;
	}

	public static void updateEquip(FNPC fNpc, FEquip equip) {
		fNpc.getEquipment().removeIf(e -> e.getType() == equip.getType());
		fNpc.getEquipment().add(equip);
		updateNPC(fNpc);
	}

	public static FNPC updateNPC(FNPC fNpc) {
		NPCPool npcPool = FantasyNPC.getInstance().getNpcPool();
		Map<String, FNPC> newNPCs = new LinkedHashMap<>(FantasyNPC.getInstance().getNpcData().getNpcs());

		removeFromPool(fNpc.getUuid());
		removeHolograms(fNpc.getUuid());
		updateAllPlayerScoreboard();

		NPC npc = loadNPC(fNpc, npcPool);
		FNPC newFNPC = FNPC.fromExist(fNpc, npc);

		for (Map.Entry<String, FNPC> npcEntry : FantasyNPC.getInstance().getNpcData().getNpcs().entrySet()) {
			FNPC oldNPC = npcEntry.getValue();
			if (oldNPC.getUuid() == newFNPC.getUuid()) {
				newNPCs.replace(npcEntry.getKey(), newFNPC);
			}
		}

		FantasyNPC.getInstance().getNpcData().setNpcs(newNPCs);
		ConfigManager.saveNPCData();

		return newFNPC;
	}

	public static FNPC updateNPC(FNPC fNpc, UpdateType updateType) {
		NPC.Builder npcBuilder = FNPC.toNPC(fNpc);

		switch (updateType) {
			case LOOK_AT_PLAYER -> npcBuilder.lookAtPlayer(!fNpc.isLookAtPlayer());
			case IMITATE_PLAYER -> npcBuilder.imitatePlayer(!fNpc.isImitatePlayer());
			case LOCATION -> npcBuilder.location(fNpc.getLocation());
			case NAME_TAG -> fNpc.setShowNameTag(!fNpc.isShowNameTag());
		}

		return updateNPC(fNpc);
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
		holograms.values().forEach(Hologram::delete);
		holograms.clear();
		FantasyNPC.getInstance().getNpcData().setNpcs(Collections.emptyMap());
		ConfigManager.saveNPCData();
		updateAllPlayerScoreboard();
	}

	public static void reload(NPCPool npcPool) {
		for (NPC npc : FantasyNPC.getInstance().getNpcPool().getNPCs()) {
			FantasyNPC.getInstance().getNpcPool().removeNPC(npc.getEntityId());
		}
		holograms.values().forEach(Hologram::delete);
		holograms.clear();
		ConfigManager.reloadNPCData();
		loadNPC(npcPool);
	}

	public static void disable(NPCPool npcPool) {
		try {
			for (NPC npc : FantasyNPC.getInstance().getNpcPool().getNPCs()) {
				FantasyNPC.getInstance().getNpcPool().removeNPC(npc.getEntityId());
			}
		} catch (Throwable ignored) {
		}
		holograms.values().forEach(Hologram::delete);
		holograms.clear();
	}

	/**
	 * Remove hologram from NPC uuid
	 *
	 * @param uuid The NPC UUID
	 */
	public static void removeHolograms(UUID uuid) {
		Hologram holo = holograms.remove(uuid);
		if (holo != null) {
			holo.delete();
		}
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
