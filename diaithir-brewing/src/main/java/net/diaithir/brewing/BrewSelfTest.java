package net.diaithir.brewing;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** /brewing selftest: runs the first loaded keg recipe through a fake player and reports PASS or what failed. */
final class BrewSelfTest {
    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("brewing").requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
            .then(Commands.literal("selftest").executes(c -> {
                String result = run(c.getSource().getLevel(), BlockPos.containing(c.getSource().getPosition()));
                c.getSource().sendSuccess(() -> Component.literal(result), false);
                return 1;
            })));
    }

    private static String run(ServerLevel level, BlockPos pos) {
        BrewRecipe r = BrewRecipes.all().stream().filter(x -> x.station().equals("keg")).findFirst().orElse(null);
        if (r == null) {
            return "FAIL no keg recipe loaded";
        }
        level.setBlockAndUpdate(pos, DiaithirBrewing.KEG.get().defaultBlockState());
        Player p = FakePlayerFactory.getMinecraft(level);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        if (!use(level, p, pos, hit, new ItemStack(r.base()))) return "FAIL base not accepted";
        for (var e : r.ingredients().entrySet()) {
            for (int i = 0; i < e.getValue(); i++) {
                if (!use(level, p, pos, hit, new ItemStack(e.getKey()))) return "FAIL ingredient " + e.getKey() + " not accepted";
            }
        }
        BrewVesselEntity be = (BrewVesselEntity) level.getBlockEntity(pos);
        if (!be.busy()) return "FAIL did not start fermenting";
        for (int i = 0; i < r.ticks() && !be.done(); i++) BrewVesselEntity.tick(level, pos, be.getBlockState(), be);
        if (!be.done()) return "FAIL did not finish";
        BrewVesselEntity reloaded = new BrewVesselEntity(pos, be.getBlockState());
        reloaded.setLevel(level);
        net.minecraft.world.level.storage.TagValueOutput out = net.minecraft.world.level.storage.TagValueOutput.createWithContext(
            net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess());
        be.saveWithoutMetadata(out);
        reloaded.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(
            net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess(), out.buildResult()));
        if (!reloaded.done() || reloaded.drink.getCount() != r.servings()) return "FAIL save round trip lost the drink";
        ItemStack bottle = new ItemStack(r.container());
        p.setItemInHand(InteractionHand.MAIN_HAND, bottle);
        level.getBlockState(pos).useItemOn(bottle, level, p, InteractionHand.MAIN_HAND, hit);
        if (be.drink.getCount() != r.servings() - 1) return "FAIL serving not drawn";
        String auto = automation(level, pos, r);
        level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        return auto != null ? auto : "PASS keg brewed " + r.servings() + " " + r.drink() + ", drew one, save round trip ok, automation ok";
    }

    /** Feeds a fresh keg through its item handler (hopper path), checks an aborted insert rolls back, then draws the drinks out. */
    private static String automation(ServerLevel level, BlockPos pos, BrewRecipe r) {
        level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(pos, DiaithirBrewing.KEG.get().defaultBlockState());
        BrewVesselEntity be = (BrewVesselEntity) level.getBlockEntity(pos);
        try (var tx = Transaction.openRoot()) {
            be.automation.insert(ItemResource.of(r.base()), 1, tx);
        }
        if (be.base != Items.AIR) return "FAIL aborted insert was not rolled back";
        try (var tx = Transaction.openRoot()) {
            be.automation.insert(ItemResource.of(r.base()), 1, tx);
            for (var e : r.ingredients().entrySet()) {
                for (int i = 0; i < e.getValue(); i++) be.automation.insert(ItemResource.of(e.getKey()), 1, tx);
            }
            tx.commit();
        }
        if (!be.busy()) return "FAIL automation did not start fermenting: base=" + be.base + " held=" + be.held + " done=" + be.done();
        for (int i = 0; i < r.ticks() && !be.done(); i++) BrewVesselEntity.tick(level, pos, be.getBlockState(), be);
        int got;
        try (var tx = Transaction.openRoot()) {
            got = be.automation.extract(ItemResource.of(r.drink()), 64, tx);
            tx.commit();
        }
        return got == r.servings() ? null : "FAIL automation drew " + got + " of " + r.servings();
    }

    private static boolean use(ServerLevel level, Player p, BlockPos pos, BlockHitResult hit, ItemStack stack) {
        p.setItemInHand(InteractionHand.MAIN_HAND, stack);
        level.getBlockState(pos).useItemOn(stack, level, p, InteractionHand.MAIN_HAND, hit);
        return stack.isEmpty() || stack.getItem() == Items.AIR || stack.getCount() == 0;
    }
}
