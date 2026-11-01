package net.diaithir.spartan;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Spartan Weaponry for NeoForge 26.1: melee weapon patterns in wood, stone, copper, bronze and iron. */
@Mod(SpartanWeaponry.ID)
public final class SpartanWeaponry {
    public static final String ID = "spartanweaponry";
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    private static final Map<String, DeferredItem<Item>> WEAPONS = new LinkedHashMap<>();
    private static final List<DeferredItem<Item>> BASIC = new ArrayList<>();

    public static final ToolMaterial BRONZE = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 320, 5.75F, 2.0F, 12,
        TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ID, "bronze_materials")));
    public static final ToolMaterial LEATHER = new ToolMaterial(BlockTags.INCORRECT_FOR_WOODEN_TOOL, 128, 2.0F, 0.0F, 5,
        TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath("c", "leathers")));

    /** One entry of data/spartanweaponry/weapon_types.json (written by tools/gen.py). */
    public record Spec(String id, String type, String material, float damage, float mult, float speed, String kind,
                       float reach, float sweep, List<String> flags) {}

    public static final List<Spec> SPECS = load();
    public static final Map<String, Spec> BY_ID = new LinkedHashMap<>();

    private static List<Spec> load() {
        try (Reader r = new InputStreamReader(SpartanWeaponry.class.getResourceAsStream("/data/" + ID + "/weapon_types.json"))) {
            Type t = new TypeToken<List<Spec>>() {}.getType();
            return new Gson().fromJson(r, t);
        } catch (Exception e) {
            throw new IllegalStateException("weapon_types.json missing", e);
        }
    }

    private static ToolMaterial material(String id) {
        return switch (id) {
            case "wood" -> ToolMaterial.WOOD;
            case "stone" -> ToolMaterial.STONE;
            case "copper" -> ToolMaterial.COPPER;
            case "iron" -> ToolMaterial.IRON;
            case "bronze" -> BRONZE;
            case "leather" -> LEATHER;
            default -> throw new IllegalArgumentException(id);
        };
    }

    /** Builds the 26.1 item components for one weapon: attributes, durability, repair, enchanting, sword/axe tool data. */
    static Item.Properties props(Spec s) {
        ToolMaterial m = material(s.material());
        float damage = s.damage() - 1.0F + m.attackDamageBonus() * s.mult();
        double speed = s.speed() - 4.0;
        ItemAttributeModifiers.Builder b = ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, damage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, speed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        if (s.reach() > 0) {
            b.add(Attributes.ENTITY_INTERACTION_RANGE,
                new AttributeModifier(Identifier.fromNamespaceAndPath(ID, "reach"), s.reach(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        }
        if (s.sweep() > 0) {
            b.add(Attributes.SWEEPING_DAMAGE_RATIO,
                new AttributeModifier(Identifier.fromNamespaceAndPath(ID, "sweep"), s.sweep(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        }
        if (s.flags().contains("knockback")) {
            b.add(Attributes.ATTACK_KNOCKBACK,
                new AttributeModifier(Identifier.fromNamespaceAndPath(ID, "knockback"), 1.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        }
        Item.Properties p = new Item.Properties();
        switch (s.kind()) {
            case "sword" -> p = m.applySwordProperties(p, 0, 0);
            case "axe" -> p = m.applyToolProperties(p, BlockTags.MINEABLE_WITH_AXE, 0, 0, Weapon.AXE_DISABLES_BLOCKING_FOR_SECONDS);
            default -> p = p.durability(m.durability()).repairable(m.repairItems()).enchantable(m.enchantmentValue())
                .component(DataComponents.WEAPON, new Weapon(1, s.flags().contains("shield_breach") ? Weapon.AXE_DISABLES_BLOCKING_FOR_SECONDS : 0.0F));
        }
        return p.attributes(b.build());
    }

    public SpartanWeaponry(IEventBus bus) {
        for (Spec s : SPECS) {
            BY_ID.put(s.id(), s);
            WEAPONS.put(s.id(), ITEMS.registerItem(s.id(), Item::new, () -> props(s)));
        }
        for (String id : new String[] {"simple_handle", "handle", "simple_pole", "pole"}) {
            BASIC.add(ITEMS.registerSimpleItem(id));
        }
        ITEMS.register(bus);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey().equals(CreativeModeTabs.COMBAT)) {
                BASIC.forEach(i -> e.accept(i.get()));
                WEAPONS.values().forEach(i -> e.accept(i.get()));
            }
        });
        NeoForge.EVENT_BUS.register(new TraitEvents());
        NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.RegisterCommandsEvent.class, e -> SelfTest.register(e.getDispatcher()));
    }

    public static Map<String, DeferredItem<Item>> weapons() { return WEAPONS; }
}
