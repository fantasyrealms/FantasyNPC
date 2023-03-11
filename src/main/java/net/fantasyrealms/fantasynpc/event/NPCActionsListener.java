package net.fantasyrealms.fantasynpc.event;

import com.github.juliarn.npc.NPC;
import com.github.juliarn.npc.event.PlayerNPCInteractEvent;
import net.fantasyrealms.fantasynpc.util.Common;
import net.fantasyrealms.fantasynpc.util.NPCUtils;
import net.fantasyrealms.fantasynpc.util.PlayerUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NPCActionsListener implements Listener {

	@EventHandler
	public void onPlayerNPCInteract(PlayerNPCInteractEvent event) {
		if (event.getUseAction() == PlayerNPCInteractEvent.EntityUseAction.INTERACT_AT) return;

		Player player = event.getPlayer();
		NPC npc = event.getNPC();

		NPCUtils.getNPCAction(npc).forEach((action -> {
			switch (action.getType()) {
				case MESSAGE -> Common.sendMessage(player, action.getExecute());
				case COMMAND -> player.performCommand(action.getExecute());
				case SERVER -> PlayerUtils.sendServer(player, action.getExecute());
			}
		}));
	}
}
