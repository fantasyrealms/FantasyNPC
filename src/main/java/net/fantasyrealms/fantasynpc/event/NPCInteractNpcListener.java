package net.fantasyrealms.fantasynpc.event;

import com.github.juliarn.npclib.api.event.InteractNpcEvent;
import com.github.juliarn.npclib.api.event.manager.NpcEventConsumer;
import net.fantasyrealms.fantasynpc.FantasyNPC;
import net.fantasyrealms.fantasynpc.util.Common;
import net.fantasyrealms.fantasynpc.util.NPCUtils;
import net.fantasyrealms.fantasynpc.util.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NPCInteractNpcListener implements NpcEventConsumer<InteractNpcEvent>  {

    @Override
    public void handle(@NotNull InteractNpcEvent interactNpcEvent) throws Exception {
        Player player = interactNpcEvent.player();
        var npc = interactNpcEvent.npc();

        NPCUtils.getNPCActionFromUUID(npc.profile().uniqueId()).forEach((action -> {
            switch (action.getType()) {
                case MESSAGE -> Common.sendMessage(player, action.getExecute());
                case COMMAND -> Bukkit.getScheduler().runTask(FantasyNPC.getInstance(), () -> player.performCommand(action.getExecute()));
                case SERVER -> PlayerUtils.sendServer(player, action.getExecute());
            }
        }));
    }

}
