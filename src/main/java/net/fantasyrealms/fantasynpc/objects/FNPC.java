package net.fantasyrealms.fantasynpc.objects;

import com.github.juliarn.npc.NPC;
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

	private String name;
	private UUID uuid;
	private FSkin skin;
	@SerializeWith(serializer = LocationStringConverter.class)
	private Location location;
	private FHolo hologram;
	private List<FAction> actions;

	public static FNPC fromNPC(NPC npc) {
		Profile profile = npc.getProfile();
		Profile.Property textureProperty = profile.getProperty("textures").isPresent() ? profile.getProperty("textures").get() : new Profile.Property("textures", "null", "null");
		return new FNPC(profile.getName(), profile.getUniqueId(),
				new FSkin(textureProperty.getValue(), textureProperty.getSignature()),
				npc.getLocation(),
				new FHolo(1.0, Collections.emptyList()),
				Collections.emptyList());
	}
}
