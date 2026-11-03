package net.diaithir.worldvisuals.lambdynamiclights.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class LuminanceResolver {
    private static volatile Map<Item, Integer> itemOverrides = Map.of();
    private static volatile Map<EntityType<?>, Integer> entityOverrides = Map.of();
    private static volatile Set<EntityType<?>> blacklist = Set.of();

    private LuminanceResolver() {}

    public static void refresh() {
        Map<Item, Integer> items = new HashMap<>();
        for (String s : DynamicLightsConfig.ITEM_OVERRIDES.get()) {
            String[] p = s.split("=", 2);
            if (p.length != 2) continue;
            Identifier id = Identifier.tryParse(p[0].trim());
            Integer lvl = parse(p[1]);
            if (id == null || lvl == null) continue;
            BuiltInRegistries.ITEM.get(id).ifPresent(h -> items.put(h.value(), lvl));
        }
        Map<EntityType<?>, Integer> ents = new HashMap<>();
        for (String s : DynamicLightsConfig.ENTITY_OVERRIDES.get()) {
            String[] p = s.split("=", 2);
            if (p.length != 2) continue;
            Identifier id = Identifier.tryParse(p[0].trim());
            Integer lvl = parse(p[1]);
            if (id == null || lvl == null) continue;
            BuiltInRegistries.ENTITY_TYPE.get(id).ifPresent(h -> ents.put(h.value(), lvl));
        }
        Set<EntityType<?>> bl = new HashSet<>();
        for (String s : DynamicLightsConfig.BLACKLIST.get()) {
            Identifier id = Identifier.tryParse(s.trim());
            if (id != null) BuiltInRegistries.ENTITY_TYPE.get(id).ifPresent(h -> bl.add(h.value()));
        }
        itemOverrides = items;
        entityOverrides = ents;
        blacklist = bl;
    }

    private static Integer parse(String s) {
        try {
            return Math.max(0, Math.min(15, Integer.parseInt(s.trim())));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static int itemLevel(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Item item = stack.getItem();
        Integer o = itemOverrides.get(item);
        if (o != null) return o;
        if (item instanceof BlockItem bi) return bi.getBlock().defaultBlockState().getLightEmission();
        return 0;
    }

    public static int resolve(Entity e) {
        if (e.isSpectator() || e.isInvisible()) return 0;
        EntityType<?> type = e.getType();
        if (blacklist.contains(type)) return 0;
        int level = 0;
        Integer o = entityOverrides.get(type);
        if (o != null) level = o;

        if (DynamicLightsConfig.ENTITIES.get()) {
            if (e.isOnFire()) level = Math.max(level, 15);
            if (e instanceof Blaze) level = Math.max(level, 10);
            else if (e instanceof MagmaCube) level = Math.max(level, 8);
            else if (e instanceof LargeFireball || e instanceof SmallFireball) level = Math.max(level, 12);
            else if (e instanceof Creeper c && c.isPowered()) level = Math.max(level, 6);
            else if (type == EntityType.GLOW_SQUID || type == EntityType.ALLAY) level = Math.max(level, 8);
        }

        if (e instanceof ItemEntity ie && DynamicLightsConfig.DROPPED_ITEMS.get()) {
            level = Math.max(level, itemLevel(ie.getItem()));
        }

        if (e instanceof LivingEntity le && DynamicLightsConfig.HELD_ITEMS.get()) {
            if (!DynamicLightsConfig.SELF_ONLY.get() || e instanceof Player) {
                level = Math.max(level, itemLevel(le.getMainHandItem()));
                level = Math.max(level, itemLevel(le.getOffhandItem()));
                level = Math.max(level, itemLevel(le.getItemBySlot(EquipmentSlot.HEAD)));
            }
        }

        if (level > 0 && DynamicLightsConfig.WATER_SENSITIVE.get() && e.isEyeInFluid(net.minecraft.tags.FluidTags.WATER)) {
            level /= 2;
        }
        return Math.min(level, DynamicLightsConfig.MAX_LIGHT_LEVEL.get());
    }
}
