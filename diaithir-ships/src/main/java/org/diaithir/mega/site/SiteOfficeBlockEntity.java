package org.diaithir.mega.site;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.diaithir.mega.MegaMod;

/**
 * Site office: runs a StageEngine for one structure, draws material from depots touching it, places and carves blocks as stages progress,
 * and exposes the function hooks (watts, function share). The structure grows 16 m east of the office (its widest layer is 24 blocks, so it never covers the office or its depots).
 */
public class SiteOfficeBlockEntity extends BlockEntity {
    /** 1 = real time. Tests raise it so a 46 h stage fits a headless run; it only scales the clock, never the rates or costs. */
    public static final double TIME_SCALE = Double.parseDouble(System.getProperty("megamachines.timescale", "1"));
    private static final int BLOCKS_PER_TICK = 128;
    private static final BlockPos ORIGIN = new BlockPos(16, 0, 0);

    private String tier = "III";
    private String structureId = "";
    private int stage, builders = 1, placed;
    private double done;
    private boolean paid = true;
    private StageEngine engine;
    private StageEngine.Block last = StageEngine.Block.NO_BUILDERS;

    public SiteOfficeBlockEntity(BlockPos pos, BlockState state) { super(MegaMod.OFFICE_BE.get(), pos, state); }

    public String tier() { return tier; }
    public String structureId() { return structureId; }
    public StageEngine engine() { return engine; }
    public String statusName() { return engine == null ? "idle" : last.name().toLowerCase(); }
    public int placed() { return placed; }
    public boolean paid() { return paid; }

    /** Wages hook: the employer's treasury pays the crew. False pauses work (the crew strikes). */
    public void setPaid(boolean p) { paid = p; setChanged(); }
    public void setBuilders(int n) { builders = Math.max(0, n); if (engine != null) engine.setBuilders(builders); setChanged(); }
    /** Office upgrade (UPGRADES.md); an office never goes back an Age. */
    public boolean setTier(String age) {
        if (Blueprints.ageIndex(age) < Blueprints.ageIndex(tier)) return false;
        tier = age;
        setChanged();
        return true;
    }

    /** Starts a structure. Refused when the office is older than the structure's station Age, or a site is already running. */
    public boolean start(String id) {
        Blueprints.Structure s = Blueprints.get(id);
        if (s == null || !structureId.isEmpty() || Blueprints.ageIndex(tier) < Blueprints.ageIndex(s.stationAge())) return false;
        structureId = id;
        stage = 0; done = 0; placed = 0;
        engine = new StageEngine(s, 0, 0, builders);
        setChanged();
        return true;
    }

    /** Function hooks for the power, water, habitat and transit systems. */
    public double watts() { return engine == null ? 0 : engine.watts(); }
    public double functionShare() { return engine == null ? 0 : engine.functionShare(); }

    public static void tick(Level level, BlockPos pos, BlockState state, SiteOfficeBlockEntity be) {
        if (!(level instanceof ServerLevel sl) || be.engine == null) return;
        Ledger pool = new Ledger();
        java.util.List<DepotBlockEntity> depots = new java.util.ArrayList<>();
        for (Direction d : Direction.values())
            if (level.getBlockEntity(pos.relative(d)) instanceof DepotBlockEntity dep) {
                depots.add(dep);
                dep.ledger.view().forEach(pool::add);
            }
        java.util.Map<String, Double> before = new java.util.HashMap<>(pool.view());
        be.last = be.engine.tick(0.05 * TIME_SCALE, be.paid, pool);
        for (var e : before.entrySet()) {
            double used = e.getValue() - pool.get(e.getKey());
            for (DepotBlockEntity dep : depots) {
                double t = Math.min(used, dep.ledger.get(e.getKey()));
                if (t > 0) { dep.ledger.take(e.getKey(), t); dep.setChanged(); used -= t; }
            }
        }
        be.stage = be.engine.stageIndex(); be.done = be.engine.done();
        be.build(sl);
        be.setChanged();
    }

    private void build(ServerLevel level) {
        int target = engine.blocksDone();
        BlockState fill = Blueprints.ageIndex(structureAge()) >= 5 ? Blocks.GRAY_CONCRETE.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState();
        for (int n = 0; placed < target && n < BLOCKS_PER_TICK; n++) {
            Layout.Off o = Layout.at(engine.structure, placed);
            BlockPos p = worldPosition.offset(ORIGIN).offset(o.x(), o.y(), o.z());
            if (!level.isLoaded(p)) return;
            level.setBlock(p, o.carve() ? Blocks.AIR.defaultBlockState() : fill, 3);
            placed++;
        }
    }

    private String structureAge() { return engine.structure.stationAge(); }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        tier = in.getStringOr("tier", "III");
        structureId = in.getStringOr("structure", "");
        stage = in.getIntOr("stage", 0);
        done = in.getDoubleOr("done", 0);
        builders = in.getIntOr("builders", 1);
        placed = in.getIntOr("placed", 0);
        paid = in.getBooleanOr("paid", true);
        Blueprints.Structure s = Blueprints.get(structureId);
        engine = s == null ? null : new StageEngine(s, stage, done, builders);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putString("tier", tier);
        out.putString("structure", structureId);
        out.putInt("stage", stage);
        out.putDouble("done", done);
        out.putInt("builders", builders);
        out.putInt("placed", placed);
        out.putBoolean("paid", paid);
    }
}
