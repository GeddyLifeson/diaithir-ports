package net.diaithir.carts;

import com.mojang.serialization.MapCodec;
import net.diaithir.yards.BuildState;
import net.diaithir.yards.PartDef;
import net.diaithir.yards.YardDef;
import net.diaithir.yards.YardRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

/** Controller of a rail assembly yard. Use a part on it to install it, bare-handed to choose the yard or read progress; when the build is done, bare-handed again rolls the train onto the nearest rail. */
public class RailYardBlock extends BaseEntityBlock {
    public static final MapCodec<RailYardBlock> CODEC = simpleCodec(RailYardBlock::new);

    public RailYardBlock(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new Entity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != DiaithirCarts.RAIL_YARD_ENTITY.get()) return null;
        return (l, p, s, be) -> ((Entity) be).tick();
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        PartDef part = YardRegistry.part(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()).orElse(null);
        if (part == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof Entity e) {
            BuildState.InstallResult r = e.install(part);
            if (r == BuildState.InstallResult.OK && !player.isCreative()) stack.shrink(1);
            player.sendOverlayMessage(Component.literal(r == BuildState.InstallResult.OK ? "Installed." : "Not now: " + r.name().toLowerCase().replace('_', ' ')
                    + (e.build == null ? " (use the yard bare-handed to choose one)" : "")));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof Entity e) player.sendOverlayMessage(Component.literal(e.onUse(player.isShiftKeyDown())));
        return InteractionResult.SUCCESS;
    }

    public static class Entity extends BlockEntity {
        String yardId = "";
        BuildState build;
        /** Installed parts with the stage each went into (BuildState keeps them unordered). */
        private final List<int[]> stageOf = new ArrayList<>();
        private final List<PartDef> parts = new ArrayList<>();

        public Entity(BlockPos pos, BlockState state) {
            super(DiaithirCarts.RAIL_YARD_ENTITY.get(), pos, state);
        }

        YardDef def() {
            return YardRegistry.yard(yardId).orElse(null);
        }

        public boolean choose(String id) {
            YardDef d = YardRegistry.yard(id).orElse(null);
            if (d == null) return false;
            yardId = id;
            build = BuildState.start(d);
            parts.clear();
            stageOf.clear();
            setChanged();
            return true;
        }

        private int inStage(int stage, String kind) {
            int n = 0;
            for (int i = 0; i < parts.size(); i++) if (stageOf.get(i)[0] == stage && parts.get(i).kind().equals(kind)) n++;
            return n;
        }

        boolean slotsFull() {
            if (build == null || build.complete()) return false;
            YardDef.Stage st = def().stages().get(build.stageIndex());
            for (YardDef.Slot s : st.slots()) if (inStage(build.stageIndex(), s.kind()) < s.count()) return false;
            return true;
        }

        public BuildState.InstallResult install(PartDef part) {
            if (build == null) return BuildState.InstallResult.NO_SLOT;
            int stage = build.stageIndex();
            BuildState.InstallResult r = build.install(part, Ages.current(((ServerLevel) level).getServer()), 0);
            if (r == BuildState.InstallResult.OK) {
                parts.add(part);
                stageOf.add(new int[]{stage});
                setChanged();
            }
            return r;
        }

        /** Work for the given ticks; true when a stage finished. */
        public boolean work(int ticks) {
            if (build == null) return false;
            boolean done = build.advance(ticks, 1.0);
            setChanged();
            return done;
        }

        void tick() {
            if (build != null && level.getGameTime() % 4 == 0 && slotsFull()) work(4);
        }

        public String onUse(boolean sneak) {
            if (build == null || (sneak && parts.isEmpty())) {
                List<String> ids = YardRegistry.yards().stream().map(YardDef::id).filter(i -> i.startsWith(DiaithirCarts.ID + ":")).sorted().toList();
                String next = ids.get((ids.indexOf(yardId) + 1 + ids.size()) % ids.size());
                choose(next);
                return "Yard: " + next.substring(next.indexOf(':') + 1).replace('_', ' ');
            }
            if (build.complete()) {
                List<?> train = launch();
                return train == null ? "Nowhere to roll out: put a rail within 3 blocks." : "Rolled out a train of " + train.size() + " carts.";
            }
            YardDef.Stage st = def().stages().get(build.stageIndex());
            StringBuilder need = new StringBuilder();
            for (YardDef.Slot s : st.slots()) need.append(s.kind()).append(" x").append(s.count() - inStage(build.stageIndex(), s.kind())).append(' ');
            return "Stage " + (build.stageIndex() + 1) + "/" + def().stages().size() + " " + st.id() + ": needs " + need + "work " + (int) (build.stageProgress() * 100) + "%";
        }

        /** Rolls the finished train out; null (and nothing consumed) if there is no rail. */
        public List<?> launch() {
            if (build == null || !build.complete()) return null;
            Object out = YardRegistry.finish(yardId, level, worldPosition, build);
            if (!(out instanceof List<?> train)) return null;
            choose(yardId);
            return train;
        }

        @Override
        protected void saveAdditional(ValueOutput output) {
            super.saveAdditional(output);
            output.putString("Yard", yardId);
            if (build == null) return;
            output.putInt("Stage", build.stageIndex());
            output.putInt("Ticks", build.progressTicks());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.size(); i++) sb.append(i > 0 ? "," : "").append(stageOf.get(i)[0]).append(':').append(parts.get(i).item());
            output.putString("Log", sb.toString());
        }

        @Override
        protected void loadAdditional(ValueInput input) {
            super.loadAdditional(input);
            yardId = input.getStringOr("Yard", "");
            YardDef d = def();
            parts.clear();
            stageOf.clear();
            if (d == null) {
                build = null;
                return;
            }
            int stage = input.getIntOr("Stage", 0);
            List<PartDef> done = new ArrayList<>(), cur = new ArrayList<>();
            for (String e : input.getStringOr("Log", "").split(",")) {
                if (e.isEmpty()) continue;
                int c = e.indexOf(':');
                int st = Integer.parseInt(e.substring(0, c));
                PartDef p = YardRegistry.part(e.substring(c + 1)).orElse(null);
                if (p == null) continue;
                parts.add(p);
                stageOf.add(new int[]{st});
                (st < stage ? done : cur).add(p);
            }
            build = BuildState.restore(d, stage, input.getIntOr("Ticks", 0), done, cur);
        }
    }
}
