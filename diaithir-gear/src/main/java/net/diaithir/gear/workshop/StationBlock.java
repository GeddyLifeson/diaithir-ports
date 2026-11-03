package net.diaithir.gear.workshop;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * A parts bench. No GUI: click part items onto it, sneak + empty hand returns the parts (or picks the next blueprint when
 * the bench is empty), empty hand takes the finished item. Hold a finished item and click to apply honing; hold it with a
 * spare improvement item in the off hand to improve it.
 */
public final class StationBlock extends BaseEntityBlock {
    public static final MapCodec<StationBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(propertiesCodec(),
            com.mojang.serialization.Codec.STRING.fieldOf("station").forGetter(b -> b.station)).apply(i, StationBlock::new));
    public final String station;

    public StationBlock(Properties p, String station) {
        super(p);
        this.station = station;
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new StationEntity(pos, state); }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof StationEntity be)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        String sch = stack.get(Workshop.SCHEMATIC.get());
        if (sch != null) {
            Craft.Blueprint b = Catalog.blueprints().get(sch);
            if (b == null) { say(player, "workshop.diaithirgear.unknown_schematic"); return InteractionResult.SUCCESS; }
            Workshop.learn(player, sch);
            stack.shrink(1);
            player.sendOverlayMessage(Component.translatable("workshop.diaithirgear.learned", Workshop.name(b)));
            return InteractionResult.SUCCESS;
        }
        if (stack.has(Workshop.ASSEMBLY.get())) {
            be.service(player, stack);
            return InteractionResult.SUCCESS;
        }
        be.place(player, stack);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof StationEntity be)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (player.isShiftKeyDown()) be.sneakUse(player); else be.take(player);
        return InteractionResult.SUCCESS;
    }

    static void say(Player p, String key, Object... args) { p.sendOverlayMessage(Component.translatable(key, args)); }
}
