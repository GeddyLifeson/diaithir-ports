package net.diaithir.relics;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Diaithir Relics (Iarmadun): unique legendary weapons, shields and trinkets for Ages III to VII. Every relic is described
 * by data/diaithirrelics/relics.json (written by tools/gen.py); the powers live in {@link Abilities} (active, on use) and
 * {@link Passives} (on hit, on hurt, worn), wired to events in {@link RelicEvents}.
 */
@Mod(DiaithirRelics.ID)
public final class DiaithirRelics {
    public static final String ID = "diaithirrelics";
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    private static final Map<String, DeferredItem<Item>> ALL = new LinkedHashMap<>();
    private static final Map<Item, Def> BY_ITEM = new java.util.IdentityHashMap<>();

    public record Tier(int durability, float speed, float bonus, int enchant, String incorrect, List<Float> spear, String repair) {}
    public record Def(String id, String kind, String tier, float extra, float speed, int durability, String active, float cooldown,
                      String passive, String slot) {
        public boolean trinket() { return !slot.isEmpty(); }
    }
    public record Data(Map<String, Tier> tiers, List<Def> items) {}

    public static final Data DATA = load();
    private static final Map<String, ToolMaterial> MATS = new LinkedHashMap<>();

    private static Data load() {
        try (Reader r = new InputStreamReader(DiaithirRelics.class.getResourceAsStream("/data/" + ID + "/relics.json"))) {
            Type t = new TypeToken<Data>() {}.getType();
            return new Gson().fromJson(r, t);
        } catch (Exception e) {
            throw new IllegalStateException("relics.json missing", e);
        }
    }

    private static TagKey<Item> itemTag(String s) {
        return TagKey.create(Registries.ITEM, Identifier.parse(s));
    }

    private static ToolMaterial material(Def d) {
        return MATS.computeIfAbsent(d.id(), k -> {
            Tier t = DATA.tiers().get(d.tier());
            TagKey<net.minecraft.world.level.block.Block> inc = switch (t.incorrect()) {
                case "netherite" -> BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
                case "iron" -> BlockTags.INCORRECT_FOR_IRON_TOOL;
                default -> BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
            };
            return new ToolMaterial(inc, d.durability(), t.speed(), t.bonus(), t.enchant(), itemTag(t.repair()));
        });
    }

    private static Item.Properties shieldProps(Def d) {
        Tier t = DATA.tiers().get(d.tier());
        float factor = d.tier().equals("III") ? 0.9F : 1.0F;
        return new Item.Properties().durability(d.durability()).component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
            .repairable(itemTag(t.repair())).enchantable(t.enchant()).equippableUnswappable(EquipmentSlot.OFFHAND)
            .delayedComponent(DataComponents.BLOCKS_ATTACKS, context -> new BlocksAttacks(0.25F, 1.0F,
                List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, factor)),
                new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
                Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                Optional.of(SoundEvents.SHIELD_BLOCK), Optional.of(SoundEvents.SHIELD_BREAK)))
            .component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK);
    }

    static Item.Properties props(Def d) {
        Tier t = DATA.tiers().get(d.tier());
        Rarity rarity = switch (d.tier()) { case "III" -> Rarity.UNCOMMON; case "IV", "V" -> Rarity.RARE; default -> Rarity.EPIC; };
        Item.Properties p = switch (d.kind()) {
            case "sword" -> new Item.Properties().sword(material(d), 3.0F + d.extra(), d.speed());
            case "dagger" -> new Item.Properties().sword(material(d), 3.0F + d.extra(), d.speed());
            case "axe", "hammer" -> new Item.Properties().axe(material(d), 6.0F + d.extra(), d.speed());
            case "spear" -> {
                List<Float> s = t.spear();
                yield new Item.Properties().spear(material(d), s.get(0), s.get(1), s.get(2), s.get(3), s.get(4), s.get(5), s.get(6), s.get(7), s.get(8));
            }
            case "shield" -> shieldProps(d);
            default -> new Item.Properties().stacksTo(1);
        };
        p = p.rarity(rarity);
        if (d.cooldown() > 0) {
            p = p.useCooldown(d.cooldown());
        } else if (d.passive().equals("mistveil")) {
            p = p.useCooldown(60);
        } else if (d.passive().equals("fatesave")) {
            p = p.useCooldown(600);
        }
        return p;
    }

    public DiaithirRelics(IEventBus bus) {
        for (Def d : DATA.items()) {
            DeferredItem<Item> it = ITEMS.registerItem(d.id(), p -> new RelicItem(p, d), () -> props(d));
            ALL.put(d.id(), it);
        }
        ITEMS.register(bus);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey().equals(CreativeModeTabs.COMBAT)) {
                ALL.values().forEach(i -> e.accept(i.get()));
            }
        });
        NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.RegisterCommandsEvent.class, e -> SelfTest.register(e.getDispatcher()));
        RelicEvents.register();
    }

    /** The relic definition of a stack, or null. */
    public static Def defOf(net.minecraft.world.item.ItemStack st) {
        if (st.isEmpty()) return null;
        if (BY_ITEM.isEmpty()) {
            for (var e : ALL.entrySet()) {
                for (Def d : DATA.items()) {
                    if (d.id().equals(e.getKey())) BY_ITEM.put(e.getValue().get(), d);
                }
            }
        }
        return BY_ITEM.get(st.getItem());
    }

    public static Map<String, DeferredItem<Item>> all() { return ALL; }
}
