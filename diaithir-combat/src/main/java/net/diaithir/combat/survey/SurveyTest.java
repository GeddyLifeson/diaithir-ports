package net.diaithir.combat.survey;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** /combatsurveytest: sword blocking components, build-up meters, lance bonus. */
public final class SurveyTest {
    private static int ok, bad;
    private static final StringBuilder why = new StringBuilder();
    private static void check(boolean c, String w) { if (c) ok++; else { bad++; why.append(" | ").append(w); } }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("combatsurveytest")
                .requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                .executes(c -> {
                    ok = 0; bad = 0; why.setLength(0);
                    try { run(c.getSource().getLevel()); } catch (Throwable t) { t.printStackTrace(); check(false, "exception " + t); }
                    String msg = "combat survey ok=" + ok + " bad=" + bad + why;
                    c.getSource().sendSuccess(() -> Component.literal(msg), false);
                    return bad == 0 ? 1 : 0;
                }));
    }

    private static void run(net.minecraft.server.level.ServerLevel lv) {
        ItemStack iron = new ItemStack(Items.IRON_SWORD);
        check(iron.has(DataComponents.BLOCKS_ATTACKS), "iron sword blocks");
        check(new ItemStack(Items.SHIELD).has(DataComponents.BLOCKS_ATTACKS), "shield still blocks");
        check(!new ItemStack(Items.IRON_AXE).has(DataComponents.BLOCKS_ATTACKS), "axe does not block");
        check(iron.getItem().getUseDuration(iron, null) == 72000, "sword is raisable");
        var ba = iron.get(DataComponents.BLOCKS_ATTACKS);
        check(ba != null && Math.abs(ba.damageReductions().get(0).factor() - 0.5F) < 1e-6, "half damage");
        check(SwordBlocking.isSwordName("spartanweaponry") == false && SwordBlocking.isSwordName("steel_sword") && SwordBlocking.isSwordName("rapier"), "name rules");

        LivingEntity z = EntityType.ZOMBIE.create(lv, EntitySpawnReason.COMMAND);
        lv.addFreshEntity(z);
        check(Buildup.meter(z, Buildup.Kind.BLEEDING) == 0, "meter starts empty");
        check(!Buildup.add(z, Buildup.Kind.BLEEDING, 60), "60 does not fire");
        check(Buildup.meter(z, Buildup.Kind.BLEEDING) == 60, "meter holds 60");
        check(Buildup.add(z, Buildup.Kind.BLEEDING, 50), "110 fires");
        check(Buildup.meter(z, Buildup.Kind.BLEEDING) == 0 && z.hasEffect(net.minecraft.world.effect.MobEffects.WITHER), "emptied, wither applied");
        Buildup.add(z, Buildup.Kind.BURNING, 100);
        check(z.getRemainingFireTicks() > 0, "burning fires");
        Buildup.add(z, Buildup.Kind.SHOCK, 100);
        check(z.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS), "shock slows");
        z.discard();

        check(Lance.bonus(0.05) == 0 && Lance.bonus(0.3) > 1.0F && Lance.bonus(5) == Lance.MAX_BONUS, "lance curve");
        check(Lance.isLance(new ItemStack(Items.IRON_SPEAR)), "iron spear is a lance");
        check(!Lance.isLance(new ItemStack(Items.IRON_SWORD)), "sword is not");
    }
}
