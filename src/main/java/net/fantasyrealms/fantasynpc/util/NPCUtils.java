package net.fantasyrealms.fantasynpc.util;

import cc.happyareabean.exceptions.APIException;
import cc.happyareabean.mojangapi.MojangAPI;
import com.github.juliarn.npclib.api.Npc;
import com.github.juliarn.npclib.api.profile.Profile;
import com.github.juliarn.npclib.api.profile.ProfileProperty;
import com.github.juliarn.npclib.bukkit.util.BukkitPlatformUtil;
import net.fantasyrealms.fantasynpc.FantasyNPC;
import net.fantasyrealms.fantasynpc.manager.FNPCManager;
import net.fantasyrealms.fantasynpc.objects.FAction;
import net.fantasyrealms.fantasynpc.objects.FNPC;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import revxrsal.commands.exception.CommandErrorException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class NPCUtils {

	public static CompletableFuture<Profile.Resolved> createProfile(String name, String skin) {
		return createProfile(null, name, skin);
	}

	public static CompletableFuture<Profile.Resolved> createProfile(@Nullable UUID uuid, String name, String skin) {
		SkinType skinType = skin == null ? SkinType.NONE : (skin.startsWith("m:") || skin.contains("minesk.in") ? SkinType.MINESKIN : SkinType.NORMAL);

		UUID uuidResult = uuid != null ? uuid : UUID.randomUUID();
		boolean nameExist = name != null && nameExists(name);
		String nameResults = name == null ? "npc" : name;

		FantasyNPC.debug("Name exists: %s / Inputted Name: %s / Final Name: %s / Skin type: %s".formatted(nameExist, name, nameResults, skinType));

		if (skinType != SkinType.NONE) FantasyNPC.debug("Fetching skin type %s: %s...".formatted(skinType.name().toLowerCase(), skin));
		return switch (skinType) {
			case MINESKIN -> MineSkinFetcher.fetchSkin(skin)
					.thenApplyAsync((textureProperty) -> {
						if (textureProperty == null) {
							throw new CommandErrorException("&cYour mineskin UUID/URL is not valid, you can change the skin by using &e/npc skin &ccommand.");
						}
						return Profile.resolved(nameResults, uuidResult, Set.of(textureProperty));
					});
			case NORMAL -> CompletableFuture.supplyAsync(() -> {
						try {
							cc.happyareabean.mojangapi.profile.Profile userProfile = MojangAPI.getProfile(skin);
							if (userProfile.getCode() != 0) throw new RuntimeException("Invalid response code.");
							return userProfile;
						} catch (IOException | APIException e) {
							throw new RuntimeException(e);
						}
					}).exceptionally((throwable) -> {
						FantasyNPC.debug("Error while getting skin: %s: %s".formatted(skin, throwable.getMessage()));
						FantasyNPC.debug("Skipped skin changing...");
						return null;
					})
					.thenApplyAsync((userProfile) -> {
						if (userProfile == null) {
							return Profile.resolved("FantasyNPC", UUID.randomUUID());
						}
						return Profile.resolved(nameResults, uuidResult, Set.of(ProfileProperty.property("textures",
								userProfile.getTextures().getRaw().getValue(), userProfile.getTextures().getRaw().getSignature())));
					});
			case NONE -> CompletableFuture.supplyAsync(() -> Profile.resolved("FantasyNPC", UUID.randomUUID()));
		};
	}

	/**
	 * Creates a new NPC
	 */
	public static CompletableFuture<Npc<World, Player, ItemStack, Plugin>> createNPC(Location location, String name, String skin) {
		return createProfile(name, skin).thenApply((profile) -> {
			var npcBuilder = FantasyNPC.getInstance().getNpcPlatform().newNpcBuilder();
			npcBuilder.position(BukkitPlatformUtil.positionFromBukkitLegacy(location));
			npcBuilder.profile(profile);
			return npcBuilder.buildAndTrack();
		});
	}

	public static CompletableFuture<Boolean> changeNPCSkin(FNPC fNpc, String skin) {
		return createProfile(fNpc.getUuid(), fNpc.getName(), skin)
				.thenApplyAsync((profile) -> {;
					FNPCManager.updateProfile(fNpc, profile);
					return true;
				});
	}

	public static List<FAction> getNPCActionFromUUID(UUID uuid) {
		for (FNPC npcD : FantasyNPC.getInstance().getNpcData().getNpcs().values()) {
			if (npcD.getUuid() == uuid) {
				return npcD.getActions();
			}
		}
		return Collections.emptyList();
	}

	public static FNPC findNPCByName(String name) {
		return FantasyNPC.getInstance().getNpcData().getNpcs().values().stream()
				.filter(npc -> npc.getName().equalsIgnoreCase(name))
				.findFirst().orElse(null);
	}

	public static FNPC findNPCByUUID(UUID uuid) {
		return FantasyNPC.getInstance().getNpcData().getNpcs().values().stream()
				.filter(npc -> npc.getUuid() == uuid)
				.findFirst().orElse(null);
	}

	public static Set<String> getNPCNames() {
		return FantasyNPC.getInstance().getNpcData().getNpcs().values().stream().map(FNPC::getName).collect(Collectors.toUnmodifiableSet());
	}

	public static Set<FNPC> getNPCs() {
		return FantasyNPC.getInstance().getNpcData().getNpcs().values().stream().collect(Collectors.toUnmodifiableSet());
	}

	public static boolean nameExists(String input) {
		return getNPCNames().contains(input);
	}
}