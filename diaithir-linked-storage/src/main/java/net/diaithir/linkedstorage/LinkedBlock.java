package net.diaithir.linkedstorage;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** The three linked blocks. Right click with a dye cycles a dye slot, empty-hand sneak cycles the lock, empty hand opens the chest or reports status. */
public final class LinkedBlock extends BaseEntityBlock {
    public static final MapCodec<LinkedBlock> CODEC = simpleCodec(p -> new LinkedBlock(p, LinkKind.CHEST));
    private final LinkKind kind;

    public LinkedBlock(Properties properties, LinkKind kind) {
        super(properties);
        this.kind = kind;
    }

    public LinkKind kind() {
        return kind;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LinkedBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player p && level.getBlockEntity(pos) instanceof LinkedBlockEntity be) be.setOwner(p.getUUID());
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof LinkedBlockEntity be)) return InteractionResult.PASS;
        if (!(stack.getItem() instanceof DyeItem)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!be.mayUse(player.getUUID())) return deny(player);
        int slot = Math.min(2, Math.max(0, hit.getDirection().get3DDataValue() % 3));
        be.cycleDye(slot);
        player.sendOverlayMessage(Component.literal("Frequency " + describe(be.frequency())));
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof LinkedBlockEntity be)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!be.mayUse(player.getUUID())) return deny(player);
        if (player.isShiftKeyDown()) {
            be.cycleLock();
            player.sendOverlayMessage(Component.literal("Lock " + be.lock().name().toLowerCase()));
            return InteractionResult.SUCCESS;
        }
        if (kind == LinkKind.CHEST && player instanceof ServerPlayer sp) {
            MenuProvider provider = new SimpleMenuProvider((id, inv, p) -> new LinkedMenu(id, inv, be.itemHandler()), Component.literal("Linked Chest " + describe(be.frequency())));
            sp.openMenu(provider);
        } else {
            player.sendOverlayMessage(Component.literal(kind.name().toLowerCase() + " " + describe(be.frequency()) + ", " + status(be)));
        }
        return InteractionResult.SUCCESS;
    }

    private static String status(LinkedBlockEntity be) {
        return switch (be.key().kind()) {
            case TANK -> be.shared().tank(be.key()).getAmount() + " mB";
            case CELL -> be.shared().cell(be.key()) + " FE";
            case CHEST -> "";
        };
    }

    private static InteractionResult deny(Player player) {
        player.sendOverlayMessage(Component.literal("This linked block is locked"));
        return InteractionResult.FAIL;
    }

    static String describe(int freq) {
        return Frequency.slot(freq, 0) + "/" + Frequency.slot(freq, 1) + "/" + Frequency.slot(freq, 2);
    }
}
