package net.fantasyrealms.fantasynpc.util;

import cc.happyareabean.exceptions.APIException;
import cc.happyareabean.mojangapi.MojangAPI;
import com.github.juliarn.npc.NPC;
import com.github.juliarn.npc.modifier.MetadataModifier;
import com.github.juliarn.npc.profile.Profile;
import net.fantasyrealms.fantasynpc.FantasyNPC;
import net.fantasyrealms.fantasynpc.objects.FNPC;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static revxrsal.commands.util.Strings.colorize;

// TODO
// Cleanup? maybe it can be better?
public class NPCUtils {

	public static CompletableFuture<Profile> createProfile(Player player, String name, String skin, SkinType skinType) {

		Profile profile = new Profile(UUID.randomUUID());
		boolean nameExist = name != null && nameExists(name);
		String uuidString = profile.getUniqueId().toString().substring(0, 5);
		String nameResults = nameExist ? "%s_%s".formatted(name, uuidString) : (name != null ? name : "NPC_%s".formatted(uuidString));

		FantasyNPC.debug("Name exists: %s / Inputted Name: %s / Final Name: %s".formatted(nameExist, name, nameResults));

		if (nameExist) {
			player.sendMessage(colorize("&cThere already have the same NPC named &f%s&c!").formatted(name));
			player.sendMessage(colorize("&aUsing random name instead: &f%s").formatted(nameResults));
		}

		profile.setName(nameResults);

		if (skinType != SkinType.NONE) player.sendMessage(colorize("&7Fetching skin type %s: &f%s&7...").formatted(skinType.name().toLowerCase(), skin));
		switch (skinType) {
			case MINESKIN:
				String skinStripped = MineSkinFetcher.removePrefix(skin.contains("minesk.in") ? MineSkinFetcher.stripURL(skin) : skin);
				return MineSkinFetcher.fetchSkinFromUUID(UUID.fromString(MojangAPI.addDashes(skinStripped)))
						.thenApplyAsync((textureProperty) -> {
							if (textureProperty == null) {
								player.sendMessage(colorize("&cYour mineskin UUID/URL is not valid, you can change the skin by using &e/npc skin &ccommand."));
								return profile;
							}
							profile.setProperty(textureProperty);
							return profile;
						});
			case NORMAL:
				return CompletableFuture.supplyAsync(() -> {
							try {
								cc.happyareabean.mojangapi.profile.Profile userProfile = MojangAPI.getProfile(skin);
								if (userProfile.getCode() != 0) throw new RuntimeException("Invalid response code.");
								return userProfile;
							} catch (IOException | APIException e) {
								throw new RuntimeException(e);
							}
						}).exceptionally((throwable) -> {
							player.sendMessage(colorize("&cError while getting skin: %s: &f%s".formatted(skin, throwable.getMessage())));
							player.sendMessage(colorize("&7Skipped skin changing..."));
							return null;
						})
						.thenApplyAsync((userProfile) -> {
							if (userProfile == null) {
								return profile;
							}
							profile.setProperty(new Profile.Property("textures",
									userProfile.getTextures().getRaw().getValue(), userProfile.getTextures().getRaw().getSignature()));
							return profile;
						});
			case NONE:
			default:
				return CompletableFuture.supplyAsync(() -> {
					profile.complete();
					return profile;
				});
		}
	}

	/**
	 * Creates a new NPC
	 */
	public static CompletableFuture<NPC> createNPC(Player player, String name, String skin) {
		SkinType skinType = skin == null ? SkinType.NONE : (skin.startsWith("m:") || skin.contains("minesk.in") ? SkinType.MINESKIN : SkinType.NORMAL);
		final Location location = player.getLocation();

		return createProfile(player, name, skin, skinType).thenApply((profile) -> {
			NPC.Builder npcBuilder = NPC.builder();
			npcBuilder.location(location);
			npcBuilder.imitatePlayer(false);
			npcBuilder.lookAtPlayer(false);
			npcBuilder.spawnCustomizer((npcSpawn, viewPlayer) -> npcSpawn.metadata()
					.queue(MetadataModifier.EntityMetadata.SKIN_LAYERS, true).send(viewPlayer));
			profile.complete();
			npcBuilder.profile(profile);
			return npcBuilder.build(FantasyNPC.getInstance().getNpcPool());
		});
	}

	public static Set<String> getNPCNames() {
		return FantasyNPC.getInstance().getNpcData().getNpcs().values().stream().map(FNPC::getName).collect(Collectors.toUnmodifiableSet());
	}

	public static boolean nameExists(String input) {
		return getNPCNames().contains(input);
	}
}