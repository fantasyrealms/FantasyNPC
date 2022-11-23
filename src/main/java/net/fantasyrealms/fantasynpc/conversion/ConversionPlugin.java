package net.fantasyrealms.fantasynpc.conversion;

import net.fantasyrealms.fantasynpc.objects.FNPC;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.List;

public abstract class ConversionPlugin {

	public abstract List<FNPC> getNPCs();

	/**
	 * Get the file where all the NPCs are stored.
	 *
	 * @return The NPCs file.
	 */
	public abstract File getNPCsFile();

	public abstract String getPluginName();

	/**
	 * Get the plugin's folder in 'plugins'.
	 *
	 * @return The folder.
	 */
	public File getPluginsFolder() {
		return new File(Bukkit.getServer().getUpdateFolderFile().getParentFile(), this.getPluginName());
	}

}
