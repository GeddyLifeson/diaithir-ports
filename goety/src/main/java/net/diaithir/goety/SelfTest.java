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
