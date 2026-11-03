package net.diaithir.upgrades;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Server-side check with no player: slots, kinds, persistence, removal, beacon sharing, planner swap.
 * Runs once at server start when the environment variable DIAITHIR_UPGRADES_SELFTEST is set.
 */
final class SelfTest {
    private SelfTest() {}

    private static ModuleSpec spec(String id) { return ModuleRegistry.get(id).orElseThrow(); }

    static List<String> run(MinecraftServer server) {
        List<String> f = new ArrayList<>();
        ServerLevel level = server.overworld();
        BlockPos m = new BlockPos(4800, 90, 4800), b = m.east(2), far = m.east(40);
        level.getChunk(m.getX() >> 4, m.getZ() >> 4);
        level.getChunk(far.getX() >> 4, far.getZ() >> 4);
        Upgrades.TARGETS.add(new TargetRegistry.Rule("minecraft:furnace", 0, 2,
                EnumSet.of(Kind.SPEED, Kind.PRODUCTIVITY, Kind.EFFICIENCY), 4));
        level.setBlock(m, Blocks.FURNACE.defaultBlockState(), 3);
        level.setBlock(b, Registry.BEACON.get().defaultBlockState(), 3);
        level.setBlock(far, Blocks.FURNACE.defaultBlockState(), 3);
        BlockEntity me = level.getBlockEntity(m), be = level.getBlockEntity(b), fe = level.getBlockEntity(far);
        if (me == null || be == null || fe == null) { f.add("block entities missing"); return f; }

        if (Upgrades.host(me).isEmpty()) f.add("furnace has no host despite rule");

        ModuleSpec spd1 = spec("circuit_speed_1"), spd2 = spec("circuit_speed_2"), prod1 = spec("ogham_productivity_1"),
                qual1 = spec("spice_quality_1");
        if (!Upgrades.insert(me, spd1, Quality.NORMAL)) f.add("insert speed_1 failed");
        if (Math.abs(Upgrades.host(me).get().effects().speedMultiplier() - 1.2) > 1e-9) f.add("speed multiplier after one module is not 1.2");
        if (Upgrades.host(me).get().slots().modules().size() != 1) f.add("module not persisted in attachment");
        if (Upgrades.insert(me, qual1, Quality.NORMAL)) f.add("quality module accepted where not allowed");
        if (!Upgrades.insert(me, prod1, Quality.NORMAL)) f.add("insert productivity failed");
        if (Upgrades.insert(me, spd1, Quality.NORMAL)) f.add("third module accepted into two slots");
        double outx = Upgrades.host(me).get().effects().outputMultiplier();
        if (Math.abs(outx - 1.04) > 1e-9) f.add("output multiplier " + outx + " is not 1.04");
        double w = Upgrades.host(me).get().powerWatts(1000);
        if (Math.abs(w - 1900) > 1e-6) f.add("power " + w + " W is not 1900 (speed +50%, productivity +40%)");

        // beacon: speed shared at half, nothing from an out-of-range machine
        if (!Upgrades.insert(be, spd1, Quality.NORMAL)) f.add("insert into beacon failed");
        if (Upgrades.insert(be, prod1, Quality.NORMAL)) f.add("beacon accepted productivity");
        double sp = Upgrades.host(me).get().effects().speedMultiplier();
        if (Math.abs(sp - 1.25) > 1e-9) f.add("speed with beacon " + sp + " is not 1.25 (own +15%, beacon +10%)");
        if (Upgrades.host(me).get().effects().outputMultiplier() != outx) f.add("beacon changed productivity");
        Upgrades.insert(fe, spd1, Quality.NORMAL);
        if (Math.abs(Upgrades.host(fe).get().effects().speedMultiplier() - 1.2) > 1e-9) f.add("out-of-range machine got beacon bonus");

        // planner swap: speed_1 -> speed_2 on both machines in range; far one is out of the 8-block radius
        int swaps = PlannerItem.upgradeArea(level, m, spd2, 99, old -> { });
        if (swaps != 2) f.add("planner swapped " + swaps + " modules, expected 2 (furnace and beacon in range)");
        if (!Upgrades.host(fe).get().slots().modules().get(0).spec().equals(spd1)) f.add("planner touched machine out of range");

        // removal
        int before = Upgrades.host(me).get().slots().modules().size();
        if (!Upgrades.remove(me, before - 1) || Upgrades.host(me).get().slots().modules().size() != before - 1) f.add("remove failed");

        // beacon gone: bonus gone
        level.setBlock(b, Blocks.AIR.defaultBlockState(), 3);
        if (!Upgrades.host(me).get().beacons().isEmpty()) f.add("removed beacon still covers machine");

        player(server, level, f);

        for (BlockPos p : List.of(m, far)) level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        return f;
    }

    /** Uses a fake player: module item on a machine, the module screen's slots, and the planner item. */
    private static void player(MinecraftServer server, ServerLevel level, List<String> f) {
        var pl = net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(level);
        BlockPos pos = new BlockPos(4900, 90, 4900);
        level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        level.setBlock(pos, Blocks.FURNACE.defaultBlockState(), 3);
        BlockEntity be = level.getBlockEntity(pos);
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos),
                net.minecraft.core.Direction.UP, pos, false);
        var hand = net.minecraft.world.InteractionHand.MAIN_HAND;
        pl.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);

        // module item used on the machine
        var item = Modules.ALL.stream().filter(d -> d.getId().getPath().equals("circuit_speed_1")).findFirst().orElseThrow().get();
        pl.setItemInHand(hand, new net.minecraft.world.item.ItemStack(item, 3));
        var r = pl.getMainHandItem().useOn(new net.minecraft.world.item.context.UseOnContext(pl, hand, hit));
        if (!r.consumesAction() || Upgrades.host(be).get().slots().modules().size() != 1) f.add("module item did not slot into the machine");
        if (pl.getMainHandItem().getCount() != 2) f.add("module item not consumed (" + pl.getMainHandItem().getCount() + ")");

        // menu: the saved module appears, slots accept and refuse, edits save
        var menu = new UpgradeMenu(7, pl.getInventory(), pos, 2, be);
        if (!(menu.slots.get(0).getItem().getItem() instanceof ModuleItem)) f.add("menu does not show the saved module");
        var prod = Modules.ALL.stream().filter(d -> d.getId().getPath().equals("ogham_productivity_1")).findFirst().orElseThrow().get();
        var qual = Modules.ALL.stream().filter(d -> d.getId().getPath().equals("spice_quality_1")).findFirst().orElseThrow().get();
        if (menu.slots.get(1).mayPlace(new net.minecraft.world.item.ItemStack(qual))) f.add("menu slot accepts a quality module the furnace refuses");
        if (!menu.slots.get(1).mayPlace(new net.minecraft.world.item.ItemStack(prod))) f.add("menu slot refuses a productivity module");
        menu.slots.get(1).set(new net.minecraft.world.item.ItemStack(prod));
        if (Upgrades.host(be).get().slots().modules().size() != 2) f.add("menu insert not saved");
        menu.slots.get(0).set(net.minecraft.world.item.ItemStack.EMPTY);
        var left = Upgrades.host(be).get().slots().modules();
        if (left.size() != 1 || !left.get(0).spec().id().equals("ogham_productivity_1")) f.add("menu removal not saved");
        if (!menu.stillValid(pl)) f.add("menu not valid next to its machine");

        // planner: off hand module, main hand planner, tier up
        var tier2 = Modules.ALL.stream().filter(d -> d.getId().getPath().equals("circuit_productivity_2")).findFirst().orElseThrow().get();
        var planner = server.registryAccess() == null ? null : net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("diaithirupgrades", "planner"));
        pl.setItemInHand(hand, new net.minecraft.world.item.ItemStack(planner));
        pl.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, new net.minecraft.world.item.ItemStack(tier2, 4));
        Upgrades.insert(be, ModuleRegistry.get("circuit_productivity_1").orElseThrow(), Quality.NORMAL);
        pl.getMainHandItem().useOn(new net.minecraft.world.item.context.UseOnContext(pl, hand, hit));
        boolean got2 = Upgrades.host(be).get().slots().modules().stream().anyMatch(i -> i.spec().id().equals("circuit_productivity_2"));
        if (!got2) f.add("planner did not upgrade the productivity module");
        if (pl.getOffhandItem().getCount() != 2) f.add("planner used " + (4 - pl.getOffhandItem().getCount()) + " modules, expected 2 (both lower-tier productivity modules)");
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }
}
