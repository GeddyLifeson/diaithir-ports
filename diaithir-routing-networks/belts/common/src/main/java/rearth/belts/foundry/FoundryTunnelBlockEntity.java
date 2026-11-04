package rearth.belts.foundry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import rearth.belts.BlockEntitiesContent;
import rearth.belts.api.item.ItemApi;

import java.util.ArrayDeque;
import java.util.Deque;

public class FoundryTunnelBlockEntity extends BlockEntity implements FoundryInventoryHolder {
    
    private static final int LANES = 2;
    private static final int SLOT_LIMIT = 64;
    private static final int MIN_DISTANCE = 2;
    private static final int TICKS_PER_BLOCK = 3;
    private static final int MAX_QUEUE = 32;
    private static final int MAX_DELIVERIES_PER_TICK = 8;
    private static final int LINK_CHECK_INTERVAL = 20;
    
    private enum Role {
        NONE,
        ENTRY,
        EXIT
    }
    
    private static final class QueuedItem {
        private final ItemStack stack;
        private final int lane;
        private int ticksLeft;
        
        private QueuedItem(ItemStack stack, int lane, int ticksLeft) {
            this.stack = stack;
            this.lane = lane;
            this.ticksLeft = ticksLeft;
        }
    }
    
    private final Deque<QueuedItem> queue = new ArrayDeque<>();
    private final EntryStorage entryStorage = new EntryStorage();
    
    private Role role = Role.NONE;
    private BlockPos partner;
    private int tier = FoundryTiers.MIN_TIER;
    
    public FoundryTunnelBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesContent.FOUNDRY_TUNNEL.get(), pos, state);
    }
    
    // returns a translation key describing the failure, or null if the link was created
    public static @Nullable String tryLink(Level level, BlockPos entryPos, BlockPos exitPos) {
        var entry = level.hasChunkAt(entryPos) ? level.getBlockEntity(entryPos, BlockEntitiesContent.FOUNDRY_TUNNEL.get()).orElse(null) : null;
        var exit = level.getBlockEntity(exitPos, BlockEntitiesContent.FOUNDRY_TUNNEL.get()).orElse(null);
        if (entry == null || exit == null || entry.isLinked() || exit.isLinked())
            return "message.belts.tunnel_unavailable";
        
        var delta = exitPos.subtract(entryPos);
        if (delta.getY() != 0 || (delta.getX() != 0) == (delta.getZ() != 0))
            return "message.belts.tunnel_invalid";
        
        var distance = Math.abs(delta.getX()) + Math.abs(delta.getZ());
        if (distance < MIN_DISTANCE) return "message.belts.tunnel_too_close";
        if (distance > FoundryTiers.maxTunnelDistance(entry.tier)) return "message.belts.tunnel_too_far";
        
        var stepX = Integer.signum(delta.getX());
        var stepZ = Integer.signum(delta.getZ());
        for (int i = 1; i < distance; i++) {
            var between = entryPos.offset(stepX * i, 0, stepZ * i);
            if (level.getBlockState(between).is(Blocks.BEDROCK))
                return "message.belts.tunnel_blocked";
        }
        
        entry.setLink(Role.ENTRY, exitPos);
        exit.setLink(Role.EXIT, entryPos);
        return null;
    }
    
    private void setLink(Role newRole, BlockPos other) {
        role = newRole;
        partner = other.immutable();
        setChanged();
    }
    
    public boolean isLinked() {
        return role != Role.NONE;
    }
    
    public void unlink(Level level) {
        var other = partner;
        resetLink(level);
        
        if (other == null || !level.hasChunkAt(other)) return;
        level.getBlockEntity(other, BlockEntitiesContent.FOUNDRY_TUNNEL.get()).ifPresent(otherTunnel -> {
            if (worldPosition.equals(otherTunnel.partner))
                otherTunnel.resetLink(level);
        });
    }
    
    private void resetLink(Level level) {
        if (role == Role.ENTRY) dropQueue(level);
        role = Role.NONE;
        partner = null;
        setChanged();
    }
    
    private void dropQueue(Level level) {
        for (var queued : queue) {
            FoundryTransfer.drop(level, worldPosition, queued.stack);
        }
        queue.clear();
        setChanged();
    }
    
    private boolean isLinkValid(Level level) {
        if (partner == null || !level.hasChunkAt(partner)) return true;
        var other = level.getBlockEntity(partner, BlockEntitiesContent.FOUNDRY_TUNNEL.get());
        if (other.isEmpty()) return false;
        
        var expectedRole = role == Role.ENTRY ? Role.EXIT : Role.ENTRY;
        return worldPosition.equals(other.get().partner) && other.get().role == expectedRole;
    }
    
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (role == Role.NONE) return;
        
        if (level.getGameTime() % LINK_CHECK_INTERVAL == 0 && !isLinkValid(level)) {
            resetLink(level);
            return;
        }
        
        if (role != Role.ENTRY || queue.isEmpty()) return;
        
        for (var queued : queue) {
            if (queued.ticksLeft > 0) queued.ticksLeft--;
        }
        
        deliver(level);
    }
    
    private void deliver(Level level) {
        var exit = level.hasChunkAt(partner) ? level.getBlockEntity(partner, BlockEntitiesContent.FOUNDRY_TUNNEL.get()).orElse(null) : null;
        if (exit == null) return;
        
        var exitFacing = exit.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        for (int i = 0; i < MAX_DELIVERIES_PER_TICK; i++) {
            var head = queue.peekFirst();
            if (head == null || head.ticksLeft > 0) return;
            
            var pushed = FoundryTransfer.push(level, partner, exitFacing, head.stack, head.lane, false);
            if (pushed <= 0) return;
            
            head.stack.shrink(pushed);
            if (head.stack.isEmpty()) queue.removeFirst();
            setChanged();
        }
    }
    
    public void upgrade(Player player, ItemStack stack) {
        tier = FoundryTiers.upgrade(tier, player, stack);
        setChanged();
    }
    
    @Override
    public @Nullable ItemApi.InventoryStorage getInventory(@Nullable Direction side) {
        var back = getBlockState().getValue(HorizontalDirectionalBlock.FACING).getOpposite();
        return role == Role.ENTRY && side == back ? entryStorage : null;
    }
    
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("role", role.ordinal());
        output.storeNullable("partner", BlockPos.CODEC, partner);
        output.putInt("tier", tier);
        
        var stored = output.childrenList("queue");
        for (var queued : queue) {
            var child = stored.addChild();
            child.store("stack", ItemStack.OPTIONAL_CODEC, queued.stack);
            child.putInt("lane", queued.lane);
            child.putInt("ticks", queued.ticksLeft);
        }
    }
    
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        partner = input.read("partner", BlockPos.CODEC).orElse(null);
        role = partner == null ? Role.NONE : Role.values()[Math.clamp(input.getIntOr("role", 0), 0, Role.values().length - 1)];
        tier = FoundryTiers.clamp(input.getIntOr("tier", FoundryTiers.MIN_TIER));
        
        queue.clear();
        input.childrenListOrEmpty("queue").stream().forEach(child -> {
            var stack = child.read("stack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
            if (stack.isEmpty()) return;
            queue.addLast(new QueuedItem(stack, Math.clamp(child.getIntOr("lane", 0), 0, LANES - 1), child.getIntOr("ticks", 0)));
        });
    }
    
    private final class EntryStorage implements ItemApi.InventoryStorage {
        
        @Override
        public boolean supportsExtraction() {
            return false;
        }
        
        @Override
        public int insert(ItemStack inserted, boolean simulate) {
            return insertToSlot(inserted, 0, simulate);
        }
        
        @Override
        public int insertToSlot(ItemStack inserted, int slot, boolean simulate) {
            if (role != Role.ENTRY || partner == null || inserted.isEmpty() || slot < 0 || slot >= LANES || queue.size() >= MAX_QUEUE)
                return 0;
            
            if (!simulate) {
                var ticks = partner.distManhattan(worldPosition) * TICKS_PER_BLOCK;
                queue.addLast(new QueuedItem(inserted.copy(), slot, ticks));
                setChanged();
            }
            return inserted.getCount();
        }
        
        @Override
        public int extract(ItemStack extracted, boolean simulate) {
            return 0;
        }
        
        @Override
        public int extractFromSlot(ItemStack extracted, int slot, boolean simulate) {
            return 0;
        }
        
        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
        }
        
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }
        
        @Override
        public int getSlotCount() {
            return LANES;
        }
        
        @Override
        public int getSlotLimit(int slot) {
            return SLOT_LIMIT;
        }
    }
}
