package me.desht.pneumaticcraft.client.pneumatic_armor.entity_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IEntityTrackEntry;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class EntityTrackEntryPlayer implements IEntityTrackEntry {
    @Override
    public boolean isApplicable(Entity entity) {
        return entity instanceof Player;
    }

    @Override
    public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
        Player player = (Player) entity;

        addInventory("pneumaticcraft.entityTracker.info.player.armor", curInfo, armorList(player));
        addInventory("pneumaticcraft.entityTracker.info.player.holding", curInfo, player.getInventory().getNonEquipmentItems());
    }

    private static NonNullList<ItemStack> armorList(Player player) {
        NonNullList<ItemStack> l = NonNullList.withSize(4, ItemStack.EMPTY);
        net.minecraft.world.entity.EquipmentSlot[] slots = {net.minecraft.world.entity.EquipmentSlot.FEET, net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.CHEST, net.minecraft.world.entity.EquipmentSlot.HEAD};
        for (int i = 0; i < 4; i++) l.set(i, player.getItemBySlot(slots[i]));
        return l;
    }

    private static void addInventory(String key, List<Component> curInfo, NonNullList<ItemStack> stacks) {
        curInfo.add(xlate(key).withStyle(ChatFormatting.GRAY));
        List<Component> l = PneumaticCraftUtils.summariseItemStacks(new ArrayList<>(), stacks);
        if (l.isEmpty()) {
            curInfo.add(xlate("pneumaticcraft.gui.misc.no_items"));
        } else {
            curInfo.addAll(l);
        }
    }
}
