package net.fantasyrealms.fantasynpc.util;

import com.github.juliarn.npc.profile.Profile;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MineSkinFetcher {

	private static final String MINESKIN_API = "https://api.mineskin.org/get/uuid/";

	public static CompletableFuture<Profile.Property> fetchSkinFromUUID(UUID uuid) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				StringBuilder builder = new StringBuilder();
				HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(MINESKIN_API + uuid.toString()).openConnection();
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
				Bukkit.getLogger().severe("Could not fetch skin! (UUID: " + uuid + "). Message: " + exception.getMessage());
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
