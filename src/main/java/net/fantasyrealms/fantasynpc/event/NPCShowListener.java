package net.fantasyrealms.fantasynpc.event;

import com.github.juliarn.npclib.api.event.ShowNpcEvent;
import com.github.juliarn.npclib.api.event.manager.NpcEventConsumer;
import com.github.juliarn.npclib.api.protocol.enums.ItemSlot;
import com.github.juliarn.npclib.api.protocol.meta.EntityMetadataFactory;
import net.fantasyrealms.fantasynpc.objects.FEquipType;
import net.fantasyrealms.fantasynpc.objects.FNPC;
import net.fantasyrealms.fantasynpc.util.NPCUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NPCShowListener implements NpcEventConsumer<ShowNpcEvent> {

    @Override
    public void handle(@NotNull ShowNpcEvent showNpcEvent) throws Exception {
        var npc = showNpcEvent.npc();
        Player player = showNpcEvent.player();

        npc.changeMetadata(EntityMetadataFactory.skinLayerMetaFactory(), true).schedule(player);

        FNPC fnpc = NPCUtils.findNPCByUUID(npc.profile().uniqueId());
        if (fnpc == null) return;

        fnpc.getEquipment().forEach(equip -> {
            ItemStack item = equip.getItem();
            FEquipType type = equip.getType();
            switch (type) {
                case HELMET -> npc.changeItem(ItemSlot.HEAD, item).schedule(player);
                case CHEST_PLATE -> npc.changeItem(ItemSlot.CHEST, item).schedule(player);
                case LEGGINGS -> npc.changeItem(ItemSlot.LEGS, item).schedule(player);
                case BOOTS -> npc.changeItem(ItemSlot.FEET, item).schedule(player);
                case MAIN_HAND -> npc.changeItem(ItemSlot.MAIN_HAND, item).schedule(player);
                case OFF_HAND -> npc.changeItem(ItemSlot.OFF_HAND, item).schedule(player);
            }
        });
    }
}
