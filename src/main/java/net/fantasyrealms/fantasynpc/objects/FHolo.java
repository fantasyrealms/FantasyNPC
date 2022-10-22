package net.fantasyrealms.fantasynpc.objects;

import de.exlll.configlib.Configuration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Configuration
@Data @NoArgsConstructor @AllArgsConstructor
public class FHolo {

	private double yHeight;
	private List<String> lines;

}
