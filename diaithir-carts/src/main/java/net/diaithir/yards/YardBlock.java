package net.diaithir.yards;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * The yard controller. Use a finished vehicle on it to choose that design (the vehicle is consumed and rolls out again when built), then use parts on it
 * stage by stage. Empty hand shows what is needed.
 */
public final class YardBlock extends BaseEntityBlock {
    public static final MapCodec<YardBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(propertiesCodec(),
            Codec.STRING.fieldOf("tier").forGetter(b -> b.tier.name())).apply(i, (p, t) -> new YardBlock(p, YardDef.Tier.valueOf(t))));

    private final YardDef.Tier tier;

    public YardBlock(Properties p, YardDef.Tier tier) {
        super(p);
        this.tier = tier;
    }

    public YardDef.Tier tier() { return tier; }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new YardBlockEntity(pos, state); }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, YardContent.YARD_BE.get(), (l, p, s, be) -> be.tick());
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof YardBlockEntity be)) return InteractionResult.PASS;
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        var part = YardRegistry.part(id);
        if (part.isPresent()) {
            BuildState.InstallResult r = be.install(part.get());
            if (r == BuildState.InstallResult.OK) stack.shrink(1);
            player.sendOverlayMessage(Component.literal(switch (r) {
                case OK -> "Installed. " + be.describe().getString();
                case WRONG_STAGE -> "Not yet. Finish the current stage first.";
                case NO_SLOT -> "This stage does not take that part.";
                case NEEDS_CRANE -> "Too heavy to place by hand. Build a crane within " + YardBlockEntity.REACH + " blocks with enough lift.";
                case AGE_LOCKED -> "Your age cannot use that part yet.";
                case TIER_LOCKED -> "This yard is too basic for that.";
                case DONE -> "Pick a design first: use a finished vehicle on the yard.";
            }));
            return InteractionResult.CONSUME;
        }
        String design = "yard_" + id.substring(id.indexOf(':') + 1);
        if (YardRegistry.yard(design).isPresent()) {
            String err = be.select(design);
            if (err == null) stack.shrink(1);
            player.sendOverlayMessage(Component.literal(err == null ? "Design chosen. " + be.describe().getString() : err));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof YardBlockEntity be) player.sendOverlayMessage(be.describe());
        return InteractionResult.CONSUME;
    }
}
