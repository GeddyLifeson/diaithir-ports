package net.diaithir.yards;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

/** One vehicle under construction. Heavy parts need a crane within reach; cranes and higher yard tiers speed the work. */
public final class YardBlockEntity extends BlockEntity {
    public static final int REACH = 8;

    private int autoCd;
    private String yardId = "";
    private BuildState build;

    public YardBlockEntity(BlockPos pos, BlockState state) {
        super(YardContent.YARD_BE.get(), pos, state);
    }

    YardDef.Tier tier() { return ((YardBlock) getBlockState().getBlock()).tier(); }

    public boolean active() { return build != null; }

    public String yardId() { return yardId; }

    public BuildState build() { return build; }

    /** Pick what to build. Null when accepted, else the reason. */
    public String select(String id) {
        if (build != null) return "A vehicle is already under construction here.";
        YardDef d = YardRegistry.yard(id).orElse(null);
        if (d == null) return "No such design.";
        if (d.tier().ordinal() > tier().ordinal()) return "This yard is too basic. It needs a " + d.tier().name().toLowerCase().replace('_', ' ') + ".";
        yardId = id;
        build = BuildState.start(d);
        setChanged();
        return null;
    }

    /** Largest lift and combined speed of the cranes in reach. */
    double[] cranes() {
        double lift = 0, speed = 1;
        if (level == null) return new double[] {0, 1};
        for (BlockPos p : BlockPos.betweenClosed(worldPosition.offset(-REACH, -2, -REACH), worldPosition.offset(REACH, REACH, REACH))) {
            double[] c = YardContent.CRANE_STATS.get(level.getBlockState(p).getBlock());
            if (c != null) {
                lift = Math.max(lift, c[0]);
                speed = Math.max(speed, c[1]);
            }
        }
        return new double[] {lift, speed};
    }

    /** Install one part (the stack is not touched). Returns the result name and a message. */
    public BuildState.InstallResult install(PartDef part) {
        if (build == null) return BuildState.InstallResult.DONE;
        BuildState.InstallResult r = build.install(part, Integer.MAX_VALUE, cranes()[0]);
        if (r == BuildState.InstallResult.OK) setChanged();
        return r;
    }

    public Component describe() {
        if (build == null) return Component.literal("Idle. Put a finished vehicle on the yard to pick the design to build.");
        YardDef d = YardRegistry.yard(yardId).orElseThrow();
        if (build.complete()) return Component.literal("Complete");
        YardDef.Stage st = d.stages().get(build.stageIndex());
        StringBuilder sb = new StringBuilder(yardId.substring(5)).append(" - stage ").append(st.id()).append(" (").append(build.stageIndex() + 1).append("/").append(d.stages().size()).append("), needs ");
        for (YardDef.Slot s : st.slots()) sb.append(s.count()).append(" ").append(s.kind()).append(" ");
        return Component.literal(sb.append(String.format("%.0f%%", build.stageProgress() * 100)).toString());
    }

    /** A crane is manned when a player or an entity of tag diaithircarts:crane_operators (villagers, workers) stands within reach of one. */
    boolean manned() {
        if (level == null) return false;
        var box = new net.minecraft.world.phys.AABB(worldPosition).inflate(REACH);
        for (net.minecraft.world.entity.LivingEntity e : level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, box)) {
            if (e instanceof net.minecraft.world.entity.player.Player p && !p.isSpectator()) return true;
            if (BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(e.getType()).is(OPERATORS)) return true;
        }
        return false;
    }

    public static final net.minecraft.tags.TagKey<net.minecraft.world.entity.EntityType<?>> OPERATORS =
            net.minecraft.tags.TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("diaithircarts:crane_operators"));

    /** Build from the blueprint (the chosen design): a manned crane takes the next needed part from containers touching the controller. */
    private void autoBuild() {
        if (build.complete() || ++autoCd % 10 != 0 || !manned()) return;
        YardDef d = YardRegistry.yard(yardId).orElse(null);
        if (d == null) return;
        for (YardDef.Slot slot : d.stages().get(build.stageIndex()).slots()) {
            long have = build.stageParts().stream().filter(p -> p.kind().equals(slot.kind())).count();
            if (have >= slot.count()) continue;
            for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
                if (!(level.getBlockEntity(worldPosition.relative(dir)) instanceof net.minecraft.world.Container box)) continue;
                for (int i = 0; i < box.getContainerSize(); i++) {
                    ItemStack s = box.getItem(i);
                    if (s.isEmpty()) continue;
                    PartDef part = YardRegistry.part(BuiltInRegistries.ITEM.getKey(s.getItem()).toString()).orElse(null);
                    if (part == null || !part.kind().equals(slot.kind())) continue;
                    if (install(part) == BuildState.InstallResult.OK) {
                        s.shrink(1);
                        box.setChanged();
                        return;
                    }
                }
            }
        }
    }

    public void tick() {
        if (build == null || level == null || level.isClientSide()) return;
        autoBuild();
        if (build == null) return;
        double[] c = cranes();
        YardDef d = YardRegistry.yard(yardId).orElse(null);
        if (d == null) return;
        double boost = c[1] * tier().speed / d.tier().speed;
        if (build.advance(1, boost)) setChanged();
        if (build.complete()) {
            Identifier id = Identifier.parse(d.result());
            ItemStack out = new ItemStack(BuiltInRegistries.ITEM.getValue(id));
            ItemEntity e = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.2, worldPosition.getZ() + 0.5, out);
            level.addFreshEntity(e);
            lastRollout = out;
            build = null;
            yardId = "";
            setChanged();
        }
    }

    /** The last vehicle rolled out, for tests. */
    public ItemStack lastRollout = ItemStack.EMPTY;

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putString("yard", yardId);
        if (build == null) return;
        out.putInt("stage", build.stageIndex());
        out.putInt("ticks", build.progressTicks());
        out.store("done", Codec.STRING.listOf(), names(build.installed()));
        out.store("now", Codec.STRING.listOf(), names(build.stageParts()));
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        yardId = in.getStringOr("yard", "");
        build = null;
        YardDef d = yardId.isEmpty() ? null : YardRegistry.yard(yardId).orElse(null);
        if (d == null) return;
        build = BuildState.restore(d, in.getIntOr("stage", 0), in.getIntOr("ticks", 0),
                parts(in.read("done", Codec.STRING.listOf()).orElse(List.of())), parts(in.read("now", Codec.STRING.listOf()).orElse(List.of())));
    }

    private static List<String> names(List<PartDef> l) {
        List<String> o = new ArrayList<>();
        for (PartDef p : l) o.add(p.item());
        return o;
    }

    private static List<PartDef> parts(List<String> ids) {
        List<PartDef> o = new ArrayList<>();
        for (String s : ids) YardRegistry.part(s).ifPresent(o::add);
        return o;
    }
}
