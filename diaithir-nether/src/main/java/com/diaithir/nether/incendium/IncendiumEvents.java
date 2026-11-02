package com.diaithir.nether.incendium;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.Map;

public final class IncendiumEvents {
    private static final Map<String, Identifier> ALIASES = Map.ofEntries(
            Map.entry("ash_sword", id("ember_blade")),
            Map.entry("incendium_sword", id("ember_blade")),
            Map.entry("ash_hammer", id("foundry_hammer")),
            Map.entry("incendium_hammer", id("foundry_hammer")),
            Map.entry("ash_lance", id("heat_lance")),
            Map.entry("ash_shield", id("cinder_shield")),
            Map.entry("ash_armor_helmet", id("slag_helmet")),
            Map.entry("ash_armor_chestplate", id("slag_chestplate")),
            Map.entry("ash_armor_leggings", id("slag_leggings")),
            Map.entry("ash_armor_boots", id("slag_boots")),
            Map.entry("ash_ingot", id("slag_ingot")),
            Map.entry("ash_sentry", id("ember_sentry")),
            Map.entry("magma_brute", id("slag_brute")),
            Map.entry("ash_stalker", id("ash_lurker")));

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(IncendiumRegistries.NS, path);
    }

    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent e) {
        e.put(IncendiumRegistries.EMBER_SENTRY.get(), EmberSentry.attributes().build());
        e.put(IncendiumRegistries.SLAG_BRUTE.get(), SlagBrute.attributes().build());
        e.put(IncendiumRegistries.ASH_LURKER.get(), AshLurker.attributes().build());
    }

    @SubscribeEvent
    public static void onIncoming(LivingIncomingDamageEvent e) {
        LivingEntity victim = e.getEntity();
        if (!e.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            return;
        }
        int pieces = 0;
        for (EquipmentSlot s : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            Item item = victim.getItemBySlot(s).getItem();
            if (item == IncendiumRegistries.SLAG_HELMET.get() || item == IncendiumRegistries.SLAG_CHESTPLATE.get()
                    || item == IncendiumRegistries.SLAG_LEGGINGS.get() || item == IncendiumRegistries.SLAG_BOOTS.get()) {
                pieces++;
            }
        }
        if (victim.getUseItem().getItem() == IncendiumRegistries.CINDER_SHIELD.get()) {
            pieces += 2;
        }
        if (pieces > 0) {
            e.setAmount(e.getAmount() * Math.max(0.0F, 1.0F - 0.2F * pieces));
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post e) {
        if (e.getSource().getEntity() instanceof Player p) {
            Item held = p.getMainHandItem().getItem();
            if (held == IncendiumRegistries.EMBER_BLADE.get() || held == IncendiumRegistries.HEAT_LANCE.get()) {
                e.getEntity().igniteForSeconds(6.0F);
            } else if (held == IncendiumRegistries.FOUNDRY_HAMMER.get()) {
                var d = p.getLookAngle();
                e.getEntity().push(d.x * 0.8, 0.35, d.z * 0.8);
            }
        }
    }

    public static class NetherContentEvent extends Event {
        private final String content;
        private final int age;

        public NetherContentEvent(String content, int age) {
            this.content = content;
            this.age = age;
        }

        public String content() {
            return content;
        }

        public int age() {
            return age;
        }
    }

    public static void fireGlobalEventHook(String content, int age) {
        NeoForge.EVENT_BUS.post(new NetherContentEvent(content, age));
    }

    private IncendiumEvents() {}
}
