/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.pneumatic_armor.handlers;

import me.desht.pneumaticcraft.api.pneumatic_armor.BaseArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.BuiltinArmorUpgrades;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorExtensionData;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.pneumatic_armor.JetBootsStateTracker;
import me.desht.pneumaticcraft.common.registry.ModArmorMaterials;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;

import java.util.Optional;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ElytraHandler extends BaseArmorUpgradeHandler<IArmorExtensionData> {
    private static final ResourceKey<EquipmentAsset> ELYTRA_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, RL("pneumatic_elytra"));

    @Override
    public Identifier getID() {
        return BuiltinArmorUpgrades.ELYTRA;
    }

    @Override
    public PNCUpgrade[] getRequiredUpgrades() {
        return new PNCUpgrade[] { ModUpgrades.ELYTRA.get() };
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        return 0;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.CHEST;
    }

    /**
     * Server side, each tick a pneumatic chestplate is worn. 26.1 glides on the GLIDER item component, so it is
     * present only while the upgrade is usable; the equipment asset is swapped too, since wings render from the asset.
     */
    public static void updateGlider(CommonArmorHandler handler, ItemStack chestStack) {
        Player player = handler.getPlayer();
        boolean canGlide = handler.upgradeUsable(CommonUpgradeHandlers.elytraHandler, true)
                && !(handler.upgradeUsable(CommonUpgradeHandlers.jetBootsHandler, true)
                && JetBootsStateTracker.getTracker(player).getJetBootsState(player).isBuilderMode());
        if (canGlide != chestStack.has(DataComponents.GLIDER)) {
            if (canGlide) {
                chestStack.set(DataComponents.GLIDER, Unit.INSTANCE);
            } else {
                chestStack.remove(DataComponents.GLIDER);
            }
            Equippable e = chestStack.get(DataComponents.EQUIPPABLE);
            if (e != null) {
                chestStack.set(DataComponents.EQUIPPABLE, new Equippable(e.slot(), e.equipSound(),
                        Optional.of(canGlide ? ELYTRA_ASSET : ModArmorMaterials.PNEUMATIC.assetId()), e.cameraOverlay(),
                        e.allowedEntities(), e.dispensable(), e.swappable(), e.damageOnHurt(), e.equipOnInteract(),
                        e.canBeSheared(), e.shearingSound()));
            }
        }
        if (canGlide && player.isFallFlying() && (player.getFallFlyingTicks() + 1) % 10 == 0) {
            handler.addAir(EquipmentSlot.CHEST, -10);  // 1 air/tick while gliding
        }
    }
}
