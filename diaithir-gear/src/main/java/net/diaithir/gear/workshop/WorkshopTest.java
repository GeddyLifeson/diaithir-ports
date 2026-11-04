package net.diaithir.gear.workshop;

import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.JsonOps;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.diaithir.gear.GearConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/** /workshoptest: stat maths, then a full bench cycle with a fake player (Age gate, parts, build, honing, improvement, schematic, codec). */
public final class WorkshopTest {
    private static int ok, bad;

    private static void check(boolean c, String what) {
        if (c) ok++; else { bad++; System.out.println("[workshoptest] BAD " + what); }
    }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("workshoptest")
                .requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                .executes(c -> {
                    ok = 0; bad = 0;
                    boolean ign = GearConfig.IGNORE_AGE.getAsBoolean();
                    try { run(c.getSource().getLevel()); } catch (Throwable t) { t.printStackTrace(); check(false, "exception " + t); }
                    finally { GearConfig.IGNORE_AGE.set(ign); }
                    String msg = "workshop ok=" + ok + " bad=" + bad;
                    System.out.println("[workshoptest] " + msg);
                    c.getSource().sendSuccess(() -> Component.literal(msg), false);
                    return bad == 0 ? 1 : 0;
                }));
    }

    private static Craft.Stats sword(String head, String handle) {
        Craft.Blueprint b = Catalog.blueprints().get("sword");
        Map<String, Craft.Material> m = new LinkedHashMap<>();
        m.put("head", Catalog.materials().get(head)); m.put("guard", Catalog.materials().get("iron"));
        m.put("handle", Catalog.materials().get(handle)); m.put("binding", Catalog.materials().get("leather"));
        return Craft.assemble(b, m, 0, 0);
    }

    static void run(ServerLevel lv) {
        // data
        check(Catalog.materials().size() >= 14, "materials loaded " + Catalog.materials().size());
        check(Catalog.blueprints().size() >= 25, "blueprints loaded " + Catalog.blueprints().size());
        check(Catalog.forStation(Workshop.SMITHY).size() >= 5, "smithy has blueprints (vanilla bases)");
        check(Catalog.blueprints().values().stream().allMatch(b -> !b.slots().isEmpty()), "every blueprint has slots");
        check(Catalog.blueprints().containsKey("ha_yumi") && Catalog.blueprints().get("ha_yumi").station().equals(Workshop.WORKSHOP), "historical bows are on the workshop bench");
        // maths: better materials, better stats; lighter handle swings faster; honing helps
        Craft.Stats bone = sword("bone", "wood"), iron = sword("iron", "wood"), steel = sword("steel", "wood"), star = sword("star_lattice", "wood");
        check(bone.damage() < iron.damage() && iron.damage() < steel.damage() && steel.damage() < star.damage(), "damage rises with material: " + bone.damage() + " " + iron.damage() + " " + steel.damage() + " " + star.damage());
        check(iron.massKg() > 0.3 && iron.massKg() < 3, "iron sword mass in kg " + iron.massKg());
        check(star.massKg() < steel.massKg(), "star lattice lighter than steel");
        Craft.Blueprint sb = Catalog.blueprints().get("sword");
        Map<String, Craft.Material> sm = new LinkedHashMap<>();
        sb.slots().forEach(s -> sm.put(s.name(), Catalog.materials().get(s.name().equals("head") || s.name().equals("guard") ? "steel" : s.name().equals("handle") ? "wood" : "leather")));
        check(Craft.assemble(sb, sm, 5, 0).damage() > Craft.assemble(sb, sm, 0, 0).damage(), "honing level 5 raises damage");
        check(Craft.honeLevel(0) == 0 && Craft.honeLevel(100) == 1 && Craft.honeLevel(3100) == 5 && Craft.honeLevel(99999) == 5, "hone thresholds");
        check(Craft.requiredAge(sb, sm) == 5, "required age follows best material");

        // bench cycle
        BlockPos sp = lv.getRespawnData().pos();
        BlockPos pos = new BlockPos(sp.getX(), lv.getMaxY() - 40, sp.getZ());
        lv.setBlock(pos, Workshop.SMITHY_BLOCK.get().defaultBlockState(), 3);
        StationEntity be = (StationEntity) lv.getBlockEntity(pos);
        check(be != null, "bench has a block entity");
        ServerPlayer fp = FakePlayerFactory.get(lv, new GameProfile(UUID.fromString("00000000-0000-0000-0000-00000000d0df"), "workshop_test"));
        fp.setData(Workshop.KNOWN.get(), new java.util.ArrayList<>());
        GearConfig.IGNORE_AGE.set(false);
        be.sneakUse(fp);
        check(be.blueprint.equals(Catalog.forStation(Workshop.SMITHY).get(0).id()), "sneak picks first blueprint " + be.blueprint);
        while (!be.blueprint.equals("sword")) be.sneakUse(fp);
        ItemStack ingots = new ItemStack(Items.IRON_INGOT, 5);
        be.place(fp, ingots);
        check(be.parts.isEmpty() && ingots.getCount() == 5, "Age gate refuses iron at default Age");
        GearConfig.IGNORE_AGE.set(true);
        be.place(fp, ingots);
        check(be.parts.size() == 1 && ingots.getCount() == 3, "head took 2 ingots, parts " + be.parts);
        be.place(fp, ingots);
        check(be.parts.containsKey("guard") && ingots.getCount() == 2, "second iron fills the guard");
        be.place(fp, new ItemStack(Items.OAK_PLANKS, 4));
        check(be.parts.containsKey("handle"), "planks fill the handle");
        be.take(fp);
        check(be.parts.size() == 3, "incomplete bench gives nothing");
        be.place(fp, new ItemStack(Items.LEATHER, 2));
        fp.getInventory().clearContent();
        be.take(fp);
        ItemStack made = fp.getInventory().getItem(0);
        Assembly a = made.get(Workshop.ASSEMBLY.get());
        check(a != null && a.blueprint().equals("sword") && a.parts().get("head").equals("iron"), "finished sword carries its assembly " + a);
        check(be.parts.isEmpty() && be.spent.isEmpty(), "bench reset after take");
        var mods = made.get(DataComponents.ATTRIBUTE_MODIFIERS);
        check(mods != null && mods.modifiers().stream().anyMatch(e -> e.attribute().is(Attributes.ATTACK_DAMAGE)), "sword has an attack damage modifier");
        check(made.has(DataComponents.CUSTOM_MODEL_DATA) && made.has(DataComponents.ITEM_MODEL) && made.has(DataComponents.ITEM_NAME), "visual and name components set");
        check(made.getMaxDamage() == Workshop.stats(Catalog.blueprints().get("sword"), a).durability(), "durability from stats " + made.getMaxDamage());

        // honing
        double before = ((net.minecraft.world.item.component.ItemAttributeModifiers) made.get(DataComponents.ATTRIBUTE_MODIFIERS)).modifiers().stream().filter(e -> e.attribute().is(Attributes.ATTACK_DAMAGE)).findFirst().get().modifier().amount();
        Workshop.addUse(fp, made, 100);
        double after = ((net.minecraft.world.item.component.ItemAttributeModifiers) made.get(DataComponents.ATTRIBUTE_MODIFIERS)).modifiers().stream().filter(e -> e.attribute().is(Attributes.ATTACK_DAMAGE)).findFirst().get().modifier().amount();
        check(made.get(Workshop.ASSEMBLY.get()).hone() == 1 && after > before, "100 uses hone to level 1 and raise damage " + before + " -> " + after);

        // improvement from the off hand
        fp.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.FLINT, 2));
        be.service(fp, made);
        check(made.get(Workshop.ASSEMBLY.get()).improvements().contains("keen") && fp.getOffhandItem().getCount() == 1, "flint adds keen edge");
        be.service(fp, made);
        check(made.get(Workshop.ASSEMBLY.get()).improvements().size() == 1 && fp.getOffhandItem().getCount() == 1, "same improvement is refused");

        // browsing: an item of the right kind picks its blueprint
        be.place(fp, new ItemStack(Items.IRON_AXE));
        check(be.blueprint.equals("axe"), "holding an iron axe selects the axe blueprint, got " + be.blueprint);
        while (!be.blueprint.equals("sword")) be.sneakUse(fp);

        // refund
        be.place(fp, new ItemStack(Items.IRON_INGOT, 3));
        check(!be.spent.isEmpty(), "spent recorded");
        fp.getInventory().clearContent();
        be.sneakUse(fp);
        check(be.parts.isEmpty() && fp.getInventory().countItem(Items.IRON_INGOT) == 2, "sneak returns the spent stack");

        // schematic gate
        Craft.Blueprint real = Catalog.blueprints().get("dagger");
        Craft.Blueprint locked = new Craft.Blueprint("testlock", real.station(), real.base(), real.age(), real.kind(), real.baseDamage(), real.baseSpeed(), real.baseDurability(), real.refMassKg(), 1, true, real.model(), real.slots());
        check(!Workshop.knows(fp, locked), "schematic blueprint unknown at first");
        Workshop.learn(fp, "testlock");
        check(Workshop.knows(fp, locked), "schematic blueprint known after learning");

        // codec round trip
        Assembly rt = new Assembly("sword", Map.of("head", "iron"), 7, List.of("keen"));
        JsonElement j = Assembly.CODEC.encodeStart(JsonOps.INSTANCE, rt).getOrThrow();
        check(Assembly.CODEC.parse(JsonOps.INSTANCE, j).getOrThrow().equals(rt), "assembly codec round trip");
        lv.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
    }
}
