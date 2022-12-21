package net.fantasyrealms.fantasynpc.util;

import cc.happyareabean.mojangapi.MojangAPI;
import com.github.juliarn.npc.profile.Profile;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fantasyrealms.fantasynpc.FantasyNPC;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class MineSkinFetcher {

	private static final String MINESKIN_API = "https://api.mineskin.org/get/uuid/";
	private static final String MINESKIN_ID_API = "https://api.mineskin.org/get/id/";

	public static CompletableFuture<Profile.Property> fetchSkin(String input) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				StringBuilder builder = new StringBuilder();
				String skinValue = removePrefix(input.contains("minesk.in") ? stripURL(input) : input);
				String apiUrl = Utils.isValidUUID(MojangAPI.addDashes(skinValue)) ? MINESKIN_API : MINESKIN_ID_API;
				HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(apiUrl + skinValue).openConnection();
				httpURLConnection.setRequestMethod("GET");
				httpURLConnection.setDoOutput(true);
				httpURLConnection.setDoInput(true);
				httpURLConnection.connect();

				Scanner scanner = new Scanner(httpURLConnection.getInputStream());
				while (scanner.hasNextLine()) {
					builder.append(scanner.nextLine());
				}

				scanner.close();
				httpURLConnection.disconnect();

				JsonObject jsonObject = (JsonObject) new JsonParser().parse(builder.toString());
				JsonObject textures = jsonObject.get("data").getAsJsonObject().get("texture").getAsJsonObject();
				String value = textures.get("value").getAsString();
				String signature = textures.get("signature").getAsString();

				return new Profile.Property("textures", value, signature);
			} catch (IOException exception) {
				FantasyNPC.debug("Could not fetch skin: %s! Message: %s".formatted(input, exception.getMessage()));
				exception.printStackTrace();
				return null;
			}
		});
	}

	public static String stripURL(String input) {
		try {
			return new URL(input).getPath().replaceFirst("/", "");
		} catch (MalformedURLException e) {
			return "null";
		}
	}

	public static String removePrefix(String input) {
		return input.replace("m:", "");
	}
}
