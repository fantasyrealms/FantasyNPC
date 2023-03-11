package net.fantasyrealms.fantasynpc.config.converter;

import com.cryptomorin.xseries.XItemStack;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import de.exlll.configlib.Serializer;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class XItemStackConverter implements Serializer<ItemStack, Map<String, Object>> {

	@Override
	public Map<String, Object> serialize(ItemStack element) {
		return XItemStack.serialize(element);
	}

	@Override
	public ItemStack deserialize(Map<String, Object> element) {
		if (!XMaterial.supports(9)) {
			element.replace("material", "POTION");
		}
		return XItemStack.deserialize(element);
	}
}
