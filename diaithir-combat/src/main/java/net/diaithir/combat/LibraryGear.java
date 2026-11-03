package net.diaithir.combat;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.Codec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Panscriptum (Library) weapons and armor as data. One registered item per weapon class and armor slot; every Library
 * entry is the same item with an {@code entry_id} component. Name, Age tier and class come from gear.tsv.gz (built by
 * tools/gear_gen.py from the entry's own text); stats come from the tier tables below. No per-entry registration.
 */
public final class LibraryGear {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, DiaithirCombat.ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DiaithirCombat.ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> ENTRY_ID = COMPONENTS.registerComponentType("entry_id",
            b -> b.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    /** class id -> base attack damage (weapon) */
    static final Map<String, Double> BASE = Map.ofEntries(Map.entry("dagger", 3.0), Map.entry("sword", 4.0), Map.entry("rapier", 3.5),
            Map.entry("greatsword", 6.0), Map.entry("axe", 5.0), Map.entry("battleaxe", 7.0), Map.entry("mace", 5.0), Map.entry("warhammer", 7.0),
            Map.entry("spear", 4.0), Map.entry("polearm", 5.0), Map.entry("staff", 2.0), Map.entry("bow", 0.0), Map.entry("longbow", 0.0));
    static final Map<String, Double> SLOT_ARMOR = Map.of("head", 2.0, "chest", 5.0, "legs", 4.0, "feet", 2.0, "shield", 3.0, "trinket", 1.0);
    static final Map<String, EquipmentSlotGroup> SLOT_GROUP = Map.of("head", EquipmentSlotGroup.HEAD, "chest", EquipmentSlotGroup.CHEST,
            "legs", EquipmentSlotGroup.LEGS, "feet", EquipmentSlotGroup.FEET, "shield", EquipmentSlotGroup.OFFHAND, "trinket", EquipmentSlotGroup.OFFHAND);
    static final Map<String, EquipmentSlot> SLOT = Map.of("head", EquipmentSlot.HEAD, "chest", EquipmentSlot.CHEST, "legs", EquipmentSlot.LEGS, "feet", EquipmentSlot.FEET);

    /** class or slot id -> registered item */
    public static final Map<String, DeferredHolder<Item, Item>> BY_CLASS = new HashMap<>();

    static {
        for (String c : BASE.keySet()) {
            boolean ranged = c.equals("bow") || c.equals("longbow");
            BY_CLASS.put(c, ITEMS.registerItem("lib_" + c, p -> ranged ? new GearBow(p) : new GearItem(p), p -> p.stacksTo(1)));
        }
        for (String s : SLOT_ARMOR.keySet())
            BY_CLASS.put(s, ITEMS.registerItem("lib_" + s, GearItem::new, p -> p.stacksTo(1)));
    }

    static class GearItem extends Item {
        GearItem(Properties p) { super(p); }
        @Override public Component getName(ItemStack s) { return LibraryGear.title(s, super.getName(s)); }
    }
    static class GearBow extends BowItem {
        GearBow(Properties p) { super(p); }
        @Override public Component getName(ItemStack s) { return LibraryGear.title(s, super.getName(s)); }
    }

    static Component title(ItemStack s, Component dflt) {
        String id = s.get(ENTRY_ID.get());
        Row r = id == null ? null : ROW.get(id);
        return r == null ? dflt : Component.literal(r.name);
    }

    // ---- table ----
    public record Row(String id, int tier, int gate, String cls, String name, boolean weapon) {}
    static final Map<String, Row> ROW = new HashMap<>();
    static final List<Row> LIST = new ArrayList<>(100_000);

    public static void load() {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new GZIPInputStream(
                LibraryGear.class.getResourceAsStream("/data/" + DiaithirCombat.ID + "/library_gear/gear.tsv.gz")), StandardCharsets.UTF_8))) {
            String ln;
            while ((ln = r.readLine()) != null) {
                String[] f = ln.split("\t", 5);
                Row row = new Row(f[0], Integer.parseInt(f[1]), Integer.parseInt(f[2]), f[3], f[4], BASE.containsKey(f[3]));
                ROW.put(row.id, row); LIST.add(row);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("diaithircombat library gear", ex);
        }
    }

    public static void register(IEventBus bus) { COMPONENTS.register(bus); ITEMS.register(bus); load(); }

    // ---- stacks ----
    private static String sid(String id) { return id.toLowerCase(Locale.ROOT); }

    public static Row row(ItemStack s) {
        if (!s.has(ENTRY_ID.get())) return null;
        return ROW.get(s.get(ENTRY_ID.get()));
    }

    /** Age gate of a library stack (planet opens_age + tier - 1), or 0 for anything else. */
    public static int tierOf(ItemStack s) { Row r = row(s); return r == null ? 0 : r.gate; }

    public static double attackDamage(String cls, int tier) { return BASE.get(cls) * (1 + 0.28 * (tier - 1)); }
    public static double armorPoints(String slot, int tier) { return SLOT_ARMOR.get(slot) * (0.5 + 0.15 * tier); }
    public static double toughness(int tier) { return tier > 4 ? 0.5 * (tier - 4) : 0; }
    public static int durability(int tier) { return (int) (120 * Math.pow(tier, 1.4)); }

    /** Tint colour for an entry: hue from the id hash, brightness from the tier. */
    static int tint(Row r) {
        int h = r.id.hashCode();
        float hue = ((h >>> 3) & 1023) / 1024f;
        return java.awt.Color.HSBtoRGB(hue, 0.35f, 0.75f + 0.03f * r.gate) & 0xFFFFFF;
    }

    public static ItemStack stack(String entryId) {
        Row r = ROW.get(entryId);
        if (r == null) return ItemStack.EMPTY;
        ItemStack s = new ItemStack(BY_CLASS.get(r.cls).get());
        s.set(ENTRY_ID.get(), r.id);
        s.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of("t" + r.gate), List.of(tint(r))));
        ItemAttributeModifiers.Builder b = ItemAttributeModifiers.builder();
        Identifier mid = Identifier.fromNamespaceAndPath(DiaithirCombat.ID, "lib_" + r.cls);
        if (r.weapon) {
            if (BASE.get(r.cls) > 0) b.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(mid, attackDamage(r.cls, r.gate), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            s.set(DataComponents.MAX_DAMAGE, durability(r.gate));
            s.set(DataComponents.DAMAGE, 0);
        } else {
            EquipmentSlotGroup g = SLOT_GROUP.get(r.cls);
            b.add(Attributes.ARMOR, new AttributeModifier(mid, armorPoints(r.cls, r.gate), AttributeModifier.Operation.ADD_VALUE), g);
            if (toughness(r.gate) > 0) b.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(mid, toughness(r.gate), AttributeModifier.Operation.ADD_VALUE), g);
            if (SLOT.containsKey(r.cls)) {
                s.set(DataComponents.EQUIPPABLE, net.minecraft.world.item.equipment.Equippable.builder(SLOT.get(r.cls))
                        .setAsset(net.minecraft.resources.ResourceKey.create(net.minecraft.world.item.equipment.EquipmentAssets.ROOT_ID,
                                Identifier.fromNamespaceAndPath(DiaithirCombat.ID, "lib_t" + r.gate))).build());
                s.set(DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(tint(r)));
                s.set(DataComponents.MAX_DAMAGE, durability(r.gate));
                s.set(DataComponents.DAMAGE, 0);
            }
        }
        s.set(DataComponents.ATTRIBUTE_MODIFIERS, b.build());
        return s;
    }

    // ---- Age gate: a piece above the player's Age does nothing ----
    public static boolean usable(ServerPlayer p, ItemStack s) { int t = tierOf(s); return t == 0 || t <= Engine.ageOf(p); }

    static void tick(ServerPlayer p) {
        if (p.tickCount % 40 != 0) return;
        for (EquipmentSlot es : SLOT.values()) {
            ItemStack s = p.getItemBySlot(es);
            if (!s.isEmpty() && !usable(p, s)) {
                p.setItemSlot(es, ItemStack.EMPTY);
                if (!p.getInventory().add(s)) p.drop(s, false);
                p.sendSystemMessage(Component.literal("Too advanced for your Age (needs Age " + tierOf(s) + ")"), true);
            }
        }
    }

    // ---- /libgear ----
    public static void commands(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("libgear").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.literal("give").then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("entry", StringArgumentType.greedyString()).executes(c -> {
                ServerPlayer sp = EntityArgument.getPlayer(c, "player");
                ItemStack s = stack(StringArgumentType.getString(c, "entry"));
                if (s.isEmpty()) { c.getSource().sendFailure(Component.literal("unknown entry")); return 0; }
                if (!sp.getInventory().add(s)) sp.drop(s, false);
                c.getSource().sendSuccess(() -> Component.literal("gave " + row(s).name + " (Age " + row(s).gate + ", " + row(s).cls + ")"), true);
                return 1;
            }))))
            .then(Commands.literal("find").then(Commands.argument("text", StringArgumentType.greedyString()).executes(c -> {
                String q = StringArgumentType.getString(c, "text").toLowerCase(Locale.ROOT);
                int n = 0;
                for (Row r : LIST) {
                    if (r.name.toLowerCase(Locale.ROOT).contains(q) || r.cls.equals(q) || r.id.toLowerCase(Locale.ROOT).contains(q)) {
                        final Row rr = r;
                        c.getSource().sendSuccess(() -> Component.literal(rr.id + "  " + rr.name + "  " + rr.cls + " A" + rr.gate), false);
                        if (++n >= 10) break;
                    }
                }
                return n;
            })))
            .then(Commands.literal("test").executes(c -> {
                int ok = 0, bad = 0;
                java.util.Set<String> seen = new java.util.HashSet<>();
                for (Row r : LIST) {
                    String k = r.cls + r.gate;
                    if (!seen.add(k)) continue;
                    ItemStack s = stack(r.id);
                    boolean good = !s.isEmpty() && row(s) == r && tierOf(s) == r.gate && s.has(DataComponents.ATTRIBUTE_MODIFIERS)
                            && s.getHoverName().getString().equals(r.name) && s.has(DataComponents.CUSTOM_MODEL_DATA)
                            && (r.weapon ? s.has(DataComponents.MAX_DAMAGE) : s.has(DataComponents.ATTRIBUTE_MODIFIERS));
                    if (good) ok++; else { bad++; c.getSource().sendFailure(Component.literal("bad " + r.id + " " + r.cls)); }
                }
                final int o = ok, b = bad;
                c.getSource().sendSuccess(() -> Component.literal("[libgear] classes x Ages ok=" + o + " bad=" + b + " of " + LIST.size() + " entries"), false);
                return b == 0 ? 1 : 0;
            }))
            .then(Commands.literal("count").executes(c -> { c.getSource().sendSuccess(() -> Component.literal(LIST.size() + " entries"), false); return LIST.size(); })));
    }
}
