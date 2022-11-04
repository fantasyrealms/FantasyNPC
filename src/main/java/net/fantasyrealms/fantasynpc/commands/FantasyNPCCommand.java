package net.fantasyrealms.fantasynpc.commands;

import net.fantasyrealms.fantasynpc.FantasyNPC;
import net.fantasyrealms.fantasynpc.constants.Constants;
import net.fantasyrealms.fantasynpc.constants.UpdateType;
import net.fantasyrealms.fantasynpc.manager.ConfigManager;
import net.fantasyrealms.fantasynpc.manager.FNPCManager;
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

import static net.fantasyrealms.fantasynpc.constants.Constants.HELP_COMMAND_FORMAT;
import static net.fantasyrealms.fantasynpc.util.Utils.LEGACY_SERIALIZER;
import static net.kyori.adventure.text.Component.text;
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
		list.forEach(message -> actor.audience().sendMessage(message));
	}

	@Subcommand({"delete"})
	@Description("Delete a npc using name")
	@Usage("[name]")
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
	@Usage("[name]")
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

	@Subcommand({"lookAtPlayer"})
	@Description("Toggles whether NPCs look at the player")
	@Usage("[name]")
	public void lookAtPlayer(BukkitCommandActor actor, FNPC fNpc) {
		FNPC npc = FNPCManager.updateNPC(fNpc, UpdateType.LOOK_AT_PLAYER);
		actor.reply("&f%s %s".formatted(npc.getName(), npc.isLookAtPlayer() ? "&anow will look at the player!" : "&cnow will no longer look at the player."));
	}

	@Subcommand({"imitatePlayer"})
	@Description("Toggles whether NPCs imitate the player")
	@Usage("[name]")
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
}
