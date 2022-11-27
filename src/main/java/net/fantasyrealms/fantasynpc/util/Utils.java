package net.fantasyrealms.fantasynpc.util;

import net.fantasyrealms.fantasynpc.constants.Constants;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import revxrsal.commands.help.CommandHelp;

import javax.annotation.Nullable;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static net.fantasyrealms.fantasynpc.constants.Constants.HELP_COMMAND_FORMAT;
import static net.fantasyrealms.fantasynpc.constants.Constants.PAGE_TEXT;
import static net.kyori.adventure.text.Component.text;

public class Utils {

	public static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder().character('&').build();
	public static String CURRENT_RELATIVE_PATH = Paths.get("").toAbsolutePath().toString();
	public static DecimalFormat LOCATION_FORMAT = new DecimalFormat("#.###");
	private static final Pattern UUID_REGEX_PATTERN = Pattern.compile("^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$");
	public static String FULL_LINE = "------------------------------------------------------------------------";

	public static List<Component> buildCommandHelp(CommandHelp<String> helpEntries, int page, @Nullable String subCommand) {
		if (subCommand != null) helpEntries.removeIf(s -> !s.contains(subCommand));
		int slotPerPage = 6;
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
		list.add(LEGACY_SERIALIZER.deserialize(""));
		if (maxPages > 1)
			list.add(Utils.paginateNavigation(page, maxPages, subCommand != null ? "/npc " + subCommand + "%s" : HELP_COMMAND_FORMAT));
		list.add(LEGACY_SERIALIZER.deserialize("&8&m----------------------------------------"));
		return list;
	}

	public static Component paginateNavigation(int currentPage, int maxPage, String commandFormat) {
		int previousPage = currentPage - 1;
		int nextPage = currentPage + 1;

		boolean havePreviousPage = previousPage != 0;
		boolean haveNextPage = maxPage != currentPage;

		TextComponent.Builder pageText = text()
				.decorate(TextDecoration.BOLD)
				.color(NamedTextColor.YELLOW);

		pageText.append(text("«", !havePreviousPage ? NamedTextColor.DARK_GRAY : null)
				.clickEvent(havePreviousPage ? ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, commandFormat.formatted(previousPage)) : null)
				.hoverEvent(havePreviousPage ? text(PAGE_TEXT.formatted(previousPage)).color(NamedTextColor.GOLD) : null));

		pageText.append(text(" ▍ "));

		pageText.append(text("»", !haveNextPage ? NamedTextColor.DARK_GRAY : null)
				.clickEvent(haveNextPage ? ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, commandFormat.formatted(nextPage)) : null)
				.hoverEvent(haveNextPage ? text(PAGE_TEXT.formatted(nextPage)).color(NamedTextColor.GOLD) : null));

		return pageText.build();
	}

	public static String pettyLocation(Location loc) {
		String x = LOCATION_FORMAT.format(loc.getX());
		String y = LOCATION_FORMAT.format(loc.getY());
		String z = LOCATION_FORMAT.format(loc.getZ());
		String yaw = LOCATION_FORMAT.format(loc.getYaw());
		String pitch = LOCATION_FORMAT.format(loc.getPitch());
		return "%s / X: %s / Y: %s / Z: %s / Yaw: %s / Pitch: %s".formatted(loc.getWorld().getName(), x, y, z, yaw, pitch);
	}

	public static String pettyLocationShort(Location loc) {
		String x = LOCATION_FORMAT.format(loc.getX());
		String y = LOCATION_FORMAT.format(loc.getY());
		String z = LOCATION_FORMAT.format(loc.getZ());
		return "%s, %s, %s, %s".formatted(loc.getWorld().getName(), x, y, z);
	}

	public static boolean isValidUUID(String str) {
		if (str == null) {
			return false;
		}
		return UUID_REGEX_PATTERN.matcher(str).matches();
	}
}
