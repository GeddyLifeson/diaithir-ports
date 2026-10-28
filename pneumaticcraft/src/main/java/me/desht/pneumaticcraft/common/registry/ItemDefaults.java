package me.desht.pneumaticcraft.common.registry;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import net.minecraft.core.component.DataComponents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

import java.util.function.IntSupplier;

/**
 * 26.1 has no per-stack getMaxDamage() hook, so the config-driven durabilities of the ammo / missile items
 * are applied as default data components instead.
 */
@EventBusSubscriber(modid = Names.MOD_ID)
public class ItemDefaults {
    @SubscribeEvent
    public static void modifyDefaults(ModifyDefaultComponentsEvent event) {
        set(event, ModItems.MICROMISSILES.get(), () -> ConfigHelper.common().micromissiles.missilePodSize.get());
        set(event, ModItems.GUN_AMMO.get(), () -> ConfigHelper.common().minigun.standardAmmoCartridgeSize.get());
        set(event, ModItems.GUN_AMMO_INCENDIARY.get(), () -> ConfigHelper.common().minigun.incendiaryAmmoCartridgeSize.get());
        set(event, ModItems.GUN_AMMO_WEIGHTED.get(), () -> ConfigHelper.common().minigun.weightedAmmoCartridgeSize.get());
        set(event, ModItems.GUN_AMMO_AP.get(), () -> ConfigHelper.common().minigun.armorPiercingAmmoCartridgeSize.get());
        set(event, ModItems.GUN_AMMO_EXPLOSIVE.get(), () -> ConfigHelper.common().minigun.explosiveAmmoCartridgeSize.get());
        set(event, ModItems.GUN_AMMO_FREEZING.get(), () -> ConfigHelper.common().minigun.freezingAmmoCartridgeSize.get());
    }

    private static void set(ModifyDefaultComponentsEvent event, net.minecraft.world.item.Item item, IntSupplier size) {
        try {
            int n = size.getAsInt();
            event.modify(item, b -> b.set(DataComponents.MAX_DAMAGE, n));
        } catch (IllegalStateException e) {
            // config not loaded yet: keep the durability the item was registered with
        }
    }
}
