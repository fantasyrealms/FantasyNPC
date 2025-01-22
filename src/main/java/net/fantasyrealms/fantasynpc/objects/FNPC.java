package net.fantasyrealms.fantasynpc.objects;

import com.github.juliarn.npclib.api.Npc;
import com.github.juliarn.npclib.api.profile.Profile;
import com.github.juliarn.npclib.api.profile.ProfileProperty;
import com.github.juliarn.npclib.bukkit.util.BukkitPlatformUtil;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.SerializeWith;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.fantasyrealms.fantasynpc.FantasyNPC;
import net.fantasyrealms.fantasynpc.config.converter.LocationStringConverter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Configuration
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FNPC {

	private UUID uuid;
	private String name;
	private FSkin skin;
	@SerializeWith(serializer = LocationStringConverter.class)
	private Location location;
	private boolean lookAtPlayer;
	private boolean imitatePlayer;
	private boolean showNameTag;
	private List<FEquip> equipment;
	private FHolo hologram;
	private List<FAction> actions;

	/**
	 * Return the first 8 character in UUID (Which is saved as a key in NPCData)
	 *
	 * @return the first 8 character in UUID
	 */
	public String getKey() {
		return uuid.toString().replace("-", "").substring(0, 8);
	}

	/**
	 * Build a new FNPC object from NPC.
	 * This normally use for creating new FNPC.
	 * <br/>
	 * <p>For NPC update converting check {@link FNPC#fromExist(FNPC, Npc)}</p>
	 *
	 * @param npc NPC object
	 * @return an FNPC object filled without FHolo and FActions
	 */
	public static FNPC fromNPC(Npc<World, Player, ItemStack, Plugin> npc) {
		Profile profile = npc.profile();
		ProfileProperty textureProperty = null;
		for (ProfileProperty property : profile.properties()) {
			if (property.name().equalsIgnoreCase("textures")) {
				textureProperty = property;
			}
		}
		if (textureProperty == null) textureProperty = ProfileProperty.property("textures", "null", "null");
		return new FNPC(profile.uniqueId(), profile.name(),
				new FSkin(textureProperty.value(), textureProperty.signature()),
				new Location(Bukkit.getWorld(npc.position().worldId()), npc.position().x(), npc.position().y(), npc.position().z(),
						npc.position().yaw(), npc.position().pitch()),
				npc.flagValueOrDefault(Npc.LOOK_AT_PLAYER),
				npc.flagValueOrDefault(Npc.HIT_WHEN_PLAYER_HITS),
				false,
				new ArrayList<>(),
				new FHolo(2.25, new ArrayList<>()),
				new ArrayList<>());
	}

	/**
	 * Build a new FNPC object from NPC and an existed FNPC object.
	 * <br/>
	 * While still keeping the exclusive setting in FNPC (if exist)
	 * <br/>
	 * <p>This normally use for updating an existed FNPC without losing data.</p>
	 * <p>For NPC creating check {@link FNPC#fromNPC(Npc)}</p>
	 *
	 * @param fNpc an exist FNPC object for covert
	 * @param npc  new NPC object
	 * @return A new FNPC
	 */
	public static FNPC fromExist(FNPC fNpc, Npc<World, Player, ItemStack, Plugin> npc) {
		Profile profile = npc.profile();
		ProfileProperty textureProperty = null;
		for (ProfileProperty property : profile.properties()) {
			if (property.name().equalsIgnoreCase("textures")) {
				textureProperty = property;
			}
		}
		if (textureProperty == null) textureProperty = ProfileProperty.property("textures", "null", "null");
		return new FNPC(fNpc.getUuid(), fNpc.getName(),
				new FSkin(textureProperty.value(), textureProperty.signature()),
				new Location(Bukkit.getWorld(npc.position().worldId()), npc.position().x(), npc.position().y(), npc.position().z(),
						npc.position().yaw(), npc.position().pitch()),
				npc.flagValueOrDefault(Npc.LOOK_AT_PLAYER),
				npc.flagValueOrDefault(Npc.HIT_WHEN_PLAYER_HITS),
				fNpc.isShowNameTag(),
				fNpc.getEquipment(),
				fNpc.getHologram(),
				fNpc.getActions());
	}

	/**
	 * Build a new FNPC object from an existed FNPC object.
	 * <br/>
	 * While still keeping the exclusive setting in FNPC (if exist)
	 * <br/>
	 * <p>This normally use for updating an existed FNPC without losing data.</p>
	 *
	 * @param fNpc an exist FNPC object for covert
	 * @return The same FNPC for edit/update
	 */
	public static FNPC fromExist(FNPC fNpc) {
		return new FNPC(fNpc.getUuid(), fNpc.getName(),
				fNpc.getSkin(),
				fNpc.getLocation(),
				fNpc.isLookAtPlayer(),
				fNpc.isImitatePlayer(),
				fNpc.isShowNameTag(),
				fNpc.getEquipment(),
				fNpc.getHologram(),
				fNpc.getActions());
	}

	/**
	 * Clone a new FNPC object from an existed FNPC object.
	 * <br/>
	 * While still keeping the exclusive setting in FNPC (if exist)
	 * <br/>
	 * <b>The cloned FNPC will also apply a random UUID!</b>
	 * <br/>
	 * <p>This useful for copying an existed FNPC without creating a new one</p>
	 *
	 * @param fNpc an exist FNPC object
	 * @return A new FNPC
	 */
	public static FNPC cloneExist(FNPC fNpc) {
		return new FNPC(UUID.randomUUID(), fNpc.getName(),
				fNpc.getSkin(),
				fNpc.getLocation(),
				fNpc.isLookAtPlayer(),
				fNpc.isImitatePlayer(),
				fNpc.isShowNameTag(),
				fNpc.getEquipment(),
				fNpc.getHologram(),
				fNpc.getActions());
	}

	/**
	 * Return a new NPC Builder from FNPC
	 * <p>This normally use for loading FNPCs to the NPCPool</p>
	 *
	 * @param npc an existed NPC object
	 * @return A new NPC Builder
	 */
	public static Npc.Builder<World, Player, ItemStack, Plugin> toNPC(FNPC npc) {
		var npcBuilder = FantasyNPC.getInstance().getNpcPlatform().newNpcBuilder();

		npcBuilder.position(BukkitPlatformUtil.positionFromBukkitLegacy(npc.getLocation()));
		npcBuilder.flag(Npc.LOOK_AT_PLAYER, npc.isLookAtPlayer());
		npcBuilder.flag(Npc.HIT_WHEN_PLAYER_HITS, npc.isImitatePlayer());
		npcBuilder.flag(Npc.SNEAK_WHEN_PLAYER_SNEAKS, npc.isImitatePlayer());

		Profile.Resolved profile = Profile.resolved("NPC_" + npc.getKey(), npc.getUuid(),
				Set.of(ProfileProperty.property("textures", npc.getSkin().getRaw(), npc.getSkin().getSignature())));

		npcBuilder.profile(profile);
		return npcBuilder;
	}
}
