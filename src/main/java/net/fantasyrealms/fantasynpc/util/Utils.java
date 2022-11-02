package net.fantasyrealms.fantasynpc.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.nio.file.Paths;

import static net.fantasyrealms.fantasynpc.constants.Constants.PAGE_TEXT;

public class Utils {

	public static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder().character('&').build();
	public static String CURRENT_RELATIVE_PATH = Paths.get("").toAbsolutePath().toString();
	public static String FULL_LINE = "------------------------------------------------------------------------";

	public static Component paginateNavigation(int currentPage, int maxPage, String commandFormat) {
		int previousPage = currentPage - 1;
		int nextPage = currentPage + 1;

		TextComponent.Builder text = Component.text()
				.append(Component.text("\n"))
				.decorate(TextDecoration.BOLD)
				.color(NamedTextColor.YELLOW);

		if (previousPage != 0)
			text.append(Component.text("«")
					.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, commandFormat.formatted(previousPage)))
					.hoverEvent(Component.text(PAGE_TEXT.formatted(previousPage)).color(NamedTextColor.GOLD)));

		text.append(Component.text(" ▍ "));

		if (maxPage != currentPage)
			text.append(Component.text("»")
					.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, commandFormat.formatted(nextPage)))
					.hoverEvent(Component.text(PAGE_TEXT.formatted(nextPage)).color(NamedTextColor.GOLD)));

		return text.build();
	}
}
