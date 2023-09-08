package net.fantasyrealms.fantasynpc.objects;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.github.juliarn.npc.NPC;
import com.github.juliarn.npc.modifier.EquipmentModifier;
import com.github.juliarn.npc.modifier.MetadataModifier;
import com.github.juliarn.npc.profile.Profile;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.SerializeWith;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.fantasyrealms.fantasynpc.config.converter.LocationStringConverter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
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
	 * <p>For NPC update converting check {@link FNPC#fromExist(FNPC, NPC)}</p>
	 *
	 * @param npc NPC object
	 * @return an FNPC object filled without FHolo and FActions
	 */
	public static FNPC fromNPC(NPC npc) {
		Profile profile = npc.getProfile();
		Profile.Property textureProperty = profile.getProperty("textures").isPresent() ? profile.getProperty("textures").get() : new Profile.Property("textures", "null", "null");
		return new FNPC(profile.getUniqueId(), profile.getName(),
				new FSkin(textureProperty.getValue(), textureProperty.getSignature()),
				npc.getLocation(),
				npc.isLookAtPlayer(),
				npc.isImitatePlayer(),
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
	 * <p>For NPC creating check {@link FNPC#fromNPC(NPC)}</p>
	 *
	 * @param fNpc an exist FNPC object for covert
	 * @param npc  new NPC object
	 * @return A new FNPC
	 */
	public static FNPC fromExist(FNPC fNpc, NPC npc) {
		Profile profile = npc.getProfile();
		Profile.Property textureProperty = profile.getProperty("textures").isPresent() ? profile.getProperty("textures").get() : new Profile.Property("textures", "null", "null");
		return new FNPC(fNpc.getUuid(), fNpc.getName(),
				new FSkin(textureProperty.getValue(), textureProperty.getSignature()),
				npc.getLocation(),
				npc.isLookAtPlayer(),
				npc.isImitatePlayer(),
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
	public static NPC.Builder toNPC(FNPC npc) {
		NPC.Builder npcBuilder = NPC.builder();

		npcBuilder.location(npc.getLocation());
		npcBuilder.lookAtPlayer(npc.isLookAtPlayer());
		npcBuilder.imitatePlayer(npc.isImitatePlayer());
		npcBuilder.spawnCustomizer((npcSpawn, viewPlayer) -> {
			npcSpawn.metadata().queue(MetadataModifier.EntityMetadata.SKIN_LAYERS, true).send(viewPlayer);
			npcSpawn.rotation().queueRotate(npc.getLocation().getYaw(), npc.getLocation().getPitch()).send(viewPlayer);

			EquipmentModifier equipModify = npcSpawn.equipment();
			npc.getEquipment().forEach(equip -> {
				ItemStack item = equip.getItem();
				FEquipType type = equip.getType();
				switch (type) {
					case HELMET -> equipModify.queue(EnumWrappers.ItemSlot.HEAD, item).send(viewPlayer);
					case CHEST_PLATE -> equipModify.queue(EnumWrappers.ItemSlot.CHEST, item).send(viewPlayer);
					case LEGGINGS -> equipModify.queue(EnumWrappers.ItemSlot.LEGS, item).send(viewPlayer);
					case BOOTS -> equipModify.queue(EnumWrappers.ItemSlot.FEET, item).send(viewPlayer);
					case MAIN_HAND -> equipModify.queue(EnumWrappers.ItemSlot.MAINHAND, item).send(viewPlayer);
					case OFF_HAND -> equipModify.queue(EnumWrappers.ItemSlot.OFFHAND, item).send(viewPlayer);
				}
			});
		});

		Profile profile = new Profile(npc.getUuid());
		profile.setProperty(new Profile.Property("textures", npc.getSkin().getRaw(), npc.getSkin().getSignature()));
		profile.setName("NPC_" + npc.getKey());
		profile.complete();

		npcBuilder.profile(profile);
		return npcBuilder;
	}
}
