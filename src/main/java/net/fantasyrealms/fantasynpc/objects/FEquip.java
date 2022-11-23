package net.fantasyrealms.fantasynpc.objects;

import de.exlll.configlib.Configuration;
import de.exlll.configlib.SerializeWith;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.fantasyrealms.fantasynpc.config.converter.XItemStackConverter;
import org.bukkit.inventory.ItemStack;

@Configuration
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FEquip {

	private FEquipType type;
	@SerializeWith(serializer = XItemStackConverter.class)
	private ItemStack item;

	public String toFancyString() {
		return "%s [%s]".formatted(type, item.toString());
	}
}
