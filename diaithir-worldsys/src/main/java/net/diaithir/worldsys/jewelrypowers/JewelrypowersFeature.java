package net.diaithir.worldsys.jewelrypowers;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Gems, rings and necklaces. Carried jewelry grants transient attribute bonuses, one ring and one necklace per kind, netherite variants doubled. */
public final class JewelrypowersFeature {
    private static final String ID = "diaithirworldsys";
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    private static final List<DeferredItem<Item>> ALL = new ArrayList<>();
    private static final Map<String, Jewel> JEWELS = new HashMap<>();
    private static final List<Holder<Attribute>> ATTRS = new ArrayList<>();

    private record Jewel(String kind, boolean necklace, Holder<Attribute> attr, double amount, AttributeModifier.Operation op) {
    }

    private static void plain(String id) {
        ALL.add(ITEMS.registerSimpleItem(id));
    }

    private static void jewel(String id, boolean necklace, Holder<Attribute> attr, double amount, AttributeModifier.Operation op) {
        ALL.add(ITEMS.registerSimpleItem(id, p -> p.stacksTo(1)));
        JEWELS.put(id, new Jewel(id, necklace, attr, amount, op));
        if (!ATTRS.contains(attr)) ATTRS.add(attr);
    }

    private static void jewel(String id, Holder<Attribute> attr, double amount, AttributeModifier.Operation op) {
        jewel(id, id.endsWith("_necklace"), attr, amount, op);
    }

    private static final AttributeModifier.Operation ADD = AttributeModifier.Operation.ADD_VALUE;
    private static final AttributeModifier.Operation MUL = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;

    static {
        for (String g : new String[] {"citrine", "jade", "tanzanite"}) plain("jewelry_" + g);
        jewel("jewelry_citrine_ring", Attributes.ATTACK_DAMAGE, 1.0, ADD);
        jewel("jewelry_citrine_necklace", Attributes.ATTACK_DAMAGE, 1.0, ADD);
        jewel("jewelry_jade_ring", Attributes.LUCK, 1.0, ADD);
        jewel("jewelry_jade_necklace", Attributes.LUCK, 1.0, ADD);
        jewel("jewelry_tanzanite_ring", Attributes.MOVEMENT_SPEED, 0.05, MUL);
        jewel("jewelry_tanzanite_necklace", Attributes.MOVEMENT_SPEED, 0.05, MUL);
        jewel("jewelry_ruby_necklace", Attributes.MAX_HEALTH, 2.0, ADD);
        jewel("jewelry_sapphire_necklace", Attributes.ARMOR_TOUGHNESS, 1.0, ADD);
        jewel("jewelry_topaz_necklace", Attributes.ATTACK_SPEED, 0.1, MUL);
        jewel("jewelry_copper_ring", Attributes.ATTACK_SPEED, 0.05, MUL);
        jewel("jewelry_iron_ring", Attributes.ARMOR, 1.0, ADD);
        jewel("jewelry_gold_ring", Attributes.LUCK, 0.5, ADD);
        jewel("jewelry_diamond_ring", Attributes.ARMOR, 2.0, ADD);
        jewel("jewelry_diamond_necklace", Attributes.ARMOR_TOUGHNESS, 1.0, ADD);
        jewel("jewelry_emerald_necklace", Attributes.LUCK, 1.0, ADD);
        for (String g : new String[] {"citrine", "jade", "ruby", "sapphire", "tanzanite", "topaz"}) {
            for (String t : new String[] {"ring", "necklace"}) {
                Jewel base = JEWELS.get("jewelry_" + g + "_" + t);
                if (base == null) base = JEWELS.get("jewelry_" + g + "_necklace");
                String id = "jewelry_netherite_" + g + "_" + t;
                jewel(id, t.equals("necklace"), base.attr(), base.amount() * 2, base.op());
            }
        }
        unique("arcane", Attributes.MAX_HEALTH, 2.0, ADD, Attributes.LUCK, 1.0, ADD);
        unique("archer", Attributes.MOVEMENT_SPEED, 0.05, MUL, Attributes.ATTACK_SPEED, 0.05, MUL);
        unique("attack", Attributes.ATTACK_DAMAGE, 2.0, ADD, Attributes.ATTACK_DAMAGE, 3.0, ADD);
        unique("crit", Attributes.ATTACK_SPEED, 0.1, MUL, Attributes.ATTACK_DAMAGE, 1.0, ADD);
        unique("dex", Attributes.MOVEMENT_SPEED, 0.1, MUL, Attributes.ATTACK_SPEED, 0.1, MUL);
        unique("fire", Attributes.ATTACK_DAMAGE, 1.5, ADD, Attributes.KNOCKBACK_RESISTANCE, 0.1, ADD);
        unique("frost", Attributes.ARMOR, 2.0, ADD, Attributes.MOVEMENT_SPEED, 0.05, MUL);
        unique("healing", Attributes.MAX_HEALTH, 4.0, ADD, Attributes.MAX_HEALTH, 6.0, ADD);
        unique("spell", Attributes.LUCK, 1.5, ADD, Attributes.ATTACK_SPEED, 0.1, MUL);
        unique("tank", Attributes.ARMOR, 3.0, ADD, Attributes.KNOCKBACK_RESISTANCE, 0.2, ADD);
    }

    private static void unique(String x, Holder<Attribute> ra, double rv, AttributeModifier.Operation ro, Holder<Attribute> na, double nv,
            AttributeModifier.Operation no) {
        jewel("jewelry_unique_" + x + "_ring", false, ra, rv, ro);
        jewel("jewelry_unique_" + x + "_necklace", true, na, nv, no);
    }

    private static String kindOf(String id) {
        String s = id.replace("jewelry_netherite_", "jewelry_");
        return s.endsWith("_ring") ? s.substring(0, s.length() - 5) : s.substring(0, s.length() - 9);
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.INGREDIENTS) ALL.forEach(i -> e.accept(i.get()));
        });
        NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Post.class, e -> {
            if (e.getEntity() instanceof ServerPlayer p && p.tickCount % 20 == 0) refresh(p);
        });
    }

    private static Identifier modId(String id) {
        return Identifier.fromNamespaceAndPath(ID, "jewel/" + id);
    }

    public static void refresh(ServerPlayer p) {
        Map<String, Jewel> active = new HashMap<>();
        Map<String, Boolean> seen = new HashMap<>();
        for (ItemStack s : p.getInventory().getNonEquipmentItems()) {
            if (s.isEmpty()) continue;
            Identifier key = BuiltInRegistries.ITEM.getKey(s.getItem());
            if (!ID.equals(key.getNamespace())) continue;
            Jewel j = JEWELS.get(key.getPath());
            if (j == null) continue;
            String slotKey = kindOf(key.getPath()) + (j.necklace() ? "#n" : "#r");
            if (seen.putIfAbsent(slotKey, true) != null) continue;
            active.put(key.getPath(), j);
        }
        for (Holder<Attribute> a : ATTRS) {
            AttributeInstance inst = p.getAttribute(a);
            if (inst == null) continue;
            for (String id : JEWELS.keySet()) {
                Identifier mid = modId(id);
                Jewel j = active.get(id);
                if (j == null || j.attr() != a) {
                    if (inst.getModifier(mid) != null) inst.removeModifier(mid);
                } else {
                    AttributeModifier cur = inst.getModifier(mid);
                    if (cur == null || cur.amount() != j.amount()) {
                        inst.removeModifier(mid);
                        inst.addTransientModifier(new AttributeModifier(mid, j.amount(), j.op()));
                    }
                }
            }
        }
    }
}
