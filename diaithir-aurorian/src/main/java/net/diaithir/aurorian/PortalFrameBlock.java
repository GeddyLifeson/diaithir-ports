package net.diaithir.aurorian;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** A frame block; a lighter (tag theaurorian:portal_lighters) used on it fills a closed frame with portal. */
public class PortalFrameBlock extends Block {
    private static final TagKey<Item> LIGHTERS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DiaithirAurorian.ID, "portal_lighters"));

    public PortalFrameBlock(Properties p) {
        super(p);
    }

    public static void light(Level level, PortalFrameShape shape) {
        Direction facing = shape.axis == Direction.Axis.X ? Direction.NORTH : Direction.EAST;
        BlockState portal = DiaithirAurorian.PORTAL.get().defaultBlockState().setValue(AurorianPortalBlock.FACING, facing);
        shape.interior.forEach(p -> level.setBlock(p, portal, 18));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(LIGHTERS)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        PortalFrameShape shape = PortalFrameShape.find(level, pos.relative(hit.getDirection()));
        if (shape == null) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!level.isClientSide()) {
            light(level, shape);
            level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
            stack.hurtAndBreak(1, player, hand);
        }
        return InteractionResult.SUCCESS;
    }
}
