package net.fantasyrealms.fantasynpc.config;

import de.exlll.configlib.Configuration;
import lombok.Getter;
import lombok.Setter;
import net.fantasyrealms.fantasynpc.objects.FNPC;

import java.util.Collections;
import java.util.Map;

@Configuration
@Getter @Setter
public class NPCData {

	private Map<String, FNPC> npcs = Collections.emptyMap();
}
