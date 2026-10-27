/*
 * Stub: the client side of this class has not been ported to 26.1.2 yet (see PORT_NOTES.md).
 * The original source is kept under port-stash/.
 */
package me.desht.pneumaticcraft.client.pneumatic_armor;


import me.desht.pneumaticcraft.api.client.pneumatic_armor.IArmorUpgradeClientHandler;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.List;

public final class ClientArmorRegistry {
    private static final ClientArmorRegistry INSTANCE = new ClientArmorRegistry();
    public static ClientArmorRegistry getInstance() { return INSTANCE; }
    public void refreshConfig() {}
    public boolean isFrozen() { return true; }
    public IArmorUpgradeClientHandler<?> getClientHandler(Identifier id) { return null; }
    public <C> C getClientHandler(Object armorUpgradeHandler, Class<C> clientClass) { return null; }
    public List<IArmorUpgradeClientHandler<?>> getHandlersForSlot(EquipmentSlot slot) { return List.of(); }
}
