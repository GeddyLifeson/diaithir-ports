package net.diaithir.steamrails;

import com.mojang.logging.LogUtils;
import com.zurrtum.create.AllBogeyStyles;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import com.zurrtum.create.content.trains.bogey.AllBogeySizes;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Builds a boiler yard high above spawn and runs the steam rules in real ticks: layout and rating, feeding from a tender, burning fuel,
 * pressure rising, steam leaving through a whistle (by redstone) and the cylinders, the safety valve, saving and loading the numbers the way a
 * moving carriage does, the water capability, Create's bogey registry, and Create's {@code Train} (through our mixin) taking its speed from
 * pressure. A real train on real track is not part of this test.
 */
final class SelfTest {
    private SelfTest() {}

    private static final org.slf4j.Logger LOG = LogUtils.getLogger();

    private static ServerLevel level;
    private static CommandSourceStack source;
    private static BlockPos at;
    private static int tick = -1;
    private static int warm = 0;
    private static final List<String> fails = new ArrayList<>();
    private static final List<ChunkPos> forced = new ArrayList<>();
    private static float whistleDrawn;
    private static float maxPressure;

    // layout: firebox at at, steel shells at +1 and +2 (x), stack above the firebox, two cylinders in front (z+1), whistle above shell 2, tender behind (z-1)
    private static BlockPos fire() {
        return at;
    }

    private static BoilerBlockEntity boiler() {
        return level.getBlockEntity(fire()) instanceof BoilerBlockEntity b ? b : null;
    }

    private static TenderBlockEntity tender() {
        return level.getBlockEntity(at.north()) instanceof TenderBlockEntity t ? t : null;
    }

    static String start(CommandSourceStack src) {
        if (tick >= 0) return "already running";
        level = src.getServer().getLevel(Level.OVERWORLD);
        if (level == null) return "no overworld";
        source = src;
        fails.clear();
        at = new BlockPos(level.getRespawnData().pos().getX() + 4, 240, level.getRespawnData().pos().getZ() + 4);
        for (int cx = (at.getX() - 6) >> 4; cx <= (at.getX() + 10) >> 4; cx++) {
            for (int cz = (at.getZ() - 6) >> 4; cz <= (at.getZ() + 10) >> 4; cz++) {
                level.getChunk(cx, cz);
                level.setChunkForced(cx, cz, true);
                forced.add(new ChunkPos(cx, cz));
            }
        }
        clear();
        tick = 0;
        warm = 0;
        return null;
    }

    private static void clear() {
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-3, -1, -3), at.offset(8, 4, 5))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
    }

    private static void check(boolean ok, String what) {
        if (!ok) fails.add(what);
    }

    static void tick(MinecraftServer server) {
        if (tick < 0) return;
        if (warm < 10) {
            warm++;
            return;
        }
        try {
            step();
        } catch (RuntimeException | LinkageError e) {
            LOG.error("steamrails self test crashed", e);
            fails.add("exception " + e);
            tick = 10000;
        }
        if (tick >= 0) tick++;
        if (tick > 520) finish();
    }

    private static void step() {
        if (tick == 0) {
            level.setBlock(at.offset(-1, -1, -2), Blocks.STONE_BRICKS.defaultBlockState(), 2 | 16);
            level.setBlock(fire(), SteamBlocks.BOILER.get().defaultBlockState(), 3);
            level.setBlock(at.east(), SteamBlocks.STEEL_SHELL.get().defaultBlockState(), 3);
            level.setBlock(at.east(2), SteamBlocks.STEEL_SHELL.get().defaultBlockState(), 3);
            level.setBlock(at.above(), SteamBlocks.SMOKESTACK.get().defaultBlockState(), 3);
            level.setBlock(at.south(), SteamBlocks.CYLINDER.get().defaultBlockState(), 3);
            level.setBlock(at.east().south(), SteamBlocks.CYLINDER.get().defaultBlockState(), 3);
            level.setBlock(at.east(2).above(), SteamBlocks.WHISTLE.get().defaultBlockState(), 3);
            level.setBlock(at.north(), SteamBlocks.TENDER.get().defaultBlockState(), 3);
            Layout l = Layout.scan(level::getBlockState, at.east());
            check(l != null, "layout not found from a shell");
            if (l != null) {
                check(l.master.equals(at), "master is not the firebox: " + l.master);
                check(l.size == 4, "size should be 4 (firebox 2 + two shells), got " + l.size);
                check(l.rating == SteamRails.CORE_RATING, "rating should be the firebox's " + SteamRails.CORE_RATING + ", got " + l.rating);
                check(l.cylinders == 2, "cylinders " + l.cylinders);
                check(l.whistles == 1, "whistles " + l.whistles);
                check(l.stacks.size() == 1, "stacks " + l.stacks.size());
                check(l.tenderPos.size() == 1, "tenders " + l.tenderPos.size());
                check(l.waterCapacity() == 4 * SteamRails.WATER_PER_SIZE, "water capacity " + l.waterCapacity());
            }
            // a copper shell drags the whole rating down
            level.setBlock(at.east(2), SteamBlocks.COPPER_SHELL.get().defaultBlockState(), 3);
            Layout w = Layout.scan(level::getBlockState, at);
            check(w != null && w.rating == SteamRails.COPPER_RATING, "copper shell should set rating " + SteamRails.COPPER_RATING + ", got " + (w == null ? "null" : w.rating));
            level.setBlock(at.east(2), SteamBlocks.STEEL_SHELL.get().defaultBlockState(), 3);
            BoilerBlockEntity b = boiler();
            check(b != null, "no boiler block entity");
            TenderBlockEntity t = tender();
            check(t != null, "no tender block entity");
            if (b == null || t == null) {
                tick = 10000;
                return;
            }
            t.state.water = 12000;
            t.state.addFuel(new ItemStack(Items.COAL, 16), Hand.burn(level));
            check(t.state.fuelCount() == 16, "tender should hold 16 coal");
            check(b.state.pressure == 0F, "cold boiler has pressure");
        }
        BoilerBlockEntity b = boiler();
        TenderBlockEntity t = tender();
        if (b == null || t == null) return;
        if (tick == 60 || tick == 200 || tick == 300) {
            LOG.info("steamrails test t={} pressure={} water={} fuel={} burnLeft={} tenderWater={} tenderFuel={}", tick, b.state.pressure, b.state.water,
                    b.state.fuelCount(), b.state.burnLeft, t.state.water, t.state.fuelCount());
        }
        if (tick == 60) {
            check(b.state.water > 500, "tender did not fill the boiler with water: " + b.state.water);
            check(b.state.fuelCount() > 0 || b.state.burning(), "tender did not give the boiler fuel");
            check(b.state.burning(), "boiler is not burning after 60 ticks");
            check(level.getBlockState(at).getValue(BoilerBlock.LIT), "firebox block state is not lit");
            check(t.state.water < 12000, "tender water did not go down");
        }
        if (tick == 200) {
            check(b.state.pressure > 1.5F, "pressure after 200 ticks only " + b.state.pressure);
        }
        if (tick == 300) {
            check(b.state.pressure > 3F, "pressure after 300 ticks only " + b.state.pressure);
            check(b.state.produced > 0F, "no steam produced");
            Layout l = b.layout(level);
            int cmp = level.getBlockState(at).getAnalogOutputSignal(level, at, Direction.UP);
            check(cmp > 0, "comparator reads 0 at pressure " + b.state.pressure);
            check(l != null && TrainPower.thrust(b.state.pressure, l.cylinders, l.size) > 0.5, "no thrust at pressure " + b.state.pressure);
            // redstone blows the whistle, which takes steam
            level.setBlock(at.east(3).above(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
        }
        if (tick > 300 && tick < 303) whistleDrawn = Math.max(whistleDrawn, b.state.drawn);
        if (tick == 303) {
            BlockState ws = level.getBlockState(at.east(2).above());
            check(ws.getValue(WhistleBlock.POWERED), "whistle not powered by redstone");
            check(whistleDrawn >= WhistleBlock.COST - 0.01F, "steam did not leave through the whistle (drawn " + whistleDrawn + ")");
            // cylinders drain steam in proportion to load
            float before = b.state.pressure;
            check(b.draw(5F, 1.5F), "draw refused at pressure " + before);
        }
        if (tick == 304) check(b.state.drawn >= 4.9F, "a 5 unit draw delivered " + b.state.drawn);
        if (tick == 305) {
            // and a draw is refused when the boiler is cold
            BoilerState cold = new BoilerState();
            Layout l = b.layout(level);
            float p0 = cold.pressure;
            cold.tick(l, Hand.burn(level), 3F);
            check(cold.drawn == 0F && cold.pressure == p0, "cold boiler gave steam");
            // water capability
            ResourceHandler<FluidResource> cap = level.getCapability(Capabilities.Fluid.BLOCK, at.north(), Direction.UP);
            check(cap != null, "no fluid capability on the tender");
            if (cap != null) {
                float w0 = t.state.water;
                try (Transaction tx = Transaction.openRoot()) {
                    int in = cap.insert(FluidResource.of(Fluids.WATER), 1000, tx);
                    check(in == 1000, "tender took " + in + " of 1000 water");
                    tx.commit();
                }
                try (Transaction tx = Transaction.openRoot()) {
                    check(cap.insert(FluidResource.of(Fluids.LAVA), 1000, tx) == 0, "tender accepted lava");
                }
                t.drainIntake();
                check(t.state.water >= w0 + 900, "intake did not reach the tender: " + w0 + " -> " + t.state.water);
            }
            // player hands: a bucket of water goes into the boiler
            // safety valve
            b.state.pressure = SteamRails.CORE_RATING - 0.02F;
            b.state.water = Math.max(b.state.water, 1000F);
        }
        if (tick > 305 && tick < 340) maxPressure = Math.max(maxPressure, b.state.pressure);
        if (tick == 340) {
            check(maxPressure <= SteamRails.CORE_RATING + 0.001F, "pressure rose above the rating: " + maxPressure);
            check(maxPressure > SteamRails.CORE_RATING - 1.0F, "pressure never reached the valve: " + maxPressure);
            registries(b);
            persistence(b);
            movingBoiler();
            trainHooks();
            tick = 520;
        }
    }

    private static void registries(BoilerBlockEntity b) {
        Identifier driverId = Identifier.fromNamespaceAndPath(SteamRails.ID, "driver");
        Identifier ponyId = Identifier.fromNamespaceAndPath(SteamRails.ID, "pony");
        BogeyStyle driver = AllBogeyStyles.BOGEY_STYLES.get(driverId);
        BogeyStyle pony = AllBogeyStyles.BOGEY_STYLES.get(ponyId);
        check(driver != null, "driver style missing in Create's BOGEY_STYLES");
        check(pony != null, "pony style missing in Create's BOGEY_STYLES");
        check(AllBogeyStyles.BOGEY_STYLES.containsKey(Identifier.fromNamespaceAndPath("create", "standard")), "standard style gone");
        Map<Identifier, BogeyStyle> group = AllBogeyStyles.getCycleGroup(AllBogeyStyles.STANDARD_CYCLE_GROUP);
        check(group.containsKey(driverId) && group.containsKey(ponyId), "steam styles not in the standard cycle group (wrench cannot reach them)");
        if (driver != null) {
            check(driver.getBlockForSize(AllBogeySizes.LARGE) == SteamBlocks.DRIVER_BOGEY.get(), "driver style has the wrong block");
            check(SteamBlocks.DRIVER_BOGEY.get().getDefaultStyle() == driver, "driver block default style");
        }
        if (pony != null) check(pony.getBlockForSize(AllBogeySizes.SMALL) == SteamBlocks.PONY_BOGEY.get(), "pony style has the wrong block");
        // the bogey blocks make working block entities that report their style
        for (var db : List.of(SteamBlocks.DRIVER_BOGEY, SteamBlocks.PONY_BOGEY)) {
            BlockPos p = at.offset(5, 0, 0);
            level.setBlock(p, db.get().defaultBlockState().setValue(AbstractBogeyBlock.AXIS, Direction.Axis.X), 3);
            if (level.getBlockEntity(p) instanceof SteamBogeyBlockEntity be) {
                check(be.getStyle().id.getNamespace().equals(SteamRails.ID), db.getId() + ": block entity style is " + be.getStyle().id);
            } else {
                fails.add(db.getId() + ": no steam bogey block entity");
            }
            check(db.get().getWheelRadius() > 0 && db.get().getWheelPointSpacing() > 0, db.getId() + ": wheel numbers");
            level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        }
        check(MovementBehaviour.REGISTRY.get(SteamBlocks.BOILER.get().defaultBlockState()) instanceof BoilerMovement, "boiler has no movement behaviour in Create");
        check(com.zurrtum.create.api.behaviour.interaction.MovingInteractionBehaviour.REGISTRY.get(SteamBlocks.TENDER.get().defaultBlockState()) != null,
                "tender has no moving interaction in Create");
    }

    /** The numbers survive a round trip through NBT, which is how a moving carriage keeps them, and a layout scan works on a map of blocks, as on a contraption. */
    private static void persistence(BoilerBlockEntity b) {
        var ops = BoilerMovement.ops(level);
        BoilerState s = new BoilerState();
        s.water = 1234F;
        s.pressure = 4.5F;
        s.burnLeft = 77;
        s.burnTotal = 100;
        s.addFuel(new ItemStack(Items.COAL, 5), Hand.burn(level));
        CompoundTag tag = new CompoundTag();
        tag.store(BoilerMovement.KEY, BoilerState.CODEC, ops, s);
        BoilerState r = tag.read(BoilerMovement.KEY, BoilerState.CODEC, ops).orElse(null);
        check(r != null, "boiler state did not read back");
        if (r != null) {
            check(r.water == 1234F && r.pressure == 4.5F && r.burnLeft == 77 && r.burnTotal == 100, "boiler numbers changed in NBT");
            check(r.fuelCount() == 5 && r.fuel[0].is(Items.COAL), "boiler fuel changed in NBT");
        }
        TenderState ts = new TenderState();
        ts.water = 999;
        ts.addFuel(new ItemStack(Items.CHARCOAL, 70), Hand.burn(level));
        CompoundTag t2 = new CompoundTag();
        t2.store(TenderBlockEntity.KEY, TenderState.CODEC, ops, ts);
        TenderState tr = t2.read(TenderBlockEntity.KEY, TenderState.CODEC, ops).orElse(null);
        check(tr != null && tr.water == 999 && tr.fuelCount() == 70, "tender state changed in NBT");
        // a tender feeds a boiler state straight
        if (tr != null && r != null) {
            Layout l = b.layout(level);
            r.water = 100F;
            boolean moved = tr.feed(r, l, Hand.burn(level));
            check(moved && r.water > 100F, "tender feed did not move water");
        }
        Map<BlockPos, BlockState> fake = new HashMap<>();
        fake.put(new BlockPos(0, 0, 0), SteamBlocks.BOILER.get().defaultBlockState());
        fake.put(new BlockPos(1, 0, 0), SteamBlocks.COPPER_SHELL.get().defaultBlockState());
        fake.put(new BlockPos(0, 0, 1), SteamBlocks.CYLINDER.get().defaultBlockState());
        fake.put(new BlockPos(0, 1, 0), SteamBlocks.SMOKESTACK.get().defaultBlockState());
        Layout fl = Layout.scan(p -> fake.getOrDefault(p, Blocks.AIR.defaultBlockState()), new BlockPos(1, 0, 0));
        check(fl != null && fl.size == 3 && fl.rating == SteamRails.COPPER_RATING && fl.cylinders == 1 && fl.stacks.size() == 1, "fake contraption layout wrong");
        // a bigger boiler heats slower but holds pressure better under the same load
        BoilerState small = new BoilerState(), big = new BoilerState();
        small.water = big.water = 8000;
        small.burnLeft = big.burnLeft = 100000;
        Layout ls = Layout.scan(p -> fake.getOrDefault(p, Blocks.AIR.defaultBlockState()), new BlockPos(1, 0, 0));
        Map<BlockPos, BlockState> bigMap = new HashMap<>(fake);
        for (int i = 2; i < 8; i++) bigMap.put(new BlockPos(i, 0, 0), SteamBlocks.STEEL_SHELL.get().defaultBlockState());
        Layout lb = Layout.scan(p -> bigMap.getOrDefault(p, Blocks.AIR.defaultBlockState()), new BlockPos(1, 0, 0));
        check(lb != null && ls != null && lb.size > ls.size, "big layout not bigger");
        if (lb != null && ls != null) {
            for (int i = 0; i < 100; i++) {
                small.tick(ls, Hand.burn(level), 0F);
                big.tick(lb, Hand.burn(level), 0F);
            }
            check(small.pressure > big.pressure, "a small boiler should heat faster: " + small.pressure + " vs " + big.pressure);
            check(big.pressure > 0, "big boiler made no pressure");
            small.pressure = big.pressure = 5F;
            small.tick(ls, Hand.burn(level), 0.5F);
            big.tick(lb, Hand.burn(level), 0.5F);
            check(big.pressure > small.pressure, "a big boiler should hold pressure better under load");
        }
    }

    /**
     * The firebox as Create runs it on a carriage: a real {@code CarriageContraption} holding the blocks, a {@code MovementContext} on the firebox, and
     * our movement behaviour ticked by hand. The boiler's numbers must climb, then be back in the contraption's block entity data. (No train or
     * entity here, so thrust is not reported; that part is covered through the Train mixin below.)
     */
    private static void movingBoiler() {
        var ops = BoilerMovement.ops(level);
        CarriageContraption c = new CarriageContraption(Direction.EAST);
        BoilerState init = new BoilerState();
        init.water = 3000;
        init.addFuel(new ItemStack(Items.COAL, 8), Hand.burn(level));
        CompoundTag nbt = new CompoundTag();
        nbt.store(BoilerMovement.KEY, BoilerState.CODEC, ops, init);
        BlockPos f = new BlockPos(0, 0, 0);
        Map<BlockPos, StructureBlockInfo> blocks = c.getBlocks();
        blocks.put(f, new StructureBlockInfo(f, SteamBlocks.BOILER.get().defaultBlockState(), nbt));
        blocks.put(new BlockPos(1, 0, 0), new StructureBlockInfo(new BlockPos(1, 0, 0), SteamBlocks.STEEL_SHELL.get().defaultBlockState(), null));
        blocks.put(new BlockPos(0, 1, 0), new StructureBlockInfo(new BlockPos(0, 1, 0), SteamBlocks.SMOKESTACK.get().defaultBlockState(), null));
        blocks.put(new BlockPos(0, 0, 1), new StructureBlockInfo(new BlockPos(0, 0, 1), SteamBlocks.CYLINDER.get().defaultBlockState(), null));
        blocks.put(new BlockPos(1, 1, 0), new StructureBlockInfo(new BlockPos(1, 1, 0), SteamBlocks.WHISTLE.get().defaultBlockState(), null));
        MovementContext ctx = new MovementContext(level, blocks.get(f), c);
        ctx.position = Vec3.atCenterOf(at);
        BoilerMovement bm = new BoilerMovement();
        for (int i = 0; i < 400; i++) bm.tick(ctx);
        bm.stopMoving(ctx);
        BoilerState out = nbt.read(BoilerMovement.KEY, BoilerState.CODEC, ops).orElse(null);
        check(out != null, "moving boiler wrote nothing back");
        if (out != null) {
            LOG.info("steamrails moving boiler pressure={} water={} fuel={}", out.pressure, out.water, out.fuelCount());
            check(out.pressure > 3F, "a boiler on a carriage did not build pressure: " + out.pressure);
            check(out.water < 3000F && out.water > 2000F, "moving boiler water " + out.water);
            check(out.fuelCount() == 7 || out.fuelCount() == 8 || out.burnLeft > 0, "moving boiler fuel " + out.fuelCount());
        }
        check(bm.mustTickWhileDisabled(), "boiler must tick while the carriage is stopped");
    }

    /** Create's Train, through our mixin: a train with pressure gets speed from it, a train that has reported zero pressure cannot move, others are untouched. */
    private static void trainHooks() {
        Train plain = new Train(UUID.randomUUID(), UUID.randomUUID(), null, new ArrayList<>(), new ArrayList<>(), false, 0);
        float base = plain.maxSpeed();
        check(base > 0F, "plain train has no top speed");
        Train steam = new Train(UUID.randomUUID(), UUID.randomUUID(), null, new ArrayList<>(), new ArrayList<>(), false, 0);
        check(steam.maxSpeed() == base, "an unreported train changed speed");
        TrainPower.report(steam.id, 0.0);
        TrainPower.tick();
        check(TrainPower.isSteamTrain(steam.id), "reporting did not make a steam train");
        check(steam.maxSpeed() == 0F, "a steam train without thrust should not move, got " + steam.maxSpeed());
        TrainPower.report(steam.id, 4.0);
        TrainPower.tick();
        float full = steam.maxSpeed();
        check(full > 0F, "a steam train with thrust cannot move: " + full);
        TrainPower.report(steam.id, 0.2);
        TrainPower.tick();
        float slow = steam.maxSpeed();
        check(slow > 0F && slow < full, "less thrust should be slower: " + slow + " vs " + full);
        check(plain.maxSpeed() == base, "a plain train changed speed");
        check(steam.acceleration() > 0F && steam.maxTurnSpeed() >= 0F, "steam train accel / turn");
    }

    private static void finish() {
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-3, -1, -3), at.offset(8, 4, 5))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
        for (ChunkPos c : forced) level.setChunkForced(c.x(), c.z(), false);
        forced.clear();
        String line = fails.isEmpty() ? "Steamrails self test PASS" : "Steamrails self test FAIL: " + String.join("; ", fails);
        LOG.info(line);
        source.sendSuccess(() -> Component.literal(line), false);
        tick = -1;
        maxPressure = 0;
        whistleDrawn = 0;
    }
}
