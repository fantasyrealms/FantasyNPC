package net.fantasyrealms.fantasynpc.commands;

import net.fantasyrealms.fantasynpc.constants.Constants;
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

import static net.fantasyrealms.fantasynpc.util.Utils.LEGACY_SERIALIZER;
import static revxrsal.commands.util.Strings.colorize;

@Command({"fantasynpc", "npc"})
public class FantasyNPCCommand {

	@Default
	@Description("FantasyNPC commands list")
	public void help(BukkitCommandActor actor, CommandHelp<String> helpEntries, @Default("1") int page) {
		int slotPerPage = 7;
		int maxPages = helpEntries.getPageSize(slotPerPage);
		List<Component> list = new ArrayList<>();
		list.add(LEGACY_SERIALIZER.deserialize("&8&m----------------------------------------"));
		list.add(LEGACY_SERIALIZER.deserialize("&b&lFantasyNPC &f(v%s) &7- &fPage &9(%s/%s)".formatted(Constants.VERSION, page, maxPages)));
		list.add(LEGACY_SERIALIZER.deserialize("&fMade With &4❤ &fBy HappyAreaBean"));
		list.add(Component.text("View more info on ")
				.append(Component.text("Wiki")
						.color(NamedTextColor.AQUA)
						.decorate(TextDecoration.BOLD, TextDecoration.UNDERLINED)
						.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, "https://example.com"))
						.hoverEvent(Component.text("Click to open wiki!"))));
		list.add(LEGACY_SERIALIZER.deserialize(""));
		list.addAll(helpEntries.paginate(page, slotPerPage).stream().map(LEGACY_SERIALIZER::deserialize).toList());
		if (maxPages > 1) list.add(Utils.paginateNavigation(page, maxPages));
		list.add(LEGACY_SERIALIZER.deserialize("&8&m----------------------------------------"));
		list.forEach(message -> actor.audience().sendMessage(message));
	}

	@Subcommand({"delete"})
	@Description("Delete a npc using name")
	@Usage("[name]")
	@AutoComplete("@npcNames *")
	public void deleteNPC(BukkitCommandActor actor, FNPC npc) {
		if (FNPCManager.remove(npc.getName())) {
			actor.reply("&aNPC &f[%s] &ahas been successfully deleted!".formatted(npc.getName()));
		} else {
			actor.reply("&cAn error happened while deleting the NPC &f[%s]&c, please check console for more info!".formatted(npc.getName()));
		}
	}

	@Subcommand({"clear"})
	@Description("Clear ALL the saved NPCs")
	public void clearNPC(BukkitCommandActor actor) {
		FNPCManager.clear();
		actor.reply("&cAll the NPCs data has been cleared.");
	}

	@Subcommand({"create"})
	@Description("Create a new npc")
	@Usage("[name] [ID/m:<mineskinUUID>/https://minesk.in/xxx]")
	public void createNPC(BukkitCommandActor actor, @Optional String name, @Optional String skin) {
		actor.requirePlayer();
		Player player = actor.getAsPlayer();
		player.sendMessage(colorize(name == null ? "&aCreating NPC..." : "&aCreating NPC with name [&f%s&a]...".formatted(name)));
		NPCUtils.createNPC(player, name, skin)
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
