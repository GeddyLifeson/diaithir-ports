package net.diaithir.arms;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Extra melee shapes (dagger, rapier, katana, glaive, halberd, warhammer) in each sword material, written from the behaviour of
 * Spartan Weaponry (survey part 2) with no upstream code. Data: data/diaithirarms/roster.json (tools/gen_roster.py).
 * Long shapes carry extra entity interaction range; heavy shapes swing slowly.
 */
final class Roster {
    record Entry(String id, String mat, float damage, float speed, float reach) {}
    record Data(List<Entry> items) {}

    private static Data data;
    static List<Entry> entries() { return data.items(); }

    static void register(DeferredRegister.Items items, java.util.Map<String, DeferredItem<Item>> all) {
        Data d;
        try (Reader r = new InputStreamReader(Roster.class.getResourceAsStream("/data/" + DiaithirArms.ID + "/roster.json"))) {
            d = new Gson().fromJson(r, Data.class);
        } catch (Exception e) {
            throw new IllegalStateException("roster.json missing", e);
        }
        data = d;
        for (Entry e : d.items()) {
            all.put(e.id(), items.registerItem(e.id(), Item::new, () -> props(e)));
        }
    }

    static Item.Properties props(Entry e) {
        var mat = DiaithirArms.toolMaterial(e.mat());
        float dmg = 1.0F + e.damage() + mat.attackDamageBonus();
        var b = ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, dmg, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, e.speed(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        if (e.reach() > 0) {
            b.add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(Identifier.fromNamespaceAndPath(DiaithirArms.ID, "roster_reach"), e.reach(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        }
        return new Item.Properties().sword(mat, 0.0F, 0.0F).attributes(b.build());
    }

    private Roster() {}
}
