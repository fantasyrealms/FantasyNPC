package net.fantasyrealms.fantasynpc.util;

import java.nio.file.Paths;

public class Utils {
	public static String CURRENT_RELATIVE_PATH = Paths.get("").toAbsolutePath().toString();
	public static String FULL_LINE = "------------------------------------------------------------------------";

	public static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch(NumberFormatException | NullPointerException e) {
			return false;
		}
		// only got here if we didn't return false
		return true;
	}
}
