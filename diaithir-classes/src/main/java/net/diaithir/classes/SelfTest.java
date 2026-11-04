package net.diaithir.classes;

import com.mojang.brigadier.CommandDispatcher;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;

/** /classesselftest: builds every stack, checks components, the rune and ring logic, and that every spell and set JSON loaded. */
public final class SelfTest {
    private static void bad(List<String> out, String m) {
        out.add(m);
        System.out.println("[classesselftest] BAD " + m);
    }

    private static int count(CommandSourceStack src, String registry, List<String> wanted, List<String> out) {
        var key = ResourceKey.createRegistryKey(Identifier.parse(registry));
        var lookup = src.getServer().registryAccess().lookup(key);
        if (lookup.isEmpty()) {
            bad(out, "registry missing " + registry);
            return 0;
        }
        HolderLookup.RegistryLookup<?> reg = lookup.get();
        int ok = 0;
        for (String id : wanted) {
            var rk = ResourceKey.create((ResourceKey) key, Identifier.parse(id));
            if (reg.get((ResourceKey) rk).isPresent()) {
                ok++;
            } else {
                bad(out, registry + " entry did not load: " + id);
            }
        }
        return ok;
    }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("classesselftest").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)).executes(c -> {
            List<String> out = new ArrayList<>();
            int ok = 0;
            for (var s : DiaithirClasses.DATA.items()) {
                ItemStack st = new ItemStack(DiaithirClasses.all().get(s.id()).get());
                boolean good = st.getMaxDamage() > 0 && switch (s.kind()) {
                    case "armor" -> st.has(DataComponents.EQUIPPABLE) && st.has(DataComponents.ATTRIBUTE_MODIFIERS);
                    case "sword", "spear" -> st.has(DataComponents.ATTRIBUTE_MODIFIERS) && st.has(DataComponents.WEAPON);
                    default -> true;
                };
                if (good && s.extras() != null) {
                    var mods = st.get(DataComponents.ATTRIBUTE_MODIFIERS);
                    for (var x : s.extras()) {
                        boolean found = mods != null && mods.modifiers().stream().anyMatch(en ->
                            en.attribute().unwrapKey().map(k -> k.identifier().toString()).orElse("").equals(x.attribute()));
                        if (!found) {
                            good = false;
                        }
                    }
                }
                if (good) ok++; else bad(out, "item " + s.id());
            }
            int runes = 0;
            for (var r : DiaithirClasses.DATA.runes()) {
                if (DiaithirClasses.attribute(r.attribute()) != null) runes++; else bad(out, "rune attribute " + r.attribute());
            }
            int rings = 0;
            for (var r : DiaithirClasses.DATA.rings()) {
                ItemStack ring = new ItemStack(DiaithirClasses.all().get(r.id()).get());
                int sockets = 0;
                for (var ru : DiaithirClasses.DATA.runes()) {
                    ItemStack rune = new ItemStack(DiaithirClasses.all().get("rune_" + ru.id()).get(), 2);
                    String err = SocketItem.socket(ring, rune);
                    if (err == null && rune.getCount() == 1) sockets++;
                    else if (!"full".equals(err)) bad(out, "socket " + r.id() + " " + ru.id() + " -> " + err);
                }
                ItemStack dup = new ItemStack(DiaithirClasses.all().get("rune_" + DiaithirClasses.DATA.runes().get(0).id()).get());
                if (sockets == r.sockets() && DiaithirClasses.socketsOf(ring).size() == r.sockets()) rings++;
                else bad(out, "ring " + r.id() + " sockets " + sockets + " of " + r.sockets());
            }
            int containers = 0;
            java.util.Map<?, ?> assigned = java.util.Map.of();
            try {
                assigned = (java.util.Map<?, ?>) Class.forName("net.spell_engine.internals.container.SpellAssignments").getField("containers").get(null);
            } catch (ReflectiveOperationException e) {
                bad(out, "SpellAssignments not reachable: " + e);
            }
            for (String id : DiaithirClasses.DATA.containers()) {
                if (assigned.containsKey(Identifier.parse(id))) containers++;
                else bad(out, "no spell assignment for " + id);
            }
            int spells = count(c.getSource(), "spell", DiaithirClasses.DATA.spells(), out);
            int sets = count(c.getSource(), "equipment_set", DiaithirClasses.DATA.sets(), out);
            String msg = "classes items=" + ok + "/" + DiaithirClasses.DATA.items().size() + " runes=" + runes + "/" + DiaithirClasses.DATA.runes().size()
                + " rings=" + rings + "/" + DiaithirClasses.DATA.rings().size() + " spells=" + spells + "/" + DiaithirClasses.DATA.spells().size()
                + " sets=" + sets + "/" + DiaithirClasses.DATA.sets().size() + " containers=" + containers + "/" + DiaithirClasses.DATA.containers().size() + " bad=" + out.size();
            System.out.println("[classesselftest] " + msg);
            c.getSource().sendSuccess(() -> Component.literal(msg), false);
            return out.isEmpty() ? 1 : 0;
        }));
    }
}
