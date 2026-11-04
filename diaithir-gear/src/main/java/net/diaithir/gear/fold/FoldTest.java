package net.diaithir.gear.fold;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import java.util.UUID;
import net.diaithir.gear.fold.lgpl.AnvilRepairing;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/** /foldselftest: magnets, repair kits and anvil repair with a fake player in the real overworld at spawn. */
public final class FoldTest {
    private static int ok, bad;
    private static final StringBuilder why = new StringBuilder();

    private static void check(boolean c, String what) { if (c) ok++; else { bad++; why.append(" | ").append(what); System.out.println("[foldselftest] BAD " + what); } }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("foldselftest")
                .requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                .executes(c -> {
                    ok = 0; bad = 0; why.setLength(0);
                    try { run(c.getSource().getLevel()); } catch (Throwable t) { t.printStackTrace(); check(false, "exception " + t); }
                    String msg = "folds ok=" + ok + " bad=" + bad + why;
                    System.out.println("[foldselftest] " + msg);
                    c.getSource().sendSuccess(() -> Component.literal(msg), false);
                    return bad == 0 ? 1 : 0;
                }));
    }

    private static void run(ServerLevel lv) {
        var p = FakePlayerFactory.get(lv, new GameProfile(UUID.fromString("00000000-0000-0000-0000-00000000f01d"), "foldtest"));
        BlockPos at = new BlockPos(8, lv.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, 8, 8) + 4, 8);
        lv.getChunk(at);
        p.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
        p.setNoGravity(true);

        // magnets
        ItemEntity near = new ItemEntity(lv, at.getX() + 3.0, at.getY(), at.getZ() + 0.5, new ItemStack(Items.DIRT));
        ItemEntity far = new ItemEntity(lv, at.getX() + 7.0, at.getY(), at.getZ() + 0.5, new ItemStack(Items.DIRT));
        ItemEntity young = new ItemEntity(lv, at.getX() + 2.0, at.getY(), at.getZ() + 0.5, new ItemStack(Items.DIRT));
        ExperienceOrb orb = new ExperienceOrb(lv, at.getX() + 0.5, at.getY(), at.getZ() + 3.0, 5);
        boolean a1 = lv.addFreshEntity(near), a2 = lv.addFreshEntity(far), a3 = lv.addFreshEntity(young), a4 = lv.addFreshEntity(orb);
        check(a1 && a2 && a3 && a4, "entities added " + a1 + a2 + a3 + a4 + " removed=" + near.isRemoved());
        near.tickCount = 10; far.tickCount = 10; orb.tickCount = 10; young.tickCount = 1;
        check(Magnets.radiusOf(new ItemStack(Folds.SUPER_ITEM_MAGNET.get())) == 8.0 && Magnets.radiusOf(new ItemStack(Folds.ITEM_MAGNET.get())) == 4.0, "radii");
        check(lv.isPositionEntityTicking(at), "test spot is entity ticking (run /forceload add 0 0 16 16 and wait first)");
        int n = Magnets.pull(p, 4.0);
        check(n == 2, "pull 4 m took near item and orb, got " + n);
        check(near.distanceToSqr(p) < 1.0 && orb.distanceToSqr(p) < 1.0, "near ones are on the player");
        check(far.distanceToSqr(p) > 16.0, "far item stays");
        check(young.distanceToSqr(p) > 1.0, "young item stays");
        Magnets.pull(p, 8.0);
        check(far.distanceToSqr(p) < 1.0, "super radius takes the far item");
        near.discard(); far.discard(); young.discard(); orb.discard();

        // repair kits
        ItemStack kit = new ItemStack(Folds.COPPER_REPAIR_KIT.get());
        ItemStack pick = new ItemStack(Items.IRON_PICKAXE);
        pick.setDamageValue(200);
        check(kit.getMaxDamage() == 300, "copper kit pool 300");
        int moved = RepairKit.transfer(kit, pick);
        check(moved == 200 && pick.getDamageValue() == 0 && kit.getDamageValue() == 200, "transfer 200");
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.setDamageValue(500);
        moved = RepairKit.transfer(kit, sword);
        check(moved == 100 && sword.getDamageValue() == 400 && kit.getDamageValue() == 300, "kit stops at its pool, moved " + moved);
        check(Folds.NETHERITE_REPAIR_KIT.get().getDefaultInstance().getMaxDamage() == 5000, "netherite pool 5000");

        // anvil
        BlockPos ap = at.below(5);
        lv.setBlock(ap, Blocks.DAMAGED_ANVIL.defaultBlockState(), 3);
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_INGOT, 2));
        InteractionResult r = AnvilRepairing.attempt(p, lv, lv.getBlockState(ap), ap, 1.0);
        check(r == InteractionResult.SUCCESS && lv.getBlockState(ap).is(Blocks.CHIPPED_ANVIL), "damaged -> chipped");
        check(p.getMainHandItem().getCount() == 1, "ingot used");
        AnvilRepairing.attempt(p, lv, lv.getBlockState(ap), ap, 1.0);
        check(lv.getBlockState(ap).is(Blocks.ANVIL), "chipped -> anvil");
        check(AnvilRepairing.attempt(p, lv, lv.getBlockState(ap), ap, 1.0) == InteractionResult.PASS, "plain anvil: pass");
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_INGOT, 1));
        lv.setBlock(ap, Blocks.CHIPPED_ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, net.minecraft.core.Direction.EAST), 3);
        AnvilRepairing.attempt(p, lv, lv.getBlockState(ap), ap, 0.0);
        check(lv.getBlockState(ap).is(Blocks.CHIPPED_ANVIL) && p.getMainHandItem().isEmpty(), "chance 0 fails but spends the ingot");
        lv.setBlock(ap, Blocks.AIR.defaultBlockState(), 3);
        p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }
}
