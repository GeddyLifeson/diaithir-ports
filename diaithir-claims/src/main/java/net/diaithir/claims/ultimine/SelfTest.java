package net.diaithir.claims.ultimine;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/** /ultimine selftest: a fake player with an iron pickaxe breaks one block of a 12 block stone vein with Ultimine on and off. */
final class SelfTest {
    private SelfTest() {}

    static int run(ServerPlayer caller) {
        ServerLevel level = (ServerLevel) caller.level();
        FakePlayer fp = FakePlayerFactory.get(level, new GameProfile(UUID.nameUUIDFromBytes("ultimine-test".getBytes()), "UltimineTest"));
        BlockPos base = caller.blockPosition().offset(5, 3, 5);
        String report = "";
        boolean ok = true;
        for (boolean on : new boolean[] {false, true}) {
            for (int i = 0; i < 12; i++) {
                level.setBlock(base.east(i), Blocks.STONE.defaultBlockState(), 3);
            }
            level.setBlock(base.east(12), Blocks.DIRT.defaultBlockState(), 3);
            ItemStack pick = new ItemStack(Items.IRON_PICKAXE);
            fp.setItemInHand(InteractionHand.MAIN_HAND, pick);
            if (on) {
                DiaithirUltimine.ON.add(fp.getUUID());
            } else {
                DiaithirUltimine.ON.remove(fp.getUUID());
            }
            fp.gameMode.destroyBlock(base);
            int left = 0;
            for (int i = 0; i < 12; i++) {
                if (level.getBlockState(base.east(i)).is(Blocks.STONE)) {
                    left++;
                }
            }
            boolean dirt = level.getBlockState(base.east(12)).is(Blocks.DIRT);
            int wear = pick.getDamageValue();
            boolean pass = dirt && (on ? left == 0 && wear == 12 : left == 11 && wear == 1);
            ok &= pass;
            report += (pass ? "PASS " : "FAIL ") + "ultimine " + (on ? "on" : "off") + ": stone left " + left + ", tool wear " + wear + ", dirt kept " + dirt + "\n";
            for (int i = 0; i <= 12; i++) {
                level.setBlock(base.east(i), Blocks.AIR.defaultBlockState(), 3);
            }
        }
        DiaithirUltimine.ON.remove(fp.getUUID());
        for (String l : report.split("\n")) {
            caller.sendSystemMessage(Component.literal(l));
            level.getServer().sendSystemMessage(Component.literal("[ultimine selftest] " + l));
        }
        return ok ? 1 : 0;
    }
}
