package net.diaithir.arms;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** /armsselftest: builds every stack and checks the components that matter for its kind. */
public final class SelfTest {
    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("armsselftest").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)).executes(c -> {
            int ok = 0, bad = 0;
            for (var s : DiaithirArms.DATA.items()) {
                ItemStack st = new ItemStack(DiaithirArms.all().get(s.id()).get());
                boolean good = st.getMaxDamage() > 0 && switch (s.kind()) {
                    case "armor" -> st.has(DataComponents.EQUIPPABLE) && st.has(DataComponents.ATTRIBUTE_MODIFIERS);
                    case "sword", "axe", "spear" -> st.has(DataComponents.ATTRIBUTE_MODIFIERS) && st.has(DataComponents.WEAPON);
                    case "shield" -> st.has(DataComponents.BLOCKS_ATTACKS) && st.has(DataComponents.EQUIPPABLE);
                    case "crossbow" -> st.has(DataComponents.CHARGED_PROJECTILES);
                    default -> true;
                };
                if (good) ok++; else { bad++; System.out.println("[armsselftest] BAD " + s.id()); }
            }
            for (var e : Roster.entries()) {
                ItemStack st = new ItemStack(DiaithirArms.all().get(e.id()).get());
                var mods = st.get(DataComponents.ATTRIBUTE_MODIFIERS);
                boolean reach = mods != null && mods.modifiers().stream().anyMatch(m -> m.attribute().is(net.minecraft.world.entity.ai.attributes.Attributes.ENTITY_INTERACTION_RANGE));
                if (st.getMaxDamage() > 0 && st.has(DataComponents.WEAPON) && reach == (e.reach() > 0)) ok++; else { bad++; System.out.println("[armsselftest] BAD roster " + e.id()); }
            }
            {
                boolean g1 = net.diaithir.arms.gadgets.SentryTurret.loaded(0, 2, 1) == 8 && net.diaithir.arms.gadgets.SentryTurret.loaded(30, 5, 2) == net.diaithir.arms.gadgets.SentryTurret.MAX;
                boolean g2 = net.diaithir.arms.gadgets.MiningLaser.canCut(1.5F) && !net.diaithir.arms.gadgets.MiningLaser.canCut(50F) && !net.diaithir.arms.gadgets.MiningLaser.canCut(-1F);
                boolean g3 = DiaithirArms.SENTRY_TURRET.isBound() && new ItemStack(DiaithirArms.LASER.get()).getMaxDamage() == 600;
                var ts = DiaithirArms.SENTRY_TURRET.get().defaultBlockState();
                boolean g4 = ts.getValue(net.diaithir.arms.gadgets.SentryTurret.AMMO) == 0;
                for (boolean x : new boolean[] {g1, g2, g3, g4}) if (x) ok++; else { bad++; System.out.println("[armsselftest] BAD gadgets " + g1 + g2 + g3 + g4); }
            }
            ItemStack helm = new ItemStack(DiaithirArms.all().get("bone_helmet").get()), look = new ItemStack(DiaithirArms.all().get("steel_helmet").get());
            ItemStack tm = TransmogRecipe.apply(helm, look);
            boolean t1 = tm.get(DataComponents.EQUIPPABLE).assetId().equals(look.get(DataComponents.EQUIPPABLE).assetId())
                    && !tm.get(DataComponents.EQUIPPABLE).assetId().equals(helm.get(DataComponents.EQUIPPABLE).assetId())
                    && tm.is(helm.getItem()) && tm.getMaxDamage() == helm.getMaxDamage();
            boolean t2 = tm.get(DataComponents.ITEM_MODEL) != null && tm.get(DataComponents.ITEM_MODEL).equals(look.get(DataComponents.ITEM_MODEL));
            boolean t3 = DiaithirArms.TRANSMOG_TABLE.isBound();
            if (t1 && t2 && t3) ok += 3; else { bad++; System.out.println("[armsselftest] BAD transmog " + t1 + t2 + t3); }
            System.out.println("[armsselftest] ok=" + ok + " bad=" + bad + " parts=" + DiaithirArms.DATA.parts().size());
            int fo = ok, fb = bad;
            c.getSource().sendSuccess(() -> Component.literal("arms ok=" + fo + " bad=" + fb), false);
            return fb == 0 ? 1 : 0;
        }));
    }
}
