package net.fantasyrealms.fantasynpc.commands;

import com.github.juliarn.npc.modifier.MetadataModifier;
import net.fantasyrealms.fantasynpc.constants.Constants;
import net.fantasyrealms.fantasynpc.manager.FNPCManager;
import net.fantasyrealms.fantasynpc.util.NPCUtils;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.help.CommandHelp;

import java.util.ArrayList;
import java.util.List;

import static revxrsal.commands.util.Strings.colorize;

@Command({"fantasynpc", "npc"})
public class FantasyNPCCommand {

	@Default
	@Description("FantasyNPC commands list")
	public void help(CommandActor actor, CommandHelp<String> helpEntries, @Default("1") int page) {
		List<String> list = new ArrayList<>();
		list.add("&8&m----------------------------------------");
		list.add("&b&lFantasyNPC &f(v%s) &7- &fPage &9(%s/%s)".formatted(Constants.VERSION, page, helpEntries.size()));
		list.add("&fMade With &4❤ &fBy HappyAreaBean");
		list.add("");
		list.addAll(helpEntries.paginate(page, 7));
		list.add("&8&m----------------------------------------");
		list.forEach(actor::reply);
	}

	@Subcommand({"create"})
	@Description("Create a new npc")
	@Usage("[name] [ID/m:<mineskinUUID>/minesk.in]")
	public void createNPC(Player sender, @Optional String name, @Optional String skin) {
		sender.sendMessage(colorize(name == null ? "&aCreating NPC..." : "&aCreating NPC with name [&f%s&a]...".formatted(name)));
		NPCUtils.createNPC(sender, name, skin)
				.whenComplete((npc, throwable) -> {
					if (throwable != null) {
						return;
					}
					FNPCManager.save(npc);
					sender.sendMessage(colorize("&aNPC &f%s &ahas been successfully created!").formatted(npc.getProfile().getName()));
				})
				.exceptionally((throwable -> {
					throwable.printStackTrace();
					return null;
				}));

	}
}
