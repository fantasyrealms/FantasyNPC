package net.fantasyrealms.fantasynpc.commands;

import net.fantasyrealms.fantasynpc.FantasyNPC;
import net.fantasyrealms.fantasynpc.constants.Constants;
import net.fantasyrealms.fantasynpc.constants.UpdateType;
import net.fantasyrealms.fantasynpc.manager.ConfigManager;
import net.fantasyrealms.fantasynpc.manager.FNPCManager;
import net.fantasyrealms.fantasynpc.objects.FAction;
import net.fantasyrealms.fantasynpc.objects.FActionType;
import net.fantasyrealms.fantasynpc.objects.FNPC;
import net.fantasyrealms.fantasynpc.util.NPCUtils;
import net.fantasyrealms.fantasynpc.util.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.AutoComplete;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.help.CommandHelp;

import java.util.ArrayList;
import java.util.List;

import static net.fantasyrealms.fantasynpc.FantasyNPC.MINIMESSAGE;
import static net.fantasyrealms.fantasynpc.constants.Constants.HELP_COMMAND_FORMAT;
import static net.fantasyrealms.fantasynpc.util.Utils.LEGACY_SERIALIZER;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.event.ClickEvent.openUrl;
import static revxrsal.commands.util.Strings.colorize;

@Command({"fantasynpc", "npc"})
public class FantasyNPCCommand {

	@Default
	@Description("FantasyNPC commands list")
	public void help(BukkitCommandActor actor, CommandHelp<String> helpEntries, @Default("1") int page) {
		int slotPerPage = 5;
		int maxPages = helpEntries.getPageSize(slotPerPage);
		List<Component> list = new ArrayList<>();
		list.add(LEGACY_SERIALIZER.deserialize("&8&m----------------------------------------"));
		list.add(LEGACY_SERIALIZER.deserialize("&b&lFantasyNPC &f(v%s) &7- &fPage &9(%s/%s)".formatted(Constants.VERSION, page, maxPages)));
		list.add(LEGACY_SERIALIZER.deserialize("&fMade With &4❤ &fBy HappyAreaBean"));
		list.add(text("View more info on ")
				.append(text("Wiki")
						.color(NamedTextColor.AQUA)
						.decorate(TextDecoration.BOLD, TextDecoration.UNDERLINED)
						.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, "https://example.com"))
						.hoverEvent(text("Click to open wiki!"))));
		list.add(LEGACY_SERIALIZER.deserialize(""));
		list.addAll(helpEntries.paginate(page, slotPerPage).stream().map(LEGACY_SERIALIZER::deserialize).toList());
		if (maxPages > 1) list.add(Utils.paginateNavigation(page, maxPages, HELP_COMMAND_FORMAT));
		list.add(LEGACY_SERIALIZER.deserialize("&8&m----------------------------------------"));
		list.forEach(actor::reply);
	}

	@Subcommand({"delete"})
	@Description("Delete a npc using name")
	@Usage("<npc>")
	@AutoComplete("@npcNames *")
	public void deleteNPC(BukkitCommandActor actor, FNPC npc) {
		if (FNPCManager.removeAndClearData(npc.getName())) {
			actor.reply("&aNPC &f[%s] &ahas been successfully deleted!".formatted(npc.getName()));
		} else {
			actor.reply("&cAn error happened while deleting the NPC &f[%s]&c, please check console for more info!".formatted(npc.getName()));
		}
	}

	@Subcommand({"skin"})
	@Description("Change NPC skin")
	@Usage("<npc> <ID/m:<mineskinUUID>/https://minesk.in/xxx>")
	@AutoComplete("@npcNames *")
	public void changeSkin(BukkitCommandActor actor, FNPC npc, String skin) {
		NPCUtils.changeNPCSkin(npc, skin)
				.whenComplete((result, throwable) -> {
					if (throwable != null) {
						actor.reply("&cAn error happened while deleting the NPC &f[%s]&c, please check console for more info!".formatted(npc.getName()));
						throwable.printStackTrace();
						return;
					}
					actor.reply("&aNPC &f[%s] &askin has been changed to &f%s&a!".formatted(npc.getName(), skin));
					actor.reply(text("▍ [Skin still not visible? Click here to reload all the NPCs.]")
									.color(NamedTextColor.RED)
									.decorate(TextDecoration.BOLD)
									.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/npc reloadnpc"))
									.hoverEvent(text("/npc reloadnpc").color(NamedTextColor.YELLOW)));
				});
	}

	@Subcommand({"clear"})
	@Description("Clear ALL the saved NPCs")
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
	@Usage("[name] [ID/m:<mineskinUUID>/https://minesk.in/xxx]")
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

	@Subcommand({"action add"})
	@Description("Add a NPC action")
	@Usage("<npc> <command/message/server> <value>")
	public void actionAdd(BukkitCommandActor actor, FNPC fNpc, FActionType actionType, String execute) {
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
	public void actionRemove(BukkitCommandActor actor, FNPC fNpc, int actionSlotNumber, @Optional Boolean showList) {
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
		if (showList && actor.isPlayer()) actor.getAsPlayer().performCommand("npc action list %s".formatted(fNpc.getKey()));
	}

	@Subcommand({"action list"})
	@Description("List a NPC actions")
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

	@Subcommand({"about"})
	@Description("Information about FantasyNPC :)")
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
				Constants.HEADER
		));
	}
}
