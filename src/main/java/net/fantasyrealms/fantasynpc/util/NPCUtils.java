package net.fantasyrealms.fantasynpc.util;

import cc.happyareabean.exceptions.APIException;
import cc.happyareabean.mojangapi.MojangAPI;
import com.github.juliarn.npc.NPC;
import com.github.juliarn.npc.modifier.MetadataModifier;
import com.github.juliarn.npc.profile.Profile;
import net.fantasyrealms.fantasynpc.FantasyNPC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static revxrsal.commands.util.Strings.colorize;

// TODO
// Cleanup? maybe it can be better?
public class NPCUtils {

	public static CompletableFuture<Profile> createProfile(Player player, String name, String skin, SkinType skinType) {

		Profile profile = new Profile(UUID.randomUUID());
		profile.setName(name != null ? name : "NPC_" + profile.getUniqueId().toString().substring(0, 5));

		switch (skinType) {
			case MINESKIN:
				String skinStripped = MineSkinFetcher.removePrefix(skin.contains("minesk.in") ? MineSkinFetcher.stripURL(skin) : skin);
				Bukkit.broadcastMessage(MojangAPI.addDashes(skinStripped));
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
							Bukkit.broadcastMessage(userProfile.getTextures().getRaw().getValue());
							profile.setProperty(new Profile.Property("textures",
									userProfile.getTextures().getRaw().getValue(), userProfile.getTextures().getRaw().getSignature()));
							return profile;
						});
			case NONE:
			default:
				return CompletableFuture.supplyAsync(() -> profile);
		}
	}

	/**
	 * Creates a new NPC
	 */
	public static CompletableFuture<NPC> createNPC(Player player, String name, String skin) {
		SkinType skinType = skin == null ? SkinType.NONE : (skin.startsWith("m:") || skin.contains("minesk.in") ? SkinType.MINESKIN : SkinType.NORMAL);
		Bukkit.broadcastMessage("skin: " + skin);

		return createProfile(player, name, skin, skinType).thenApply((profile) -> {
			NPC.Builder npcBuilder = NPC.builder();
			npcBuilder.location(player.getLocation());
			npcBuilder.imitatePlayer(false);
			npcBuilder.spawnCustomizer((npcSpawn, viewPlayer) -> npcSpawn.metadata()
					.queue(MetadataModifier.EntityMetadata.SKIN_LAYERS, true).send(viewPlayer));
			profile.complete();
			npcBuilder.profile(profile);
			return npcBuilder.build(FantasyNPC.getInstance().getNpcPool());
		});
	}
}