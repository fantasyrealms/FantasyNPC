package net.fantasyrealms.fantasynpc.objects;

import de.exlll.configlib.Configuration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@Data @NoArgsConstructor
@AllArgsConstructor
public class FAction {

	private FActionType type;
	private String execute;

	public String toFancyString() {
		return "%s [%s]".formatted(type, execute);
	}
}
