package net.diaithir.classes;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.common.NeoForge;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;

/** Applies the attribute of every rune socketed in a worn ring or charm. Kept apart so the mod loads without Curios. */
final class CuriosHook {
    private CuriosHook() {}

    static void register() {
        NeoForge.EVENT_BUS.addListener(CurioAttributeModifierEvent.class, CuriosHook::onModifiers);
    }

    private static void onModifiers(CurioAttributeModifierEvent e) {
        if (!(e.getItemStack().getItem() instanceof SocketItem)) {
            return;
        }
        List<String> runes = DiaithirClasses.socketsOf(e.getItemStack());
        for (String id : runes) {
            DiaithirClasses.Rune r = DiaithirClasses.RUNES.get(id);
            if (r == null) {
                continue;
            }
            Holder<Attribute> a = DiaithirClasses.attribute(r.attribute());
            if (a == null) {
                continue;
            }
            e.addModifier(a, new AttributeModifier(Identifier.fromNamespaceAndPath(DiaithirClasses.ID, "rune_" + id), r.amount(),
                DiaithirClasses.operation(r.op())));
        }
    }
}
