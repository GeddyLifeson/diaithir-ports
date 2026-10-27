package cn.leolezury.eternalstarlight.common.item.misc;

import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import cn.leolezury.eternalstarlight.common.data.ESPaintingVariant;
import cn.leolezury.eternalstarlight.common.data.ESRegistries;
import cn.leolezury.eternalstarlight.common.entity.misc.ESPainting;
import cn.leolezury.eternalstarlight.common.registry.ESEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.List;
import java.util.Optional;

public class ESPaintingItem extends HangingEntityItem {
	private static final Component TOOLTIP_RANDOM_VARIANT = Component.translatable("painting.random").withStyle(ChatFormatting.GRAY);

	public ESPaintingItem(Properties properties) {
		super(ESEntities.PAINTING.get(), properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		BlockPos blockPos = context.getClickedPos();
		Direction direction = context.getClickedFace();
		BlockPos relativePos = blockPos.relative(direction);
		Player player = context.getPlayer();
		ItemStack itemStack = context.getItemInHand();
		if (player != null && !this.mayPlace(player, direction, itemStack, relativePos)) {
			return InteractionResult.FAIL;
		} else {
			Level level = context.getLevel();
			Optional<ESPainting> optional = ESPainting.createPainting(level, itemStack.copyWithCount(1), relativePos, direction);
			if (optional.isEmpty()) {
				return InteractionResult.CONSUME;
			}
			HangingEntity hangingEntity = optional.get();
			net.minecraft.world.item.component.TypedEntityData<EntityType<?>> customData = itemStack.get(DataComponents.ENTITY_DATA);
			if (customData != null) {
				EntityType.updateCustomEntityTag(level, player, hangingEntity, customData);
			}
			if (hangingEntity.survives()) {
				if (!level.isClientSide()) {
					hangingEntity.playPlacementSound();
					level.gameEvent(player, GameEvent.ENTITY_PLACE, hangingEntity.position());
					level.addFreshEntity(hangingEntity);
					// [ES] Fix incorrect shrinking on clientside
					itemStack.shrink(1);
				}
				return InteractionResult.SUCCESS;
			} else {
				return InteractionResult.CONSUME;
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> list, TooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, tooltipContext, tooltipDisplay, list, tooltipFlag);
		net.minecraft.world.item.component.TypedEntityData<EntityType<?>> customData = itemStack.get(DataComponents.ENTITY_DATA);
		if (customData != null) {
			CompoundTag tag = customData.copyTagWithoutId();
			Identifier variantId = Identifier.tryParse(tag.getStringOr(ESPainting.TAG_VARIANT, ""));
			if (variantId != null) {
				list.accept(Component.translatable(variantId.toLanguageKey("painting", "title")).withStyle(ChatFormatting.YELLOW));
				list.accept(Component.translatable(variantId.toLanguageKey("painting", "author")).withStyle(ChatFormatting.GRAY));
				HolderLookup.Provider provider = tooltipContext.registries();
				if (provider != null) {
					ResourceKey<ESPaintingVariant> key = ResourceKey.create(ESRegistries.PAINTING_VARIANT, variantId);
					provider.lookup(ESRegistries.PAINTING_VARIANT).flatMap(registry -> registry.get(key)).ifPresent(holder ->
						list.accept(Component.translatable("painting.dimensions", holder.value().width(), holder.value().height())));
				}
			}
		} else if (tooltipFlag.isCreative()) {
			list.accept(TOOLTIP_RANDOM_VARIANT);
		}
	}
}
