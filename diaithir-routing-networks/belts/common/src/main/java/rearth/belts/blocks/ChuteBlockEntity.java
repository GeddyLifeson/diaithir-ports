package rearth.belts.blocks;

import dev.architectury.platform.Platform;
import dev.ftb.mods.ftbfiltersystem.api.FTBFilterSystemAPI;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import rearth.belts.BlockContent;
import rearth.belts.BlockEntitiesContent;
import rearth.belts.ItemContent;
import rearth.belts.api.item.ItemApi;
import rearth.belts.collision.BeltCollisionRegistry;
import rearth.belts.util.SplineUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class ChuteBlockEntity extends BlockEntity implements BlockEntityTicker<ChuteBlockEntity> {

    // config
    private static final float BASE_BELT_SPEED = 0.9f;
    // total items per second (both lanes) for tiers 1..4
    private static final float[] TIER_ITEMS_PER_SECOND = {7.5f, 15f, 22.5f, 30f};
    // blocks between items on one lane. Identical for every tier, as belt speed and lane throughput scale together.
    private static final float ITEM_QUEUE_SPACING = BASE_BELT_SPEED / (TIER_ITEMS_PER_SECOND[0] / 2f);
    private static final float MIN_INSERT_GAP_FACTOR = 0.7f;
    private static final float MAX_EXTRACTION_BUDGET = 2f;
    private static final int LANE_COUNT = 2;
    private static final int DEFAULT_BELT_TIER = 1;
    private static final int MAX_BELT_TIER = TIER_ITEMS_PER_SECOND.length;
    
    // everything in this section is synced to the client
    private BlockPos target;
    private List<BlockPos> midPoints = new ArrayList<>();
    private int beltTier = DEFAULT_BELT_TIER;
    // items that are in transit, one deque per lane. Progress is [0-1] along the current path;
    // first = at begin of transit path, near extraction point. Last = near target.
    private final List<Deque<BeltItem>> lanes = List.of(new ArrayDeque<>(), new ArrayDeque<>());
    
    // number of items actively waiting / blocked by the output side of the belt, per lane.
    private final int[] outputQueue = new int[LANE_COUNT];
    
    // this is calculated on both the client and server
    private BeltData beltData;
    
    // used to check if a belt is used as target. Periodically updated on belt ends from the belt starts.
    private long lastTargetedTime;
    private BlockPos sourceBeltPos = BlockPos.ZERO;
    
    // used for filtering. Optionally works with create and ftb filters.
    public ItemStack filteredItem = ItemStack.EMPTY;
    
    // client only data, used for rendering
    public final Map<Short, Vec3> lastRenderedPositions = new HashMap<>();
    public final Map<Long, Integer> cachedLightCoords = new HashMap<>();
    
    private boolean networkDirty = false;
    private float extractionBudget = 0;
    private int nextLane = 0;
    
    public ChuteBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesContent.CHUTE_BLOCK.get(), pos, state);
    }
    
    @Override
    public void tick(Level level, BlockPos pos, BlockState state, ChuteBlockEntity blockEntity) {
        if (target == null || target.equals(BlockPos.ZERO)) {
            BeltCollisionRegistry.unregister(this);
            if (!level.isClientSide() && hasMovingItems()) {
                dropContent(level, pos);
            }
            return;
        }
        
        if (beltData == null) {
            beltData = BeltData.create(this);
            if (level instanceof ServerLevel serverLevel)
                serverLevel.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        }
        
        if (beltData == null) {
            BeltCollisionRegistry.unregister(this);
            target = null;
            midPoints = new ArrayList<>();
            return;
        }

        BeltCollisionRegistry.register(this);
        
        if (level.isClientSide()) return;
        
        moveItemsOnBelt();
        loadItemsOnBelt();
        
        // refresh target
        if (level.getGameTime() % 19 == 0)
            assignTargetState(level);
        
        
        if (networkDirty && level instanceof ServerLevel serverWorld) {
            serverWorld.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
            networkDirty = false;
        }
        
    }
    
    public void dropContent(Level level, BlockPos pos) {
        BeltCollisionRegistry.unregister(this);
        
        // notify source to be reset
        if (level.getGameTime() - this.lastTargetedTime < 20 && !this.sourceBeltPos.equals(BlockPos.ZERO) && level instanceof ServerLevel serverWorld) {
            var sourceEntityCandidate = level.getBlockEntity(this.sourceBeltPos, BlockEntitiesContent.CHUTE_BLOCK.get());
            if (sourceEntityCandidate.isPresent() && sourceEntityCandidate.get() != this) {
                var source = sourceEntityCandidate.get();
                source.dropContent(level, pos);
                source.target = null;
                serverWorld.sendBlockUpdated(this.sourceBeltPos, source.getBlockState(), source.getBlockState(), Block.UPDATE_ALL);
                source.networkDirty = true;
                source.setChanged();
            }
        }
        
        var spawnAt = pos.getCenter();
        for (var lane : lanes) {
            for (var beltItem : lane) {
                level.addFreshEntity(new ItemEntity(level, spawnAt.x, spawnAt.y, spawnAt.z, beltItem.stack));
            }
        }
        
        if (hasMovingItems() || (target != null && !target.equals(BlockPos.ZERO))) {
            // pretend to drop an actual belt
            var stack = new ItemStack(ItemContent.beltForTier(beltTier).get(), 1);
            level.addFreshEntity(new ItemEntity(level, spawnAt.x, spawnAt.y, spawnAt.z, stack));
        }
        
        lanes.forEach(Deque::clear);
    }
    
    // notifies the belt end entity that the current entity is the sender to it
    private void assignTargetState(Level level) {
        var beltTargetCandidate = level.getBlockEntity(target, BlockEntitiesContent.CHUTE_BLOCK.get());
        if (beltTargetCandidate.isPresent()) {
            beltTargetCandidate.get().lastTargetedTime = level.getGameTime();
            beltTargetCandidate.get().sourceBeltPos = worldPosition;
        } else {
            target = null;
            midPoints = new ArrayList<>();
        }
    }
    
    private boolean hasMovingItems() {
        return !lanes.get(0).isEmpty() || !lanes.get(1).isEmpty();
    }
    
    // lanes take turns being processed first, so the output inventory is fed evenly
    private void moveItemsOnBelt() {
        var first = (int) (level.getGameTime() & 1);
        moveLane(first);
        moveLane(1 - first);
    }
    
    private void moveLane(int lane) {
        
        var progressDelta = getBeltSpeed() / beltData.totalLength() / 20f;
        
        outputQueue[lane] = 0;
        
        var iterator = lanes.get(lane).descendingIterator();
        while (iterator.hasNext()) {
            var item = iterator.next();
            var newProgress = item.progress + progressDelta;
            
            var inQueue = newProgress >= getPotentialQueueStart(lane);
            
            // only accept new position if not in queue
            if (inQueue) {
                outputQueue[lane]++;
            } else {
                item.progress = (float) newProgress;
                networkDirty = true;
            }
            
            // try to insert last item (if its in queue). Gets put into queue when the end is reached.
            if (inQueue && outputQueue[lane] == 1) {
                var conveyorEndEntityCandidate = level.getBlockEntity(target, BlockEntitiesContent.CHUTE_BLOCK.get());
                if (conveyorEndEntityCandidate.isEmpty()) continue;
                var conveyorEndEntity = conveyorEndEntityCandidate.get();
                var targetInv = ItemApi.BLOCK.find(level, target.relative(conveyorEndEntity.getOwnFacing().getOpposite()), null, null, conveyorEndEntity.getOwnFacing());
                if (targetInv == null) continue;
                
                var insertionStack = item.stack;
                var insertedAmount = targetInv.insert(insertionStack, true);
                if (insertedAmount == insertionStack.getCount()) {
                    targetInv.insert(insertionStack, false);
                    iterator.remove();
                    outputQueue[lane] = 0;    // reset queue
                    networkDirty = true;
                }
            }
        }
        
    }
    
    @SuppressWarnings("DataFlowIssue")
    private void loadItemsOnBelt() {
        extractionBudget = Math.min(extractionBudget + getItemsPerSecond() / 20f, MAX_EXTRACTION_BUDGET);
        if (extractionBudget < 1) return;
        
        var source = ItemApi.BLOCK.find(level, worldPosition.relative(getOwnFacing().getOpposite()), null, null, getOwnFacing());
        if (source == null) {
            extractionBudget = 1;
            return;
        }
        
        while (extractionBudget >= 1) {
            if (!extractOne(source)) {
                extractionBudget = Math.min(extractionBudget, 1);
                return;
            }
            extractionBudget -= 1;
        }
    }
    
    // takes a single item from the source and puts it on the next lane in round robin order
    private boolean extractOne(ItemApi.InventoryStorage source) {
        var lane = nextLane;
        if (!laneHasRoom(lane)) lane = 1 - lane;
        if (!laneHasRoom(lane)) return false;
        
        for (int slot = 0; slot < source.getSlotCount(); slot++) {
            var availableStack = source.getStackInSlot(slot);
            if (availableStack.isEmpty() || !stackMatchesFilter(availableStack)) continue;
            
            var extractingStack = availableStack.copyWithCount(1);
            if (source.extract(extractingStack, false) <= 0) continue;
            
            addToLane(lane, extractingStack);
            nextLane = 1 - lane;
            return true;
        }
        
        return false;
    }
    
    @SuppressWarnings("DataFlowIssue")
    private void addToLane(int lane, ItemStack stack) {
        var id = (short) level.getRandom().nextIntBetweenInclusive(Short.MIN_VALUE, Short.MAX_VALUE);
        lanes.get(lane).addFirst(new BeltItem(id, lane, stack));
        setChanged();
        networkDirty = true;
    }
    
    private boolean laneHasRoom(int lane) {
        if (beltData == null) return false;
        var newest = lanes.get(lane).peekFirst();
        return newest == null || newest.progress * beltData.totalLength() >= ITEM_QUEUE_SPACING * MIN_INSERT_GAP_FACTOR;
    }
    
    // inserts a single item on the given lane at the start of the belt. Returns the amount inserted (0 or 1).
    public int insertOnLane(ItemStack stack, int lane, boolean simulate) {
        if (stack.isEmpty() || lane < 0 || lane >= LANE_COUNT) return 0;
        if (level == null || level.isClientSide() || target == null || target.equals(BlockPos.ZERO)) return 0;
        if (!laneHasRoom(lane)) return 0;
        
        if (!simulate)
            addToLane(lane, stack.copyWithCount(1));
        return 1;
    }
    
    private boolean stackMatchesFilter(ItemStack stack) {
        if (filteredItem.isEmpty()) return true;
        
        if (Platform.isModLoaded("ftbfiltersystem")) {
            var filterAPI = FTBFilterSystemAPI.api();
            if (filterAPI.isFilterItem(filteredItem))
                return filterAPI.doesFilterMatch(filteredItem, stack, level.registryAccess());
        }
        
        return stack.getItem().equals(filteredItem.getItem());
    }
    
    private float getPotentialQueueStart(int lane) {
        return (float) (1 - outputQueue[lane] * ITEM_QUEUE_SPACING / beltData.totalLength());
    }
    
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("target", BlockPos.CODEC, target);
        output.store("midpoints", BlockPos.CODEC.listOf(), midPoints);
        output.store("filter", ItemStack.OPTIONAL_CODEC, filteredItem);
        output.putInt("beltTier", beltTier);

        var positions = output.childrenList("moving");
        for (var lane : lanes) {
            for (var pair : lane) {
                var item = positions.addChild();
                item.putFloat("progress", pair.progress);
                item.store("stack", ItemStack.OPTIONAL_CODEC, pair.stack);
                item.putShort("id", pair.id);
                item.putByte("lane", pair.lane);
            }
        }
    }
    
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        var loadedTarget = input.read("target", BlockPos.CODEC).orElse(null);
        var loadedMidPoints = new ArrayList<>(input.read("midpoints", BlockPos.CODEC.listOf()).orElse(List.of()));
        var pathChanged = !Objects.equals(target, loadedTarget) || !midPoints.equals(loadedMidPoints);

        target = loadedTarget;
        midPoints = loadedMidPoints;
        filteredItem = input.read("filter", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        beltTier = clampBeltTier(input.getIntOr("beltTier", DEFAULT_BELT_TIER));

        lanes.forEach(Deque::clear);
        input.childrenListOrEmpty("moving").stream().forEach(item -> {
            var progress = item.getFloatOr("progress", 0);
            var id = (short) item.getShortOr("id", (short) 0);
            var stack = item.read("stack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
            var lane = Math.clamp(item.getByteOr("lane", (byte) 0), 0, LANE_COUNT - 1);
            lanes.get(lane).addLast(new BeltItem(progress, id, lane, stack));
        });
        
        if (level != null && (pathChanged || beltData == null)) {
            beltData = BeltData.create(this);
        }
        
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }
    
    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    public Iterable<BeltItem> getMovingItems() {
        return () -> Stream.concat(lanes.get(0).stream(), lanes.get(1).stream()).iterator();
    }
    
    public BlockPos getTarget() {
        return target;
    }
    
    public BeltData getBeltData() {
        return beltData;
    }
    
    public Direction getOwnFacing() {
        return getBlockState().getValue(HorizontalDirectionalBlock.FACING);
    }

    public int getBeltSpeedMultiplier() {
        return beltTier;
    }

    public float getBeltSpeed() {
        return BASE_BELT_SPEED * getBeltSpeedMultiplier();
    }

    public int getBeltTier() {
        return beltTier;
    }

    private float getItemsPerSecond() {
        return TIER_ITEMS_PER_SECOND[beltTier - 1];
    }
    
    public boolean isUsed() {
        var usedAsTarget = level.getGameTime() - lastTargetedTime < 40;
        var usedAsSource = target != null && !target.equals(BlockPos.ZERO);
        return usedAsTarget || usedAsSource;
    }
    
    public void assignFromBeltItem(BlockPos target, List<BlockPos> midpoints, int beltTier) {
        this.target = target;
        this.midPoints = midpoints;
        this.beltTier = clampBeltTier(beltTier);
        beltData = BeltData.create(this);
        networkDirty = true;
        this.setChanged();
        
        if (level instanceof ServerLevel serverWorld)
            serverWorld.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public void setRemoved() {
        BeltCollisionRegistry.unregister(this);
        super.setRemoved();
    }

    private static int clampBeltTier(int tier) {
        return Math.clamp(tier, DEFAULT_BELT_TIER, MAX_BELT_TIER);
    }
    
    public List<Pair<BlockPos, Direction>> getMidPointsWithTangents() {
        return midPoints.stream()
                 .filter(point -> level.getBlockState(point).getBlock().equals(BlockContent.CONVEYOR_SUPPORT_BLOCK.get()))
                 .map(point -> new Pair<>(point, level.getBlockState(point).getValue(HorizontalDirectionalBlock.FACING)))
                 .toList();
    }
    
    public void assignFilterItem(ItemStack stack, Player player) {
        
        if (stack.isEmpty()) {
            resetFilterItem(player);
            return;
        }
        
        player.sendSystemMessage(Component.translatable("message.belts.filter_set"));
        filteredItem = stack.copy();
        this.setChanged();
        
        if (level instanceof ServerLevel serverWorld)
            serverWorld.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }
    
    public void resetFilterItem(Player player) {
        player.sendSystemMessage(Component.translatable("message.belts.filter_reset"));
        filteredItem = ItemStack.EMPTY;
        this.setChanged();
        
        if (level instanceof ServerLevel serverWorld)
            serverWorld.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }
    
    public static class BeltItem {
        public float progress;
        public final short id;
        public final byte lane;
        public final ItemStack stack;
        
        public BeltItem(short id, int lane, ItemStack stack) {
            this.id = id;
            this.lane = (byte) lane;
            this.stack = stack;
        }
        
        public BeltItem(float progress, short id, int lane, ItemStack stack) {
            this.id = id;
            this.lane = (byte) lane;
            this.stack = stack;
            this.progress = progress;
        }
    }
    
    public record BeltData(List<Pair<Vec3, Vec3>> allPoints, double totalLength, double[] segmentLengths) {
        
        public static @Nullable BeltData create(ChuteBlockEntity entity) {
            
            if (entity.getLevel() == null || entity.target == null || entity.target.equals(BlockPos.ZERO))
                return null;
            
            var targetCandidate = entity.getLevel().getBlockEntity(entity.getTarget(), BlockEntitiesContent.CHUTE_BLOCK.get());
            if (targetCandidate.isEmpty()) return null;
            
            var conveyorStartPoint = entity.getBlockPos();
            var conveyorEndPoint = entity.getTarget();
            var conveyorStartDir = Vec3.atLowerCornerOf(entity.getOwnFacing().getUnitVec3i());
            var conveyorFacing = targetCandidate.get().getOwnFacing();
            var conveyorEndDir = Vec3.atLowerCornerOf(conveyorFacing.getOpposite().getUnitVec3i());
            
            var conveyorMidPointsVisual = entity.getMidPointsWithTangents();
            var conveyorStartPointVisual = conveyorStartPoint.getCenter().add(conveyorStartDir.scale(-0.5f));
            var conveyorEndPointVisual = conveyorEndPoint.getCenter().add(conveyorEndDir.scale(0.5f));
            
            var transformedMidPoints = conveyorMidPointsVisual.stream().map(elem -> new Pair<>(elem.getFirst().getCenter(), Vec3.atLowerCornerOf(elem.getSecond().getUnitVec3i()))).toList();
            var segmentPoints = SplineUtil.getPointPairs(conveyorStartPointVisual, conveyorStartDir, conveyorEndPointVisual, conveyorEndDir, transformedMidPoints);
            
            var segmentLengths = new double[segmentPoints.size() - 1];
            var totalLength = 0d;
            for (int i = 0; i < segmentPoints.size() - 1; i++) {
                var from = segmentPoints.get(i);
                var to = segmentPoints.get(i + 1);
                var length = SplineUtil.getLineLength(from.getFirst(), from.getSecond(), to.getFirst(), to.getSecond());
                segmentLengths[i] = (length);
                totalLength += length;
            }
            
            return new BeltData(segmentPoints, totalLength, segmentLengths);
        }
        
    }
}
