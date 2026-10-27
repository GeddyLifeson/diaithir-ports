package net.diaithir.goety.rite;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.diaithir.goety.GoetyEntities;
import net.diaithir.goety.GoetyBlockEntities;
import net.diaithir.goety.entity.Servant;
import net.diaithir.goety.magic.Souls;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/** The dark altar: holds the activation item and runs a rite with the offerings on the pedestals around it. */
public class AltarBlockEntity extends HolderBlockEntity {
    public static final int RANGE = 8;
    private int ritual = -1;      // index into RitualManager.all(), -1 when idle
    private int time;
    private @Nullable UUID caster;
    /** What the last completed rite produced (kept for tests and for the Library of the altar). */
    public ItemStack lastOutput = ItemStack.EMPTY;
    public @Nullable Mob lastSummoned;
    private @Nullable Player casterRef; // the player object that began the rite, until the world reloads

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(GoetyBlockEntities.ALTAR.get(), pos, state);
    }

    public boolean busy() {
        return ritual >= 0;
    }

    /** Pedestal positions around the altar that hold something. */
    List<BlockPos> pedestals() {
        List<BlockPos> out = new ArrayList<>();
        if (level == null) return out;
        for (BlockPos p : BlockPos.betweenClosed(worldPosition.offset(-RANGE, -3, -RANGE), worldPosition.offset(RANGE, 3, RANGE))) {
            if (level.getBlockEntity(p) instanceof PedestalBlockEntity pe && !pe.held().isEmpty()) out.add(p.immutable());
        }
        return out;
    }

    private List<ItemStack> stacksAt(List<BlockPos> ps) {
        List<ItemStack> out = new ArrayList<>();
        for (BlockPos p : ps) out.add(((PedestalBlockEntity) level.getBlockEntity(p)).held());
        return out;
    }

    /** Tries to begin a rite with the stack in the player's hand. Returns true when one began. */
    public boolean tryStart(Player player, ItemStack hand) {
        if (busy() || !held.isEmpty() || level == null || level.isClientSide()) return false;
        List<Ritual> all = RitualManager.all();
        List<BlockPos> ps = pedestals();
        List<ItemStack> stacks = stacksAt(ps);
        boolean sawActivation = false;
        for (int i = 0; i < all.size(); i++) {
            Ritual r = all.get(i);
            if (!r.activatedBy(hand)) continue;
            sawActivation = true;
            if (r.assign(stacks) == null) continue;
            int total = r.soulCost() * r.duration();
            if (!player.isCreative() && Souls.total(player) < total) {
                player.sendOverlayMessage(Component.translatable("goety.msg.rite_souls", total));
                return false;
            }
            setHeld(hand.split(1));
            ritual = i;
            time = 0;
            caster = player.getUUID();
            casterRef = player;
            level.setBlock(worldPosition, getBlockState().setValue(AltarBlock.LIT, true), 3);
            level.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 0.6F);
            return true;
        }
        player.sendOverlayMessage(Component.translatable(sawActivation ? "goety.msg.rite_offerings" : "goety.msg.rite_unknown"));
        return false;
    }

    private void stop(boolean ok) {
        ritual = -1;
        time = 0;
        if (level != null) {
            level.setBlock(worldPosition, getBlockState().setValue(AltarBlock.LIT, false), 3);
            level.playSound(null, worldPosition, ok ? SoundEvents.ENCHANTMENT_TABLE_USE : SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AltarBlockEntity be) {
        if (!(level instanceof ServerLevel sl) || be.ritual < 0) return;
        List<Ritual> all = RitualManager.all();
        if (be.ritual >= all.size()) { be.stop(false); return; }
        Ritual r = all.get(be.ritual);
        Player player = be.caster == null ? null : sl.getPlayerByUUID(be.caster);
        if (player == null && be.casterRef != null && be.casterRef.getUUID().equals(be.caster)) player = be.casterRef;
        List<BlockPos> ps = be.pedestals();
        List<Integer> used = r.assign(be.stacksAt(ps));
        if (player == null || used == null) {
            if (player != null) player.sendOverlayMessage(Component.translatable("goety.msg.rite_broken"));
            be.stop(false);
            return;
        }
        be.time++;
        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 1, 0.2, 0.1, 0.2, 0.01);
        for (int k : used) {
            BlockPos p = ps.get(k);
            sl.sendParticles(ParticleTypes.ENCHANT, p.getX() + 0.5, p.getY() + 1.3, p.getZ() + 0.5, 2, 0.15, 0.2, 0.15, 0.1);
        }
        if (be.time % 20 == 0) {
            if (!Souls.consume(player, r.soulCost())) {
                player.sendOverlayMessage(Component.translatable("goety.msg.rite_souls", r.soulCost()));
                be.stop(false);
                return;
            }
            sl.playSound(null, pos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 1.0F, 0.9F);
        }
        if (be.time >= r.duration() * 20) be.finish(sl, r, player, ps, used);
    }

    private void finish(ServerLevel sl, Ritual r, Player player, List<BlockPos> ps, List<Integer> used) {
        for (int k : used) {
            BlockPos p = ps.get(k);
            if (sl.getBlockEntity(p) instanceof PedestalBlockEntity pe) pe.setHeld(ItemStack.EMPTY);
            sl.sendParticles(ParticleTypes.POOF, p.getX() + 0.5, p.getY() + 1.2, p.getZ() + 0.5, 4, 0.1, 0.1, 0.1, 0.02);
        }
        setHeld(ItemStack.EMPTY);
        if (r.type().equals("summon") && !r.entity().isEmpty()) {
            EntityType<?> t = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(r.entity()));
            if (t.create(sl, EntitySpawnReason.MOB_SUMMONED) instanceof Mob m) {
                m.snapTo(worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, sl.getRandom().nextFloat() * 360F, 0);
                if (m instanceof Servant s) s.core().setOwner(player);
                sl.addFreshEntity(m);
                lastSummoned = m;
            }
        } else if (r.resultItem() != null) {
            ItemStack out = new ItemStack(r.resultItem(), r.count());
            lastOutput = out.copy();
            ItemEntity ie = new ItemEntity(sl, worldPosition.getX() + 0.5, worldPosition.getY() + 1.1, worldPosition.getZ() + 0.5, out);
            ie.setDeltaMovement(0, 0.15, 0);
            sl.addFreshEntity(ie);
        }
        sl.sendParticles(ParticleTypes.SOUL, worldPosition.getX() + 0.5, worldPosition.getY() + 1.2, worldPosition.getZ() + 0.5, 20, 0.3, 0.4, 0.3, 0.05);
        stop(true);
    }

    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putInt("Ritual", ritual);
        out.putInt("Time", time);
        if (caster != null) out.putString("Caster", caster.toString());
    }

    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        ritual = in.getIntOr("Ritual", -1);
        time = in.getIntOr("Time", 0);
        caster = in.getString("Caster").map(s -> { try { return UUID.fromString(s); } catch (IllegalArgumentException e) { return null; } }).orElse(null);
    }
}
