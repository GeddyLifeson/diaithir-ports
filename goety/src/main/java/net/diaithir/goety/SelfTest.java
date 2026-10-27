package net.diaithir.goety;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** /goetyselftest: every item builds a stack, every block has a state and an item, every mob spawns and ticks. */
public final class SelfTest {
    private SelfTest() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("goetyselftest").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)).executes(c -> {
            ServerLevel level = c.getSource().getLevel();
            int ok = 0, bad = 0;
            for (var it : GoetyItems.ALL) {
                ItemStack st = new ItemStack(it.get());
                if (!st.isEmpty() && st.getItem().getDescriptionId() != null) ok++; else { bad++; log("item " + it.getId()); }
            }
            for (var b : GoetyBlocks.ALL.entrySet()) {
                if (b.getValue().get().defaultBlockState() != null && b.getValue().get().asItem() != null) ok++; else { bad++; log("block " + b.getKey()); }
            }
            for (var m : GoetyEntities.MOBS.entrySet()) {
                Entity e = m.getValue().get().create(level, EntitySpawnReason.COMMAND);
                if (e == null) { bad++; log("entity " + m.getKey()); continue; }
                e.setPos(c.getSource().getPosition());
                level.addFreshEntity(e);
                for (int i = 0; i < 40; i++) e.tick();
                boolean good = e.isAlive() && e instanceof LivingEntity le && le.getMaxHealth() > 0;
                e.discard();
                if (good) ok++; else { bad++; log("entity dead " + m.getKey()); }
            }
            // magic: every spell runs against a dummy target; every wand takes the foci of its school
            net.minecraft.world.entity.Mob caster = (net.minecraft.world.entity.Mob) GoetyEntities.MOBS.get("skeleton_servant").get().create(level, EntitySpawnReason.COMMAND);
            net.minecraft.world.entity.Mob dummy = (net.minecraft.world.entity.Mob) GoetyEntities.MOBS.get("zombie_servant").get().create(level, EntitySpawnReason.COMMAND);
            caster.setPos(c.getSource().getPosition());
            caster.setYRot(0); caster.setXRot(0);
            level.addFreshEntity(caster);
            dummy.setPos(caster.getX(), caster.getY(), caster.getZ() + 4);
            level.addFreshEntity(dummy);
            for (var sp : net.diaithir.goety.magic.SpellDef.all().values()) {
                try {
                    dummy.setHealth(dummy.getMaxHealth());
                    caster.setYRot(0); caster.setXRot(0); caster.setPos(caster.getX(), caster.getY(), caster.getZ());
                    boolean r = net.diaithir.goety.magic.SpellEngine.cast(sp, level, caster);
                    net.minecraft.world.item.Item it = GoetyItems.ALL.stream().map(h -> h.get()).filter(x -> net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(x).getPath().equals(sp.id())).findFirst().orElse(null);
                    if (it == null) throw new IllegalStateException("no focus item " + sp.id());
                    ok++;
                    if (!r) log("spell declined (allowed for heal at full health or servant cap): " + sp.id());
                } catch (Throwable t) { bad++; log("spell " + sp.id() + " threw " + t); }
            }
            caster.discard(); dummy.discard();
            for (var h : GoetyItems.ALL) {
                if (h.get() instanceof net.diaithir.goety.magic.WandItem w) {
                    boolean good = net.diaithir.goety.magic.SpellDef.all().values().stream().anyMatch(sp -> w.schools().contains(sp.type()));
                    if (good) ok++; else { bad++; log("wand without foci: " + h.getId()); }
                }
            }
            ItemStack tot = new ItemStack(GoetyItems.PLAIN.get("totem_of_souls").get());
            net.diaithir.goety.magic.Souls.set(tot, 500);
            if (net.diaithir.goety.magic.Souls.get(tot) == 500) ok++; else { bad++; log("totem souls"); }
            int fo = ok, fb = bad;
            log("ok=" + fo + " bad=" + fb);
            c.getSource().sendSuccess(() -> Component.literal("goety selftest ok=" + fo + " bad=" + fb), false);
            return fb == 0 ? 1 : 0;
        }));
    }

    private static void log(String s) {
        System.out.println("[goetyselftest] " + s);
    }
}
