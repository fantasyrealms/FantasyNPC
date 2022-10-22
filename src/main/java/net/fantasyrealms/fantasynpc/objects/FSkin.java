package net.fantasyrealms.fantasynpc.objects;

import de.exlll.configlib.Configuration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@Data @NoArgsConstructor
@AllArgsConstructor
public class FSkin {

	private String raw;
	private String signature;
}
