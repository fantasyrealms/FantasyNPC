package net.fantasyrealms.fantasynpc.objects;

import com.github.juliarn.npc.NPC;
import com.github.juliarn.npc.modifier.MetadataModifier;
import com.github.juliarn.npc.profile.Profile;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.SerializeWith;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.fantasyrealms.fantasynpc.config.converter.LocationStringConverter;
import org.bukkit.Location;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Configuration
@Data @NoArgsConstructor
@AllArgsConstructor
public class FNPC {

	private UUID uuid;
	private String name;
	private FSkin skin;
	@SerializeWith(serializer = LocationStringConverter.class)
	private Location location;
	private boolean lookAtPlayer;
	private boolean imitatePlayer;
	private FHolo hologram;
	private List<FAction> actions;

	/**
	 * Return the first 8 character in UUID (Which is saved as a key in NPCData)
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
				new FHolo(1.0, Collections.emptyList()),
				Collections.emptyList());
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
	 * @param npc new NPC object
	 * @return A new FNPC
	 */
	public static FNPC fromExist(FNPC fNpc, NPC npc) {
		Profile profile = npc.getProfile();
		Profile.Property textureProperty = profile.getProperty("textures").isPresent() ? profile.getProperty("textures").get() : new Profile.Property("textures", "null", "null");
		return new FNPC(profile.getUniqueId(), profile.getName(),
				new FSkin(textureProperty.getValue(), textureProperty.getSignature()),
				npc.getLocation(),
				npc.isLookAtPlayer(),
				npc.isImitatePlayer(),
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
			npcSpawn.metadata()
					.queue(MetadataModifier.EntityMetadata.SKIN_LAYERS, true).send(viewPlayer);
			npcSpawn.rotation().queueRotate(npc.getLocation().getYaw(), npc.getLocation().getPitch()).send(viewPlayer);
		});

		Profile profile = new Profile(npc.getUuid());
		profile.setProperty(new Profile.Property("textures", npc.getSkin().getRaw(), npc.getSkin().getSignature()));
		profile.setName(npc.getName());
		profile.complete();

		npcBuilder.profile(profile);
		return npcBuilder;
	}
}
