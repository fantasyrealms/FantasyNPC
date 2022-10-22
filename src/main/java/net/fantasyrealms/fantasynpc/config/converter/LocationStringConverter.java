package net.fantasyrealms.fantasynpc.config.converter;

import de.exlll.configlib.Serializer;
import net.fantasyrealms.fantasynpc.util.LocationSerilization;
import org.bukkit.Location;

public class LocationStringConverter implements Serializer<Location, String> {

	@Override
	public String serialize(Location location) {
		return LocationSerilization.serialize(location);
	}

	@Override
	public Location deserialize(String locationString) {
		return LocationSerilization.deserialize(locationString);
	}
}
