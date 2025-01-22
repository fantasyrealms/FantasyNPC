package net.fantasyrealms.fantasynpc.manager;

import com.cryptomorin.xseries.XMaterial;
import com.github.juliarn.npclib.api.Npc;
import com.github.juliarn.npclib.api.Platform;
import com.github.juliarn.npclib.api.profile.Profile;
import com.github.juliarn.npclib.bukkit.util.BukkitPlatformUtil;
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
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FNPCManager {

	@Getter
	private static Map<UUID, Hologram> holograms = new HashMap<>();

	public static void loadNPC(Platform<World, Player, ItemStack, Plugin> platform) {
		FantasyNPC.getInstance().getNpcData().getNpcs().forEach((key, npc) -> {
			loadNPC(npc, platform);
		});
	}

	public static Npc<World, Player, ItemStack, Plugin> loadNPC(FNPC fNPC, Platform<World, Player, ItemStack, Plugin> platform) {
		FantasyNPC.debug("Loading NPC: %s (%s)".formatted(fNPC.getName(), fNPC.getUuid()));
		var npc = FNPC.toNPC(fNPC).buildAndTrack();
		var newNpc = platform.npcTracker().npcByUniqueId(fNPC.getUuid());
		if (newNpc != null) {
			FantasyNPC.debug("NPC [%s] loaded with Entity ID: %s".formatted(fNPC.getName(), newNpc.entityId()));
		}
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

	public static void save(Npc<World, Player, ItemStack, Plugin> npc) {
		Map<String, FNPC> newNPCs = new LinkedHashMap<>(FantasyNPC.getInstance().getNpcData().getNpcs());
		newNPCs.put(npc.profile().uniqueId().toString().replace("-", "").substring(0, 8), FNPC.fromNPC(npc));
		FantasyNPC.getInstance().getNpcData().setNpcs(newNPCs);
		ConfigManager.saveNPCData();
		updateAllPlayerScoreboard();
	}

	public static void save(FNPC fNpc) {
		Map<String, FNPC> newNPCs = new LinkedHashMap<>(FantasyNPC.getInstance().getNpcData().getNpcs());
		newNPCs.put(fNpc.getUuid().toString().replace("-", "").substring(0, 8), fNpc);
		FantasyNPC.getInstance().getNpcData().setNpcs(newNPCs);
		ConfigManager.saveNPCData();
		updateAllPlayerScoreboard();
	}

	public static void addNPCActions(FNPC fNpc, FAction action) {
		fNpc.getActions().add(action);
//		updateNPC(fNpc);
	}

	public static FAction removeNPCAction(FNPC fNpc, int slot) {
		List<FAction> actions = fNpc.getActions();
		if (slot > actions.size()) return null;
		FAction removedAction = actions.remove(slot);

//		updateNPC(fNpc);
		return removedAction;
	}

	public static void updateEquip(FNPC fNpc, FEquip equip) {
		FNPC npc = FNPC.fromExist(fNpc);
		npc.getEquipment().removeIf(e -> e.getType() == equip.getType());
		npc.getEquipment().add(equip);
		updateNPC(npc);
	}

	public static FNPC updateNPC(FNPC fNpc) {
		var npcPlatform = FantasyNPC.getInstance().getNpcPlatform();
		Map<String, FNPC> newNPCs = new LinkedHashMap<>(FantasyNPC.getInstance().getNpcData().getNpcs());

		removeFromPool(fNpc.getUuid());
		removeHolograms(fNpc.getUuid());
		updateAllPlayerScoreboard();

		var npc = loadNPC(fNpc, npcPlatform);
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
		var npcBuilder = FNPC.toNPC(fNpc);

		switch (updateType) {
			case LOOK_AT_PLAYER -> fNpc.setLookAtPlayer(!fNpc.isLookAtPlayer());
			case IMITATE_PLAYER -> fNpc.setImitatePlayer(!fNpc.isImitatePlayer());
			case LOCATION -> npcBuilder.position(BukkitPlatformUtil.positionFromBukkitLegacy(fNpc.getLocation()));
			case NAME_TAG -> fNpc.setShowNameTag(!fNpc.isShowNameTag());
			case NAME -> fNpc.setName(fNpc.getName());
		}

		return updateNPC(fNpc);
	}

	public static void updateProfile(FNPC fNpc, Profile.Resolved profile) {
		var npcPlatform = FantasyNPC.getInstance().getNpcPlatform();
		Map<String, FNPC> newNPCs = new LinkedHashMap<>(FantasyNPC.getInstance().getNpcData().getNpcs());
		var npcBuilder = FNPC.toNPC(fNpc);
		npcBuilder.profile(profile);
		var npc = npcBuilder.buildAndTrack();

		var npcP = npcPlatform.npcTracker().npcByUniqueId(fNpc.getUuid());
		if (npcP != null) {
			npcP.unlink();
			FantasyNPC.debug("[NPC UPDATE] Removed NPC [EID: %s]: %s (%s)".formatted(npcP.entityId(), fNpc.getName(), fNpc.getUuid()));
		}

		for (Map.Entry<String, FNPC> npcEntry : FantasyNPC.getInstance().getNpcData().getNpcs().entrySet()) {
			FNPC oldNPC = npcEntry.getValue();
			if (oldNPC.getUuid() == fNpc.getUuid()) {
				newNPCs.replace(npcEntry.getKey(), FNPC.fromExist(fNpc, npc));
			}
		}
		FantasyNPC.getInstance().getNpcData().setNpcs(newNPCs);
		ConfigManager.saveNPCData();
	}

	public static void clear() {
		for (Npc<World, Player, ItemStack, Plugin> npc : FantasyNPC.getInstance().getNpcPlatform().npcTracker().trackedNpcs()) {
			npc.unlink();
		}
		holograms.values().forEach(Hologram::delete);
		holograms.clear();
		FantasyNPC.getInstance().getNpcData().setNpcs(Collections.emptyMap());
		ConfigManager.saveNPCData();
		updateAllPlayerScoreboard();
	}

	public static void reload(Platform<World, Player, ItemStack, Plugin> platform) {
		for (Npc<World, Player, ItemStack, Plugin> npc : platform.npcTracker().trackedNpcs()) {
			npc.unlink();
		}
		holograms.values().forEach(Hologram::delete);
		holograms.clear();
		ConfigManager.reloadNPCData();
		loadNPC(platform);
	}

	public static void disable(Platform<World, Player, ItemStack, Plugin> platform) {
		try {
			for (Npc<World, Player, ItemStack, Plugin> npc : platform.npcTracker().trackedNpcs()) {
				npc.unlink();
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
		var npc = FantasyNPC.getInstance().getNpcPlatform().npcTracker().npcByUniqueId(uuid);
		if (npc == null) return;
		npc.unlink();
		FantasyNPC.debug("Removed NPC [EID: %s]: %s (%s)".formatted(npc.entityId(), npc.profile().name(), npc.profile().uniqueId()));
	}

	public static boolean removeAndClearData(FNPC fNpc) {
		try {
			Map<String, FNPC> newNPCs = new HashMap<>(FantasyNPC.getInstance().getNpcData().getNpcs());
			for (Map.Entry<String, FNPC> npcEntry : FantasyNPC.getInstance().getNpcData().getNpcs().entrySet()) {
				String key = npcEntry.getKey();
				FNPC npc = npcEntry.getValue();
				if (fNpc.getUuid() == npc.getUuid()) {
					newNPCs.remove(key);
					removeHolograms(npc.getUuid());
					removeFromPool(fNpc.getUuid());
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
