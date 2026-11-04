package net.diaithir.relics.yori;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.diaithir.relics.DiaithirRelics;
import net.diaithir.relics.Equipped;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Stage 1 of the Yori3o's Relics 'n Jewelry fold (MIT, see NOTICE-YORI.md in META-INF/licenses): the stat jewellery. Each piece is a plain item that
 * adds one or two attribute modifiers while worn in a Curios slot or held in the off hand; the numbers are the upstream defaults.
 * Spells, the rage bar, dash, the void amulet and the armour/geo items are later stages (they need mixins and client UI that are not
 * ported yet). Items are written fresh for 26.1 (platform layer dropped); textures are redrawn by tools/gen_yori.py.
 */
public final class YoriStats {
    public record Bonus(String attr, double amount, AttributeModifier.Operation op, String key) {}
    public record Piece(String id, String slot, List<Bonus> bonuses) {}

    public static final List<Piece> PIECES = List.of(
        new Piece("netherite_ring", "ring", List.of(new Bonus("armor", 3.0, AttributeModifier.Operation.ADD_VALUE, "armor_netherite_ring"))),
        new Piece("emerald_ring", "ring", List.of(new Bonus("max_health", 2.0, AttributeModifier.Operation.ADD_VALUE, "max_health_emerald_ring"))),
        new Piece("diamond_ring", "ring", List.of(new Bonus("attack_damage", 1.0, AttributeModifier.Operation.ADD_VALUE, "attack_damage_diamond_ring"))),
        new Piece("amethyst_ring", "ring", List.of(new Bonus("oxygen_bonus", 0.6, AttributeModifier.Operation.ADD_VALUE, "oxygen_bonus_amethyst_ring"))),
        new Piece("quartz_ring", "ring", List.of(new Bonus("block_break_speed", 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, "block_break_speed_quartz_ring"))),
        new Piece("golden_aglet", "charm", List.of(new Bonus("movement_speed", 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, "movement_speed_golden_aglet"))),
        new Piece("amethyst_aglet", "charm", List.of(new Bonus("safe_fall_distance", 2.0, AttributeModifier.Operation.ADD_VALUE, "safe_fall_distance_amethyst_aglet"))),
        new Piece("netherite_shield_charm", "charm", List.of(
            new Bonus("knockback_resistance", 1.0, AttributeModifier.Operation.ADD_VALUE, "knockback_resistance_netherite_shield"),
            new Bonus("armor", 4.0, AttributeModifier.Operation.ADD_VALUE, "armor_netherite_shield"))));

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DiaithirRelics.ID);
    private static final Map<String, DeferredItem<Item>> ALL = new LinkedHashMap<>();

    public static void register(IEventBus bus) {
        for (Piece p : PIECES) ALL.put(p.id(), ITEMS.registerItem(p.id(), Item::new, () -> new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
        ITEMS.register(bus);
        NeoForge.EVENT_BUS.register(new YoriStats());
    }

    public static Map<String, DeferredItem<Item>> all() { return ALL; }

    public static Piece pieceOf(ItemStack s) {
        if (s.isEmpty()) return null;
        Identifier k = BuiltInRegistries.ITEM.getKey(s.getItem());
        if (!k.getNamespace().equals(DiaithirRelics.ID)) return null;
        for (Piece p : PIECES) if (p.id().equals(k.getPath())) return p;
        return null;
    }

    static Identifier modId(Bonus b) { return Identifier.fromNamespaceAndPath(DiaithirRelics.ID, "yori/" + b.key()); }

    static Holder<Attribute> attr(String name) { return BuiltInRegistries.ATTRIBUTE.get(Identifier.withDefaultNamespace(name)).orElse(null); }

    /** Pure: for a set of worn pieces, the bonuses in force (one piece of each id counts once). */
    public static List<Bonus> inForce(List<Piece> worn) {
        return PIECES.stream().filter(worn::contains).flatMap(p -> p.bonuses().stream()).toList();
    }

    @SubscribeEvent
    public void tick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || p.tickCount % 20 != 7) return;
        apply(p, Equipped.stacks(p, s -> pieceOf(s) != null).stream().map(YoriStats::pieceOf).toList());
    }

    /** Put the bonuses of the worn pieces on the entity and take the others off. */
    public static void apply(net.minecraft.world.entity.LivingEntity p, List<Piece> worn) {
        List<Bonus> on = inForce(worn);
        for (Piece piece : PIECES) {
            for (Bonus b : piece.bonuses()) {
                Holder<Attribute> h = attr(b.attr());
                AttributeInstance inst = h == null ? null : p.getAttribute(h);
                if (inst == null) continue;
                if (on.contains(b)) inst.addOrReplacePermanentModifier(new AttributeModifier(modId(b), b.amount(), b.op()));
                else inst.removeModifier(modId(b));
            }
        }
    }

    public static void commands(com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> d) {
        d.register(net.minecraft.commands.Commands.literal("yoriselftest").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)).executes(c -> {
            int ok = 0, bad = 0;
            boolean[] r = new boolean[6];
            r[0] = ALL.size() == 8 && ALL.values().stream().allMatch(DeferredItem::isBound);
            r[1] = pieceOf(new ItemStack(ALL.get("emerald_ring").get())) == PIECES.get(1) && pieceOf(new ItemStack(net.minecraft.world.item.Items.STICK)) == null;
            r[2] = inForce(List.of(PIECES.get(7))).size() == 2 && inForce(List.of()).isEmpty();
            var z = net.minecraft.world.entity.EntityType.ZOMBIE.create(c.getSource().getLevel(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            double hp0 = z.getAttributeValue(Attributes.MAX_HEALTH), ar0 = z.getAttributeValue(Attributes.ARMOR);
            apply(z, List.of(PIECES.get(1), PIECES.get(7)));
            r[3] = Math.abs(z.getAttributeValue(Attributes.MAX_HEALTH) - (hp0 + 2.0)) < 1e-6 && Math.abs(z.getAttributeValue(Attributes.ARMOR) - (ar0 + 4.0)) < 1e-6;
            apply(z, List.of(PIECES.get(1)));
            r[4] = Math.abs(z.getAttributeValue(Attributes.ARMOR) - ar0) < 1e-6 && Math.abs(z.getAttributeValue(Attributes.MAX_HEALTH) - (hp0 + 2.0)) < 1e-6;
            apply(z, List.of());
            r[5] = Math.abs(z.getAttributeValue(Attributes.MAX_HEALTH) - hp0) < 1e-6;
            for (boolean x : r) if (x) ok++; else bad++;
            String m = "yori ok=" + ok + " bad=" + bad;
            c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(m), false);
            return bad == 0 ? 1 : 0;
        }));
    }

    private YoriStats() {}
}
