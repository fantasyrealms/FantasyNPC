package net.fantasyrealms.fantasynpc.constants;

import com.google.gson.Gson;
import net.fantasyrealms.fantasynpc.FantasyNPC;

import java.util.Arrays;
import java.util.List;

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
	public static String VERSION = FantasyNPC.getInstance().getDescription().getVersion();
}
