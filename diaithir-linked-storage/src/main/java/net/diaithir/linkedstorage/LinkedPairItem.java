package net.diaithir.linkedstorage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

/** Linker tool. First use on a linked block remembers it, second use on another of the same kind entangles both onto one private channel. */
public final class LinkedPairItem extends Item {
    public LinkedPairItem(Properties properties) {
        super(properties);
    }

    private static void say(Player player, String text) {
        player.sendOverlayMessage(Component.literal(text));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockEntity be = level.getBlockEntity(pos);
        Player player = ctx.getPlayer();
        if (!(be instanceof LinkedBlockEntity target) || player == null) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack tool = ctx.getItemInHand();
        CompoundTag tag = tool.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        GlobalPos here = GlobalPos.of(level.dimension(), pos);
        if (!target.mayUse(player.getUUID())) {
            say(player, "This linked block is locked");
            return InteractionResult.FAIL;
        }
        Optional<GlobalPos> first = tag.contains("first") ? GlobalPos.CODEC.parse(NbtOps.INSTANCE, tag.get("first")).result() : Optional.empty();
        if (first.isEmpty() || first.get().equals(here)) {
            tag.put("first", GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, here).result().orElseThrow());
            tool.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            say(player, "First block marked");
            return InteractionResult.SUCCESS;
        }
        GlobalPos other = first.get();
        var otherLevel = level.getServer().getLevel(other.dimension());
        if (otherLevel == null || !(otherLevel.getBlockEntity(other.pos()) instanceof LinkedBlockEntity second)
                || second.getBlockState().getBlock() != target.getBlockState().getBlock()) {
            tag.remove("first");
            tool.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            say(player, "The first block is gone or a different kind");
            return InteractionResult.FAIL;
        }
        int freq = Pair.frequencyFor(other, here);
        second.setOwner(target.owner());
        second.setFrequency(freq);
        target.setFrequency(freq);
        while (second.lock() != Lock.OWNER) second.cycleLock();
        while (target.lock() != Lock.OWNER) target.cycleLock();
        tag.remove("first");
        tool.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        say(player, "Blocks entangled on " + LinkedBlock.describe(freq));
        return InteractionResult.SUCCESS;
    }
}
