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
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** /brewing selftest: runs the first loaded keg recipe through a fake player and reports PASS or what failed. */
final class BrewSelfTest {
    private static final org.slf4j.Logger LOG = LogUtils.getLogger();

    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("brewing").requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
            .then(Commands.literal("selftest").executes(c -> {
                String result = run(c.getSource().getLevel(), BlockPos.containing(c.getSource().getPosition()));
                c.getSource().sendSuccess(() -> Component.literal(result), false);
                return 1;
            })));
    }

    private static String run(ServerLevel level, BlockPos pos) {
        if (BrewRecipes.all().isEmpty()) {
            return "FAIL no recipes loaded";
        }
        java.util.List<String> failures = new java.util.ArrayList<>();
        String cellar = cellar(level, pos);
        if (cellar != null) {
            failures.add(cellar);
        }
        for (BrewRecipe r : BrewRecipes.all()) {
            String fail = one(level, pos, r);
            if (fail != null) {
                failures.add(fail + " (recipe " + BuiltInRegistries.ITEM.getKey(r.drink()) + " on " + r.station() + ")");
            }
        }
        failures.forEach(f -> LOG.warn("brewing selftest: {}", f));
        int n = BrewRecipes.all().size();
        return failures.isEmpty() ? "PASS " + n + " recipes brewed, drew, round-tripped and automated"
            : "FAIL " + failures.size() + " of " + n + " recipes, first: " + failures.get(0) + " (all are in the log)";
    }

    /** Heat flips when its cause is placed, a poor cellar can spoil only recipes that name a spoiled item, and rates rise with score. Darkness and shelter read the light engine, which updates asynchronously, so they are not flipped here. */
    private static String cellar(ServerLevel level, BlockPos pos) {
        var air = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        var heat = pos.east();
        level.setBlockAndUpdate(heat, air);
        boolean coolBefore = Cellar.cool(level, pos);
        level.setBlockAndUpdate(heat, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState());
        boolean coolAfter = Cellar.cool(level, pos);
        level.setBlockAndUpdate(heat, air);
        if (!coolBefore || coolAfter) return "FAIL cellar: a lit campfire beside the vessel should make it warm (before " + coolBefore + ", after " + coolAfter + ")";
        BrewRecipe sample = new BrewRecipe("keg", Items.WATER_BUCKET, Items.AIR, java.util.Map.of(), 1, Items.HONEY_BOTTLE, 1, Items.GLASS_BOTTLE, Items.DIRT, 1.0F);
        BrewRecipe never = new BrewRecipe("keg", Items.WATER_BUCKET, Items.AIR, java.util.Map.of(), 1, Items.HONEY_BOTTLE, 1, Items.GLASS_BOTTLE, Items.AIR, 1.0F);
        var rnd = level.getRandom();
        if (!Cellar.spoils(sample, 0, rnd) || Cellar.spoils(sample, 1, rnd) || Cellar.spoils(sample, Cellar.BEST, rnd) || Cellar.spoils(never, 0, rnd)) return "FAIL cellar: spoilage rules";
        if (Cellar.rate(0) >= Cellar.rate(1) || Cellar.rate(2) >= Cellar.rate(Cellar.BEST)) return "FAIL cellar: rates should rise with score";
        return null;
    }

    private static String one(ServerLevel level, BlockPos pos, BrewRecipe r) {
        BrewRecipe winner = BrewRecipes.all().stream().filter(x -> x.matches(r.station(), r.base(), r.ingredients())).findFirst().orElse(r);
        if (winner != r) {
            return "AMBIGUOUS shadowed by " + BuiltInRegistries.ITEM.getKey(winner.drink()) + ", identical station, base and ingredients";
        }
        var block = DiaithirBrewing.block(r.station());
        level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(pos, block.get().defaultBlockState());
        Player p = FakePlayerFactory.getMinecraft(level);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        if (r.base() != Items.AIR && !use(level, p, pos, hit, new ItemStack(r.base()))) return "FAIL base not accepted";
        for (var e : r.ingredients().entrySet()) {
            for (int i = 0; i < e.getValue(); i++) {
                if (!use(level, p, pos, hit, new ItemStack(e.getKey()))) return "FAIL ingredient " + e.getKey() + " not accepted";
            }
        }
        BrewVesselEntity be = (BrewVesselEntity) level.getBlockEntity(pos);
        if (!be.busy() && be.settle < 0) return "FAIL did not start fermenting";
        for (int i = 0; i < r.ticks() * 2 + BrewVesselEntity.SETTLE_TICKS + 5 && !be.done(); i++) BrewVesselEntity.tick(level, pos, be.getBlockState(), be);
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
        return auto;
    }

    /** Feeds a fresh keg through its item handler (hopper path), checks an aborted insert rolls back, then draws the drinks out. */
    private static String automation(ServerLevel level, BlockPos pos, BrewRecipe r) {
        level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(pos, DiaithirBrewing.block(r.station()).get().defaultBlockState());
        BrewVesselEntity be = (BrewVesselEntity) level.getBlockEntity(pos);
        try (var tx = Transaction.openRoot()) {
            be.automation.insert(ItemResource.of(r.base() != Items.AIR ? r.base() : r.ingredients().keySet().iterator().next()), 1, tx);
        }
        if (be.base != Items.AIR || !be.held.isEmpty()) return "FAIL aborted insert was not rolled back";
        try (var tx = Transaction.openRoot()) {
            if (r.base() == Items.AIR) {
                // ageing recipe: no base
            } else if (r.base() instanceof net.minecraft.world.item.BucketItem bucket && bucketFluid(bucket) != Fluids.EMPTY) {
                if (be.fluidIntake.insert(FluidResource.of(bucketFluid(bucket)), FluidType.BUCKET_VOLUME - 1, tx) != 0) return "FAIL a partial bucket of fluid was accepted";
                if (be.fluidIntake.insert(FluidResource.of(bucketFluid(bucket)), FluidType.BUCKET_VOLUME, tx) != FluidType.BUCKET_VOLUME) return "FAIL fluid base not accepted";
            } else {
                be.automation.insert(ItemResource.of(r.base()), 1, tx);
            }
            for (var e : r.ingredients().entrySet()) {
                for (int i = 0; i < e.getValue(); i++) be.automation.insert(ItemResource.of(e.getKey()), 1, tx);
            }
            tx.commit();
        }
        if (!be.busy() && be.settle < 0) return "FAIL automation did not start fermenting: base=" + be.base + " held=" + be.held + " done=" + be.done();
        for (int i = 0; i < r.ticks() * 2 + BrewVesselEntity.SETTLE_TICKS + 5 && !be.done(); i++) BrewVesselEntity.tick(level, pos, be.getBlockState(), be);
        int got;
        try (var tx = Transaction.openRoot()) {
            got = be.automation.extract(ItemResource.of(r.drink()), 64, tx);
            tx.commit();
        }
        return got == r.servings() ? null : "FAIL automation drew " + got + " of " + r.servings();
    }

    private static net.minecraft.world.level.material.Fluid bucketFluid(net.minecraft.world.item.BucketItem b) {
        for (var f : BuiltInRegistries.FLUID) {
            if (f.isSource(f.defaultFluidState()) && f.getBucket() == b) return f;
        }
        return Fluids.EMPTY;
    }

    private static boolean use(ServerLevel level, Player p, BlockPos pos, BlockHitResult hit, ItemStack stack) {
        p.setItemInHand(InteractionHand.MAIN_HAND, stack);
        level.getBlockState(pos).useItemOn(stack, level, p, InteractionHand.MAIN_HAND, hit);
        return stack.isEmpty() || stack.getItem() == Items.AIR || stack.getCount() == 0;
    }
}
