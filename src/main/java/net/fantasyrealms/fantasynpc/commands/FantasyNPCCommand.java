package net.fantasyrealms.fantasynpc.commands;

import net.fantasyrealms.fantasynpc.FantasyNPC;
import net.fantasyrealms.fantasynpc.constants.Constants;
import net.fantasyrealms.fantasynpc.constants.UpdateType;
import net.fantasyrealms.fantasynpc.conversion.ConversionManager;
import net.fantasyrealms.fantasynpc.conversion.ConversionPlugin;
import net.fantasyrealms.fantasynpc.manager.ConfigManager;
import net.fantasyrealms.fantasynpc.manager.FNPCManager;
import net.fantasyrealms.fantasynpc.objects.FAction;
import net.fantasyrealms.fantasynpc.objects.FActionType;
import net.fantasyrealms.fantasynpc.objects.FEquip;
import net.fantasyrealms.fantasynpc.objects.FEquipType;
import net.fantasyrealms.fantasynpc.objects.FHolo;
import net.fantasyrealms.fantasynpc.objects.FNPC;
import net.fantasyrealms.fantasynpc.util.NPCUtils;
import net.fantasyrealms.fantasynpc.util.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;
import revxrsal.commands.annotation.AutoComplete;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.help.CommandHelp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.fantasyrealms.fantasynpc.FantasyNPC.MINIMESSAGE;
import static net.fantasyrealms.fantasynpc.util.Utils.LEGACY_SERIALIZER;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.event.ClickEvent.openUrl;
import static revxrsal.commands.util.Strings.colorize;

@Command({"fantasynpc", "npc"})
@CommandPermission("fantasynpc.admin")
public class FantasyNPCCommand {

	@Default
	@Description("FantasyNPC commands list")
	public void help(BukkitCommandActor actor, CommandHelp<String> helpEntries, @Default("1") int page) {
		Utils.buildCommandHelp(helpEntries, page, null).forEach(actor::reply);
	}

	@Subcommand({"delete", "remove"})
	@Description("Delete a npc")
	@Usage("<npc>")
	public void deleteNPC(BukkitCommandActor actor, FNPC npc, @Default("false") Boolean showList) {
		if (FNPCManager.removeAndClearData(npc)) {
			actor.reply("&aNPC &f[%s] &ahas been successfully deleted!".formatted(npc.getName()));
			if (showList && actor.isPlayer()) actor.getAsPlayer().performCommand("npc list");
		} else {
			actor.reply("&cAn error happened while deleting the NPC &f[%s]&c, please check console for more info!".formatted(npc.getName()));
		}
	}

	@Subcommand({"skin"})
	@Description("Change NPC skin")
	@Usage("<npc> <ID/m:<mineskinUUID>/https://minesk.in/xxx>")
	public void changeSkin(BukkitCommandActor actor, FNPC npc, @Named("skin ID / Mineskin URL/UUID") String skin) {
		NPCUtils.changeNPCSkin(npc, skin)
				.whenComplete((result, throwable) -> {
					if (throwable != null) {
						actor.reply("&cAn error happened while deleting the NPC &f[%s]&c, please check console for more info!".formatted(npc.getName()));
						throwable.printStackTrace();
						return;
					}
					actor.reply("&aNPC &f[%s] &askin has been changed to &f%s&a!".formatted(npc.getName(), skin));
					actor.reply(text("▍ Skin still not visible? Click here to reload all the NPCs.")
							.color(NamedTextColor.RED)
							.decorate(TextDecoration.BOLD)
							.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/npc reloadnpc"))
							.hoverEvent(text("/npc reloadnpc").color(NamedTextColor.YELLOW)));
				});
	}

	@Subcommand({"clear"})
	@Description("Clear and delete **ALL** the saved NPCs")
	public void clearNPC(BukkitCommandActor actor) {
		FNPCManager.clear();
		actor.reply("&cAll the NPCs data has been cleared.");
	}

	@Subcommand({"teleport"})
	@Description("Teleport you to the NPC")
	@Usage("<npc>")
	public void teleport(BukkitCommandActor actor, FNPC fNpc) {
		actor.requirePlayer();
		actor.getAsPlayer().teleport(fNpc.getLocation());
		actor.reply("&aTeleported to &f%s &7[%s]".formatted(fNpc.getName(), Utils.pettyLocation(fNpc.getLocation())));
	}

	@Subcommand({"location"})
	@Description("Change NPC location to your current location")
	@Usage("<npc>")
	public void location(BukkitCommandActor actor, FNPC fNpc) {
		actor.requirePlayer();
		fNpc.setLocation(actor.getAsPlayer().getLocation());
		FNPC npc = FNPCManager.updateNPC(fNpc, UpdateType.LOCATION);
		actor.reply("&aUpdated &f%s &alocation to &f%s".formatted(npc.getName(), Utils.pettyLocation(npc.getLocation())));
	}

	@Subcommand({"lookAtPlayer"})
	@Description("Toggles whether NPCs look at the player")
	@Usage("<npc>")
	public void lookAtPlayer(BukkitCommandActor actor, FNPC fNpc) {
		FNPC npc = FNPCManager.updateNPC(fNpc, UpdateType.LOOK_AT_PLAYER);
		actor.reply("&f%s %s".formatted(npc.getName(), npc.isLookAtPlayer() ? "&anow will look at the player!" : "&cnow will no longer look at the player."));
	}

	@Subcommand({"imitatePlayer"})
	@Description("Toggles whether NPCs imitate the player")
	@Usage("<npc>")
	public void imitatePlayer(BukkitCommandActor actor, FNPC fNpc) {
		FNPC npc = FNPCManager.updateNPC(fNpc, UpdateType.IMITATE_PLAYER);
		actor.reply("&f%s %s".formatted(npc.getName(), npc.isImitatePlayer() ? "&anow will now imitate the player!" : "&cnow will no longer imitate the player."));
	}

	@Subcommand({"nametag"})
	@Description("Toggles NPCs nametag visibility")
	@Usage("<npc>")
	public void nameTag(BukkitCommandActor actor, FNPC fNpc) {
		FNPC npc = FNPCManager.updateNPC(fNpc, UpdateType.NAME_TAG);
		actor.reply("&f%s %s".formatted(npc.getName(), npc.isShowNameTag() ? "&anametag now will be visible!" : "&cnametag now will no longer be visible."));
	}

	@Subcommand({"list"})
	@Description("List all the NPCs")
	public void list(BukkitCommandActor actor) {
		Map<String, FNPC> fNpc = FantasyNPC.getInstance().getNpcData().getNpcs();
		List<Component> list = new ArrayList<>();
		list.add(LEGACY_SERIALIZER.deserialize("&8&m----------------------------------------"));
		list.add(LEGACY_SERIALIZER.deserialize("&b&lFantasyNPC &f(v%s) &7- &fTotal &9%s &fnps".formatted(Constants.VERSION, fNpc.size())));
		if (fNpc.size() > 0) {
			list.add(LEGACY_SERIALIZER.deserialize("&eHover for more info!"));
			for (Map.Entry<String, FNPC> entry : fNpc.entrySet()) {
				String key = entry.getKey();
				FNPC npc = entry.getValue();
				list.add(textOfChildren(
						text("[X]", NamedTextColor.RED, TextDecoration.BOLD)
								.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/npc delete %s true".formatted(key)))
								.hoverEvent(text("Click here to delete npc %s (%s)".formatted(npc.getName(), key), NamedTextColor.RED, TextDecoration.BOLD)),
						MINIMESSAGE.deserialize(" <green>%s <gray>(%s)</gray> <dark_gray>| <gray>%s".formatted(npc.getName(), npc.getKey(),
										Utils.pettyLocationShort(npc.getLocation())))
								.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/npc teleport %s".formatted(key)))
								.hoverEvent(textOfChildren(
										text("UUID: ", NamedTextColor.GRAY),
										text(npc.getUuid().toString(), NamedTextColor.WHITE),
										newline(),
										text("Key: ", NamedTextColor.GRAY),
										text(key, NamedTextColor.WHITE),
										newline(),
										text("Name: ", NamedTextColor.GRAY),
										text(npc.getName(), NamedTextColor.WHITE),
										newline(),
										text("Location: ", NamedTextColor.GRAY),
										text(Utils.pettyLocationShort(npc.getLocation()), NamedTextColor.WHITE),
										newline(),
										text("lookAtPlayer: ", NamedTextColor.GRAY),
										text(npc.isLookAtPlayer(), NamedTextColor.WHITE),
										newline(),
										text("imitatePlayer: ", NamedTextColor.GRAY),
										text(npc.isImitatePlayer(), NamedTextColor.WHITE),
										newline(),
										text("showNameTag: ", NamedTextColor.GRAY),
										text(npc.isShowNameTag(), NamedTextColor.WHITE),
										newline(),
										text("equipment: \n", NamedTextColor.GRAY),
										text(npc.getEquipment().size() == 0 ? "- None" : npc.getEquipment().stream().map(s -> "- type: %s\n  - Material: %s".formatted(s.getType(), s.getItem().getType().name())).collect(Collectors.joining("\n")), NamedTextColor.WHITE),
										newline(),
										text("hologram height: ", NamedTextColor.GRAY),
										text(npc.getHologram().getYHeight(), NamedTextColor.WHITE),
										newline(),
										text("holograms: \n", NamedTextColor.GRAY),
										text(npc.getHologram().getLines().size() == 0 ? "- None" : npc.getHologram().getLines().stream().map(s -> "- " + s).collect(Collectors.joining("\n")), NamedTextColor.WHITE),
										newline(),
										text("actions: \n", NamedTextColor.GRAY),
										text(npc.getActions().size() == 0 ? "- None" : npc.getActions().stream().map(s -> "- type: %s\n  - execute: %s".formatted(s.getType(), s.getExecute())).collect(Collectors.joining("\n")), NamedTextColor.WHITE),
										newline(),
										newline(),
										text("Click to teleport!", NamedTextColor.RED, TextDecoration.BOLD)
								))
				));
			}
		} else {
			list.add(textOfChildren(
					text("NPC list are empty.", NamedTextColor.YELLOW),
					space(),
					text("Click here to create some?", NamedTextColor.GOLD)
							.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc create"))
							.hoverEvent(text("Click here! :)", NamedTextColor.AQUA, TextDecoration.BOLD))
			).decorate(TextDecoration.BOLD));
		}
		list.add(LEGACY_SERIALIZER.deserialize("&8&m----------------------------------------"));
		list.forEach(actor::reply);
	}

	@Subcommand({"convert"})
	@Description("Convert NPC from another plugin")
	@Usage("<plugin name>")
	@AutoComplete("servernpc")
	public void convertNPC(BukkitCommandActor actor, @Named("plugin name") ConversionPlugin plugin) {
		actor.reply("&aConverting NPCs...");
		ConversionManager.conversion(plugin).whenComplete((list, throwable) -> {
			if (throwable != null) {
				throwable.printStackTrace();
				return;
			}
			FNPCManager.reload(FantasyNPC.getInstance().getNpcPool());
			actor.reply("&aSuccessfully converted &f%s &aNPCs!".formatted(list.size()));
		});
	}

	@Subcommand({"reloadNPC"})
	@Description("Reload NPC")
	public void reloadNPC(BukkitCommandActor actor) {
		FNPCManager.reload(FantasyNPC.getInstance().getNpcPool());
		actor.reply("&aAll the NPCs has been reloaded.");
	}

	@Subcommand({"reloadConfig"})
	@Description("Reload Config")
	public void reloadConfig(BukkitCommandActor actor) {
		ConfigManager.reloadConfig();
		actor.reply("&aFantasyNPC config has been reloaded.");
	}

	@Subcommand({"create"})
	@Description("Create a new npc")
	@Usage("[npc name] [ID/m:<mineskinUUID>/https://minesk.in/xxx]")
	public void createNPC(BukkitCommandActor actor, @Optional String name, @Optional String skin) {
		actor.requirePlayer();
		Player player = actor.getAsPlayer();
		player.sendMessage(colorize(name == null ? "&aCreating NPC..." : "&aCreating NPC with name [&f%s&a]...".formatted(name)));
		NPCUtils.createNPC(player.getLocation(), name, skin)
				.whenComplete((npc, throwable) -> {
					if (throwable != null) {
						return;
					}
					FNPCManager.save(npc);
					player.sendMessage(colorize("&aNPC &f%s &ahas been successfully created!").formatted(npc.getProfile().getName()));
				})
				.exceptionally((throwable -> {
					throwable.printStackTrace();
					return null;
				}));
	}

	@Subcommand({"copy"})
	@Description("Copy a existed npc")
	@Usage("[npc name]")
	public void copyNPC(BukkitCommandActor actor, FNPC fnpc) {
		Player player = actor.getAsPlayer();
		player.sendMessage(colorize("&eCopying NPC [&f%s&e]...".formatted(fnpc.getKey())));

		FNPC npc = FNPC.cloneExist(fnpc);
		npc.setLocation(player.getLocation());

		FNPCManager.save(npc);
		player.sendMessage(colorize("&aCopied NPC [&f%s&a] &afrom &f%s!".formatted(npc.getKey(), fnpc.getKey())));
		FNPCManager.reload(FantasyNPC.getInstance().getNpcPool());
		player.sendMessage(colorize("&aReloaded all NPC."));
	}

	@Subcommand({"equip help"})
	@Description("Equipment commands help")
	public void equipHelp(BukkitCommandActor actor, CommandHelp<String> helpEntries) {
		Utils.buildCommandHelp(helpEntries, 1, "equip").forEach(actor::reply);
	}

	@Subcommand({"equip add"})
	@Description("Add a NPC equipment by the item in your hand")
	@Usage("<npc> <equipment type>")
	public void equipAdd(BukkitCommandActor actor, FNPC fNpc, @Named("equipment type") FEquipType equipType) {
		actor.requirePlayer();
		Player player = actor.getAsPlayer();

		if (player.getInventory().getItemInHand().getType() == Material.AIR) {
			throw new CommandErrorException("You must hold an item in your hand!");
		}

		ItemStack item = player.getInventory().getItemInHand();
		FEquip equip = new FEquip(equipType, item);
		FNPCManager.updateEquip(fNpc, equip);
		actor.reply(textOfChildren(
				text("Equipment "),
				text(equip.toFancyString(), NamedTextColor.WHITE),
				text(" has been added to NPC "),
				text(fNpc.getName(), NamedTextColor.WHITE)
		).color(NamedTextColor.GREEN));
	}

	@Subcommand({"equip remove"})
	@Description("Remove a NPC equipment by type")
	@Usage("<npc> <equipment type>")
	public void equipRemove(BukkitCommandActor actor, FNPC fNpc, @Named("equipment type") FEquipType equipType) {
		boolean success = fNpc.getEquipment().removeIf(e -> e.getType() == equipType);

		if (!success) {
			throw new CommandErrorException("This equipment type is not available on this NPC!");
		}

		FNPCManager.updateNPC(fNpc);
		actor.reply(textOfChildren(
				text("Equipment type "),
				text(equipType.name(), NamedTextColor.WHITE),
				text(" has been removed from NPC "),
				text(fNpc.getName(), NamedTextColor.WHITE)
		).color(NamedTextColor.GREEN));
	}

	@Subcommand({"equip clear"})
	@Description("Clear all NPC equipment")
	@Usage("<npc>")
	public void equipClear(BukkitCommandActor actor, FNPC fNpc) {
		fNpc.getEquipment().clear();
		FNPCManager.updateNPC(fNpc);
		actor.reply(textOfChildren(
				text("All equipments has been cleared from NPC "),
				text(fNpc.getName(), NamedTextColor.WHITE)
		).color(NamedTextColor.GREEN));
	}

	@Subcommand({"action help"})
	@Description("Action commands help")
	public void actionHelp(BukkitCommandActor actor, CommandHelp<String> helpEntries) {
		Utils.buildCommandHelp(helpEntries, 1, "action").forEach(actor::reply);
	}

	@Subcommand({"action add"})
	@Description("Add a NPC action")
	@Usage("<npc> <command/message/server> <value>")
	public void actionAdd(BukkitCommandActor actor, FNPC fNpc, @Named("action type") FActionType actionType, @Named("action content") String execute) {
		FAction action = new FAction(actionType, execute);
		FNPCManager.addNPCActions(fNpc, action);
		actor.reply(textOfChildren(
				text("Actions "),
				text(action.toFancyString(), NamedTextColor.WHITE),
				text(" has been added to NPC "),
				text(fNpc.getName(), NamedTextColor.WHITE)
		).color(NamedTextColor.GREEN));
	}

	@Subcommand({"action remove"})
	@Description("Remove a NPC action")
	@Usage("<npc> <action slot number> [show list (true/false)]")
	public void actionRemove(BukkitCommandActor actor, FNPC fNpc, @Named("action slot number") int actionSlotNumber, @Default("false") Boolean showList) {
		FAction removedAction = FNPCManager.removeNPCAction(fNpc, actionSlotNumber);
		if (removedAction == null) {
			actor.reply("&cAction remove failed.");
			return;
		}
		actor.reply(textOfChildren(
				text("Actions "),
				text("%s - %s".formatted(actionSlotNumber, removedAction.toFancyString()), NamedTextColor.WHITE),
				text(" has been successfully removed!")
		).color(NamedTextColor.RED));
		if (showList && actor.isPlayer())
			actor.getAsPlayer().performCommand("npc action list %s".formatted(fNpc.getKey()));
	}

	@Subcommand({"action list"})
	@Description("List NPC actions")
	@Usage("<npc>")
	public void actionList(BukkitCommandActor actor, FNPC fNpc) {
		List<Component> list = new ArrayList<>();
		list.add(LEGACY_SERIALIZER.deserialize("&8&m----------------------------------------"));
		list.add(LEGACY_SERIALIZER.deserialize("&b&lFantasyNPC &f(v%s) &7- &fTotal &9%s &factions".formatted(Constants.VERSION, fNpc.getActions().size())));
		if (fNpc.getActions().size() > 0) {
			list.add(LEGACY_SERIALIZER.deserialize("&eHover for more info!"));
			for (int i = 0; i < fNpc.getActions().size(); i++) {
				FAction action = fNpc.getActions().get(i);
				list.add(textOfChildren(
						text("[X]", NamedTextColor.RED, TextDecoration.BOLD)
								.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/npc action remove %s %s true".formatted(fNpc.getKey(), i)))
								.hoverEvent(text("Click here to delete action " + i, NamedTextColor.RED, TextDecoration.BOLD)),
						text(" %s - %s".formatted(action.getType(), action.getExecute()), NamedTextColor.WHITE)
								.hoverEvent(textOfChildren(
										text("Type: ", NamedTextColor.GRAY),
										text(action.getType().name(), NamedTextColor.WHITE),
										newline(),
										text("Execute: ", NamedTextColor.GRAY),
										text(action.getExecute(), NamedTextColor.WHITE)
								))
				));
			}
		} else {
			list.add(textOfChildren(
					text("Actions list are empty.", NamedTextColor.YELLOW),
					space(),
					text("Click here to create some?", NamedTextColor.GOLD)
							.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc action add %s ".formatted(fNpc.getKey())))
							.hoverEvent(text("Click here! :)", NamedTextColor.AQUA, TextDecoration.BOLD))
			).decorate(TextDecoration.BOLD));
		}
		list.add(LEGACY_SERIALIZER.deserialize("&8&m----------------------------------------"));
		list.forEach(actor::reply);
	}

	@Subcommand({"holo help"})
	@Description("Hologram commands help")
	public void holoHelp(BukkitCommandActor actor, CommandHelp<String> helpEntries) {
		Utils.buildCommandHelp(helpEntries, 1, "holo").forEach(actor::reply);
	}

	@Subcommand({"holo add"})
	@Description("Add a NPC hologram")
	@Usage("<npc> <value>")
	public void holoAdd(BukkitCommandActor actor, FNPC fNpc, @Named("hologram content") String value) {
		fNpc.getHologram().getLines().add(value);
		FNPCManager.updateNPC(fNpc);
		actor.reply(textOfChildren(
				text("Holograms "),
				text(value, NamedTextColor.WHITE),
				text(" has been added to NPC "),
				text(fNpc.getName(), NamedTextColor.WHITE)
		).color(NamedTextColor.GREEN));
	}

	@Subcommand({"holo insert"})
	@Description("Insert a NPC hologram")
	@Usage("<npc> <slot> <value>")
	public void holoInsert(BukkitCommandActor actor, FNPC fNpc, @Named("hologram slot") Integer slot, @Named("hologram content") String value) {
		if (slot > fNpc.getHologram().getLines().size()) {
			actor.error("Invalid hologram slot. Must between 1 and %s.".formatted(fNpc.getHologram().getLines().size()));
			return;
		}
		fNpc.getHologram().getLines().add(slot, value);
		FNPCManager.updateNPC(fNpc);
		actor.reply(textOfChildren(
				text("Holograms "),
				text(value, NamedTextColor.WHITE),
				text(" has been inserted to NPC "),
				text(fNpc.getName(), NamedTextColor.WHITE)
		).color(NamedTextColor.GREEN));
	}

	@Subcommand({"holo set"})
	@Description("Set a NPC hologram")
	@Usage("<npc> <slot> <value>")
	public void holoSet(BukkitCommandActor actor, FNPC fNpc, @Named("hologram slot") Integer slot, @Named("hologram content") String value) {
		if (slot > fNpc.getHologram().getLines().size()) {
			actor.error("Invalid hologram slot. Must between 1 and %s.".formatted(fNpc.getHologram().getLines().size()));
			return;
		}
		String oldLine = fNpc.getHologram().getLines().set(slot, value);
		FNPCManager.updateNPC(fNpc);
		actor.reply(textOfChildren(
				text("Holograms #"),
				text(slot, NamedTextColor.WHITE),
				space(),
				text("has been changed from"),
				space(),
				text(oldLine, NamedTextColor.WHITE),
				space(),
				text("to"),
				space(),
				text(value, NamedTextColor.WHITE)
		).color(NamedTextColor.GREEN));
	}

	@Subcommand({"holo height"})
	@Description("Set NPC hologram height")
	@Usage("<npc> <height>")
	public void holoHeight(BukkitCommandActor actor, FNPC fNpc, @Named("hologram height") Double holoHeight) {
		fNpc.getHologram().setYHeight(holoHeight);
		FNPCManager.updateNPC(fNpc);
		actor.reply(textOfChildren(
				text("NPC"),
				space(),
				text(fNpc.getName() + "'s", NamedTextColor.WHITE),
				space(),
				text("hologram height is now set to"),
				space(),
				text(holoHeight, NamedTextColor.WHITE)
		).color(NamedTextColor.GREEN));
	}

	@Subcommand({"holo remove"})
	@Description("Remove a NPC hologram")
	@Usage("<npc> <hologram slot number> [show list (true/false)]")
	public void holoRemove(BukkitCommandActor actor, FNPC fNpc, @Named("hologram slot") int holoSlotNumber, @Default("false") Boolean showList) {
		String removedHolo = fNpc.getHologram().getLines().remove(holoSlotNumber);
		if (removedHolo == null) {
			actor.reply("&cHolo remove failed.");
			return;
		}
		FNPCManager.updateNPC(fNpc);
		actor.reply(textOfChildren(
				text("Hologram "),
				text(removedHolo, NamedTextColor.WHITE),
				text(" has been successfully removed!")
		).color(NamedTextColor.RED));
		if (showList && actor.isPlayer())
			actor.getAsPlayer().performCommand("npc holo list %s".formatted(fNpc.getKey()));
	}

	@Subcommand({"holo list"})
	@Description("List NPC holograms")
	@Usage("<npc>")
	public void holoList(BukkitCommandActor actor, FNPC fNpc) {
		FHolo holo = fNpc.getHologram();
		List<Component> list = new ArrayList<>();
		list.add(LEGACY_SERIALIZER.deserialize("&8&m----------------------------------------"));
		list.add(LEGACY_SERIALIZER.deserialize("&b&lFantasyNPC &f(v%s) &7- &fTotal &9&l%s&r lines".formatted(Constants.VERSION, holo.getLines().size())));
		if (holo.getLines().size() > 0) {
			list.add(LEGACY_SERIALIZER.deserialize("&eHover for more info!"));
			for (int i = 0; i < holo.getLines().size(); i++) {
				String selectedHolo = holo.getLines().get(i);
				list.add(textOfChildren(
						text("[X]", NamedTextColor.RED, TextDecoration.BOLD)
								.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/npc holo remove %s %s true".formatted(fNpc.getKey(), i)))
								.hoverEvent(text("Click here to delete line " + i, NamedTextColor.RED, TextDecoration.BOLD)),
						space(),
						text(selectedHolo, NamedTextColor.WHITE)
								.hoverEvent(textOfChildren(
										text("Type: ", NamedTextColor.GRAY),
										text(selectedHolo.startsWith("ICON:") ? "Item" : "Text", NamedTextColor.WHITE),
										newline(),
										newline(),
										text("Click to edit line " + i, NamedTextColor.RED, TextDecoration.BOLD))
								))
								.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc holo set %s %s %s".formatted(fNpc.getKey(), i, selectedHolo))
				));
			}
		} else {
			list.add(textOfChildren(
					text("Holograms list are empty.", NamedTextColor.YELLOW),
					space(),
					text("Click here to create some?", NamedTextColor.GOLD)
							.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc holo add %s ".formatted(fNpc.getKey())))
							.hoverEvent(text("Click here! :)", NamedTextColor.AQUA, TextDecoration.BOLD))
			).decorate(TextDecoration.BOLD));
		}
		list.add(LEGACY_SERIALIZER.deserialize("&8&m----------------------------------------"));
		list.forEach(actor::reply);
	}

	@Subcommand({"about"})
	@Description("Information about FantasyNPC :)")
	@CommandPermission(value = "", defaultAccess = PermissionDefault.TRUE)
	public void about(BukkitCommandActor actor) {
		actor.reply(textOfChildren(
				Constants.HEADER,
				newline(),
				MINIMESSAGE.deserialize("<b><gradient:#18f6c1:#ff579b>FantasyNPC - %s</gradient></b>".formatted(Constants.VERSION))
						.clickEvent(openUrl("https://go.happyareabean/fantasynpc"))
						.hoverEvent(MINIMESSAGE.deserialize("<rainbow>click me!")),
				newline(),
				text("By ", NamedTextColor.GRAY)
						.append(text("HappyAreaBean", NamedTextColor.GREEN)),
				newline(),
				Constants.HEADER,
				newline(),
				MINIMESSAGE.deserialize(" <dark_gray>•</dark_gray> Plugin Version: <yellow>%s</yellow>".formatted(Constants.VERSION)),
				newline(),
				MINIMESSAGE.deserialize(" <dark_gray>•</dark_gray> Commit: <click:open_url:'https://github.com/fantasyrealms/fantasynpc/commit/%s'><yellow>%s</yellow></click>".formatted(Constants.COMMIT, Constants.COMMIT)),
				newline(),
				MINIMESSAGE.deserialize(" <dark_gray>•</dark_gray> Build Date: <yellow>%s</yellow>".formatted(Constants.BUILD_DATE)),
				newline(),
				Constants.HEADER
		));
	}
}
