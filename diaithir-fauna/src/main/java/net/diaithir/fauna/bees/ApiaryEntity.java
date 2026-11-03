package net.diaithir.fauna.bees;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Up to three bees. Each makes one comb of its species per CYCLE ticks while there are at least MIN_FLOWERS flowers within RADIUS blocks and it is not raining or dark. */
public final class ApiaryEntity extends BlockEntity {
    public static final int MAX_BEES = 3, CYCLE = 3600, RADIUS = 5, MIN_FLOWERS = 3, CAP = 256;
    private final List<String> bees = new ArrayList<>();
    private final List<Integer> timers = new ArrayList<>();
    private final Map<String, Integer> combs = new LinkedHashMap<>();
    private int flowers = -1;

    public ApiaryEntity(BlockPos pos, BlockState state) { super(Bees.APIARY_ENTITY.get(), pos, state); }

    public boolean addBee(String species) {
        if (bees.size() >= MAX_BEES) return false;
        bees.add(species);
        timers.add(0);
        setChanged();
        return true;
    }

    public List<String> takeBees() {
        List<String> out = new ArrayList<>(bees);
        bees.clear();
        timers.clear();
        setChanged();
        return out;
    }

    public int takeCombs(Player player) {
        int n = 0;
        for (var e : combs.entrySet()) {
            int left = e.getValue();
            while (left > 0) {
                ItemStack st = new ItemStack(Bees.combOf(e.getKey()), Math.min(left, 64));
                left -= st.getCount();
                n += st.getCount();
                if (!player.getInventory().add(st)) player.drop(st, false);
            }
        }
        combs.clear();
        setChanged();
        return n;
    }

    public int combCount(String species) { return combs.getOrDefault(species, 0); }

    public String status() {
        return bees.isEmpty() ? "Empty apiary. Put a jar of bees in." : bees.size() + " bees (" + String.join(", ", bees) + "), "
                + combs.values().stream().mapToInt(Integer::intValue).sum() + " combs, " + (flowers < 0 ? "?" : flowers) + " flowers near.";
    }

    static int countFlowers(Level level, BlockPos pos) {
        int n = 0;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-RADIUS, -2, -RADIUS), pos.offset(RADIUS, 2, RADIUS))) {
            if (level.getBlockState(p).is(BlockTags.FLOWERS)) n++;
        }
        return n;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ApiaryEntity be) {
        if (level instanceof ServerLevel sl) be.serverTick(sl, 20);
    }

    /** Advances by the given ticks when called every 20 game ticks. */
    void serverTick(ServerLevel level, int step) {
        if (bees.isEmpty()) return;
        if (flowers < 0 || level.getGameTime() % 200 < step) flowers = countFlowers(level, worldPosition);
        if (flowers < MIN_FLOWERS || level.isRaining() || level.getBrightness(net.minecraft.world.level.LightLayer.SKY, worldPosition.above()) < 8) return;
        for (int i = 0; i < bees.size(); i++) {
            int t = timers.get(i) + step;
            if (t >= CYCLE) {
                t = 0;
                combs.merge(bees.get(i), 1, (a, b) -> Math.min(CAP, a + b));
                setChanged();
            }
            timers.set(i, t);
        }
    }

    /** Test hook: forget the cached flower count. */
    void countFlowersForTest(Level level) { flowers = countFlowers(level, worldPosition); }

    /** Test hook: fills every timer so the next serverTick makes a comb per bee. */
    void ripen() { timers.replaceAll(t -> CYCLE - 20); }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putString("bees", String.join(",", bees));
        out.putIntArray("timers", timers.stream().mapToInt(Integer::intValue).toArray());
        combs.forEach((k, v) -> out.putInt("comb_" + k, v));
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        bees.clear();
        timers.clear();
        combs.clear();
        String joined = in.getStringOr("bees", "");
        if (!joined.isEmpty()) for (String s : joined.split(",")) if (BeeSpecies.byId(s) != null) bees.add(s);
        int[] t = in.getIntArray("timers").orElse(new int[0]);
        for (int i = 0; i < bees.size(); i++) timers.add(i < t.length ? t[i] : 0);
        for (BeeSpecies s : BeeSpecies.ALL) {
            int n = in.getIntOr("comb_" + s.id(), 0);
            if (n > 0) combs.put(s.id(), n);
        }
    }
}
