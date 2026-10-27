package net.diaithir.butchery;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import java.util.Iterator;

@Mod(Butchery.MOD)
public final class DiaithirButchery {
    public DiaithirButchery(IEventBus modBus) {
        Butchery.register(modBus);
        NeoForge.EVENT_BUS.addListener(AddServerReloadListenersEvent.class,
                e -> e.addListener(Identifier.fromNamespaceAndPath(Butchery.MOD, "butchery"), new ButcheryData.Loader()));
        NeoForge.EVENT_BUS.addListener(LivingDropsEvent.class, DiaithirButchery::onDrops);
    }

    /** Raw meat drops are replaced by one whole carcass; hides, feathers and the rest of the loot stay. */
    private static void onDrops(LivingDropsEvent event) {
        LivingEntity e = event.getEntity();
        if (e.level().isClientSide()) return;
        String body = ButcheryData.bodyOf(e);
        if (body == null || !ButcheryData.known(body)) return;
        boolean meat = false;
        for (Iterator<ItemEntity> it = event.getDrops().iterator(); it.hasNext();) {
            ItemStack s = it.next().getItem();
            if (isRawMeat(s)) {
                it.remove();
                meat = true;
            }
        }
        if (!meat) return;
        String species = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString();
        ItemStack carcass = new ItemStack(Butchery.CARCASS.get());
        carcass.set(Butchery.SPECIES.get(), species);
        carcass.set(Butchery.BODY.get(), body);
        carcass.set(Butchery.KILLED.get(), e.level().getGameTime());
        carcass.set(net.minecraft.core.component.DataComponents.ITEM_NAME, Component.translatable("item.diaithirbutchery.carcass_of", e.getType().getDescription()));
        event.getDrops().add(new ItemEntity(e.level(), e.getX(), e.getY(), e.getZ(), carcass));
    }

    private static boolean isRawMeat(ItemStack s) {
        if (s.getItem().components().get(net.minecraft.core.component.DataComponents.FOOD) == null) return false;
        String path = BuiltInRegistries.ITEM.getKey(s.getItem()).getPath();
        return !path.contains("cooked") && !path.contains("steak") && (path.contains("beef") || path.contains("pork") || path.contains("mutton")
                || path.contains("chicken") || path.contains("rabbit") || path.contains("cod") || path.contains("salmon") || path.contains("meat")
                || path.contains("fish") || path.contains("venison") || path.contains("leg") || path.contains("flesh"));
    }
}
