package net.diaithir.classes.tree;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.diaithir.classes.DiaithirClasses;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Class skill trees (survey part 2, class/skill-tree data; Skill Tree (RPG Series) is all rights reserved, so this is written from the
 * idea only). Each class (fian, sleagh, ceilg, cleirech) has nodes in data/diaithir_classes/skill_trees.json: a node has an optional
 * parent, a point cost and one attribute modifier. A player picks a class, earns one point per experience level gained, spends points
 * on nodes whose parent is unlocked, and can reset for free. Unlocked nodes are permanent attribute modifiers re-applied on login,
 * respawn and clone. State lives in world SavedData ({@link TreeData}).
 */
public final class Trees {
    public record Node(String id, String name, String parent, int cost, String attr, double amount, String op) {}
    public record Data(Map<String, List<Node>> classes) {}

    public static final Data DATA = load();

    private static Data load() {
        try (Reader r = new InputStreamReader(Trees.class.getResourceAsStream("/data/" + DiaithirClasses.ID + "/skill_trees.json"))) {
            Data d = new Gson().fromJson(r, Data.class);
            return new Data(new LinkedHashMap<>(d.classes()));
        } catch (Exception e) {
            throw new IllegalStateException("skill_trees.json missing", e);
        }
    }

    public static Node node(String cls, String id) {
        List<Node> l = DATA.classes().get(cls);
        if (l == null) return null;
        for (Node n : l) if (n.id().equals(id)) return n;
        return null;
    }

    /** Result text key for a failed unlock, or null when it can be unlocked. */
    public static String whyNot(TreeData.Entry e, Node n) {
        if (e.cls() == null) return "no class chosen";
        if (n == null) return "no such node";
        if (e.unlocked().contains(n.id())) return "already unlocked";
        if (n.parent() != null && !e.unlocked().contains(n.parent())) return "needs " + n.parent();
        if (e.points() < n.cost()) return "needs " + n.cost() + " points";
        return null;
    }

    public static Identifier modId(String cls, String node) { return Identifier.fromNamespaceAndPath(DiaithirClasses.ID, "tree/" + cls + "/" + node); }

    /** Re-apply every unlocked node of the player's class (and remove the rest). */
    public static void apply(ServerPlayer p) {
        TreeData.Entry e = TreeData.get(p.level().getServer()).entry(p.getUUID());
        for (var cls : DATA.classes().entrySet()) {
            for (Node n : cls.getValue()) {
                AttributeInstance inst = inst(p, n);
                if (inst == null) continue;
                Identifier id = modId(cls.getKey(), n.id());
                boolean on = cls.getKey().equals(e.cls()) && e.unlocked().contains(n.id());
                if (on) {
                    AttributeModifier.Operation op = n.op().equals("add_multiplied_base") ? AttributeModifier.Operation.ADD_MULTIPLIED_BASE : AttributeModifier.Operation.ADD_VALUE;
                    inst.addOrReplacePermanentModifier(new AttributeModifier(id, n.amount(), op));
                } else {
                    inst.removeModifier(id);
                }
            }
        }
    }

    static AttributeInstance inst(ServerPlayer p, Node n) {
        Holder<net.minecraft.world.entity.ai.attributes.Attribute> h = BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(n.attr())).orElse(null);
        return h == null ? null : p.getAttribute(h);
    }

    private Trees() {}
}
