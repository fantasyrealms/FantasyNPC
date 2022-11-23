package net.fantasyrealms.fantasynpc.conversion.impl;

import com.github.juliarn.npc.NPC;
import net.fantasyrealms.fantasynpc.FantasyNPC;
import net.fantasyrealms.fantasynpc.conversion.ConversionPlugin;
import net.fantasyrealms.fantasynpc.manager.FNPCManager;
import net.fantasyrealms.fantasynpc.objects.FAction;
import net.fantasyrealms.fantasynpc.objects.FActionType;
import net.fantasyrealms.fantasynpc.objects.FEquip;
import net.fantasyrealms.fantasynpc.objects.FEquipType;
import net.fantasyrealms.fantasynpc.objects.FHolo;
import net.fantasyrealms.fantasynpc.objects.FNPC;
import net.fantasyrealms.fantasynpc.util.LocationSerilization;
import net.fantasyrealms.fantasynpc.util.NPCUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ServerNPCConversion extends ConversionPlugin {

	@Override
	public List<FNPC> getNPCs() {
		List<FNPC> nps = new ArrayList<>();
		YamlConfiguration config = YamlConfiguration.loadConfiguration(getNPCsFile());
		ConfigurationSection section = config.getConfigurationSection("NPCs");

		if (section == null)
			return nps;

		section.getKeys(false)
				.forEach(key -> {
					String path = key + ".";
					String skin = section.getString(path + "Skin");
					Location location = LocationSerilization.deserialize(section.getString(path + "Location"));
					FHolo holo = new FHolo(section.getDouble(path + "Hologram.yHeight"), section.getStringList(path + "Hologram.lines"));
					List<FAction> actions = new ArrayList<>();
					section.getStringList(path + "Action").forEach(str -> {
						String[] value = str.split("\\|")[2].split(":");
						String type = value[0];
						String execute = value[1];
						if (type.contains("cmd")) {
							actions.add(new FAction(FActionType.COMMAND, execute));
						}
						if (type.contains("msg")) {
							actions.add(new FAction(FActionType.MESSAGE, execute));
						}
						if (type.contains("server")) {
							actions.add(new FAction(FActionType.SERVER, execute));
						}
					});

					try {
						Integer.parseInt(skin);
						skin = "m:" + skin;
					} catch (Throwable ignored) {
					}

					if (location.getWorld() == null) location.setWorld(Bukkit.getWorlds().get(0));

					NPC npc = NPCUtils.createNPC(location, key, skin).join();
					FNPC fNpc = FNPC.fromNPC(npc);
					fNpc.setHologram(holo);
					fNpc.setActions(actions);

					ConfigurationSection settings = section.getConfigurationSection(path + "Settings");
					if (settings != null) {
						settings.getKeys(false).forEach(settingsKey -> {
							String sPath = key + ".";
							boolean lookClose = settings.getBoolean(sPath + "lookClose", false);

							ConfigurationSection inventory = section.getConfigurationSection(sPath + "Inventory");
							List<FEquip> equip = new ArrayList<>();
							if (inventory != null) {
								inventory.getKeys(false).forEach(inv -> {
									ItemStack item = inventory.getItemStack(key + ".");
									if (item == null) return;
									if (inv.contains("HEAD")) {
										equip.add(new FEquip(FEquipType.HELMET, item));
									}
									if (inv.contains("CHEST")) {
										equip.add(new FEquip(FEquipType.CHEST_PLATE, item));
									}
									if (inv.contains("LEGS")) {
										equip.add(new FEquip(FEquipType.LEGGINGS, item));
									}
									if (inv.contains("FEET")) {
										equip.add(new FEquip(FEquipType.BOOTS, item));
									}
									if (inv.contains("MAINHAND")) {
										equip.add(new FEquip(FEquipType.MAIN_HAND, item));
									}
									if (inv.contains("OFFHAND")) {
										equip.add(new FEquip(FEquipType.OFF_HAND, item));
									}
								});
							}
							fNpc.setLookAtPlayer(lookClose);
							fNpc.setEquipment(equip);
						});
					}
					nps.add(fNpc);
					FNPCManager.save(fNpc);
				});

		FNPCManager.reload(FantasyNPC.getInstance().getNpcPool());
		return nps;
	}

	@Override
	public File getNPCsFile() {
		return new File(this.getPluginsFolder(), "Database.yml");
	}

	@Override
	public String getPluginName() {
		return "ServerNPC";
	}
}
