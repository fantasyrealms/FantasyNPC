package net.fantasyrealms.fantasynpc.constants;

import com.google.gson.Gson;
import net.fantasyrealms.fantasynpc.FantasyNPC;
import net.kyori.adventure.text.Component;

import java.util.Arrays;
import java.util.List;

import static net.fantasyrealms.fantasynpc.FantasyNPC.MINIMESSAGE;

public class Constants {

	public static List<String> LOGO = Arrays.asList(
			"",
			"______          _                  _   _ ______  _____ ",
			"|  ___|        | |                | \\ | || ___ \\/  __ \\",
			"| |_ __ _ _ __ | |_ __ _ ___ _   _|  \\| || |_/ /| /  \\/",
			"|  _/ _` | '_ \\| __/ _` / __| | | | . ` ||  __/ | |    ",
			"| || (_| | | | | || (_| \\__ \\ |_| | |\\  || |    | \\__/\\",
			"\\_| \\__,_|_| |_|\\__\\__,_|___/\\__, \\_| \\_/\\_|     \\____/",
			"                              __/ |                    ",
			"                             |___/                     ",
			"");
	public static Gson GSON = new Gson();
	public static String VERSION = "${pluginVersion}";
	public static String COMMIT = "${commit}";
	public static String BUILD_DATE = "${buildDate}";

	public static final Component HEADER = MINIMESSAGE.deserialize("<gradient:green:blue>－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－</gradient>");

	public static final String HELP_COMMAND_FORMAT = "/npc %s";
	public static final String PAGE_TEXT = "Page %s";
	public static final String CONFIG_FILE_NAME = "config.yml";
	public static final String NPCDATA_FILE_NAME = "npcs.yml";
}
