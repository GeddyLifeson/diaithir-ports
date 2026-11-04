// Ported from Structure Compass (MIT) https://github.com/Mrbysco/StructureCompass
// Modifications: Tag locating fixed to use tag holder sets, tooltip moved to client ItemTooltipEvent, config/registry names changed.
package net.diaithir.structurenavigation.structurecompass.items;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.diaithir.structurenavigation.structurecompass.component.StructureInfo;
import net.diaithir.structurenavigation.structurecompass.config.CompassConfig;
import net.diaithir.structurenavigation.structurecompass.network.OpenCompassPayload;
import net.diaithir.structurenavigation.structurecompass.registry.CompassComponents;
import net.diaithir.structurenavigation.structurecompass.util.AsyncLocator;
import net.diaithir.structurenavigation.structurecompass.util.StructureUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.network.PacketDistributor;

public class StructureCompassItem extends Item {

    public StructureCompassItem(Properties builder) {
        super(builder);
    }

    @Override
    public InteractionResult use(Level level, Player playerIn, InteractionHand hand) {
        ItemStack stack = playerIn.getItemInHand(hand);
        if (playerIn.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                List<Identifier> allStructures = StructureUtil.getAvailableStructureList(level);
                List<Identifier> allTags = StructureUtil.getAvailableTagList(level);
                PacketDistributor.sendToPlayer((ServerPlayer) playerIn, new OpenCompassPayload(hand, stack, allStructures, allTags));
            }
        } else {
            locateStructure(stack, playerIn);
        }

        return super.use(level, playerIn, hand);
    }

    private void locateStructure(ItemStack stack, Player player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (!stack.has(CompassComponents.STRUCTURE.get())) {
            player.sendSystemMessage(Component.translatable("structurecompass.structure.unset.tooltip").withStyle(ChatFormatting.YELLOW));
            return;
        }
        Identifier structureLocation = stack.get(CompassComponents.STRUCTURE.get());
        boolean isTag = stack.getOrDefault(CompassComponents.IS_TAG.get(), false);

        if (structureLocation == null || StructureUtil.isBlacklisted(structureLocation)) {
            player.sendSystemMessage(Component.translatable("structurecompass.locate.fail").withStyle(ChatFormatting.RED));
            return;
        }

        Registry<Structure> registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        HolderSet<Structure> holderSet;
        if (isTag) {
            holderSet = registry.get(TagKey.create(Registries.STRUCTURE, structureLocation)).orElse(null);
        } else {
            Holder.Reference<Structure> single = registry.get(ResourceKey.create(Registries.STRUCTURE, structureLocation)).orElse(null);
            if (single != null && single.is(Tags.Structures.HIDDEN_FROM_LOCATOR_SELECTION)) {
                stack.remove(CompassComponents.STRUCTURE.get());
                player.sendSystemMessage(Component.translatable("structurecompass.locate.structure_prohibited").withStyle(ChatFormatting.RED));
                return;
            }
            holderSet = single == null ? null : HolderSet.direct(single);
        }
        if (holderSet == null) {
            return;
        }

        player.sendSystemMessage(Component.translatable("structurecompass.structure.locating", structureLocation.toString()).withStyle(ChatFormatting.YELLOW));

        boolean findUnexplored = CompassConfig.LOCATE_UNEXPLORED.get();

        if (CompassConfig.LOCATE_ASYNC.get()) {
            AsyncLocator.locate(level, holderSet, player.blockPosition(), 100, findUnexplored)
                    .thenOnServerThread(pair -> bindPosition(stack, structureLocation, player, level, pair));
        } else {
            Pair<BlockPos, Holder<Structure>> pair = StructureUtil.findNearestMapStructure(level,
                    holderSet, player.blockPosition(), 100, findUnexplored);
            bindPosition(stack, structureLocation, player, level, pair);
        }
    }

    private void bindPosition(ItemStack stack, Identifier boundStructure, Player player, Level level, Pair<BlockPos, Holder<Structure>> pair) {
        BlockPos structurePos = pair != null ? pair.getFirst() : null;
        if (structurePos == null) {
            stack.remove(CompassComponents.STRUCTURE_INFO.get());
            int range = CompassConfig.COMPASS_RANGE.get();
            player.sendSystemMessage(Component.translatable("structurecompass.structure.failed", boundStructure.toString(), range).withStyle(ChatFormatting.RED));
        } else {
            StructureInfo info = new StructureInfo(GlobalPos.of(level.dimension(), structurePos));
            stack.set(CompassComponents.STRUCTURE_INFO.get(), info);
            int distance = player.blockPosition().distManhattan(structurePos);
            player.sendSystemMessage(Component.translatable("structurecompass.structure.found", boundStructure.toString(), distance).withStyle(ChatFormatting.GREEN));
        }

        player.getCooldowns().addCooldown(stack, 100);
    }
}
