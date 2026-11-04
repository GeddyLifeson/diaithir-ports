package org.valkyrienskies.tournament.items

import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import org.valkyrienskies.tournament.TournamentItems
import org.valkyrienskies.tournament.registry.DeferredRegister
import java.util.function.Consumer

/** Gift bag; its contents live in the `minecraft:custom_data` component under the key `vs_tournament_gift`. */
class GiftBagItem : Item(
    DeferredRegister.itemProps().stacksTo(1)
) {
    companion object {
        private const val KEY = "vs_tournament_gift"

        private fun base(tooltip: String, rarity: Rarity, extra: CompoundTag.() -> Unit): ItemStack {
            val stack = ItemStack(TournamentItems.GIFT_BAG.get())
            val tag = CompoundTag()
            tag.putString("tooltip", tooltip)
            tag.extra()
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(CompoundTag().also { it.put(KEY, tag) }))
            stack.set(DataComponents.RARITY, rarity)
            return stack
        }

        fun of(tooltip: String, rarity: Rarity): ItemStack = base(tooltip, rarity) {}

        fun of(lootTable: String, tooltip: String, rarity: Rarity): ItemStack =
            base(tooltip, rarity) { putString("table", lootTable) }

        fun of(items: List<ItemStack>, tooltip: String, rarity: Rarity, level: Level): ItemStack =
            base(tooltip, rarity) {
                val ops = level.registryAccess().createSerializationContext(NbtOps.INSTANCE)
                val list = ListTag()
                items.forEach { item -> ItemStack.CODEC.encodeStart(ops, item).result().ifPresent { list.add(it) } }
                put("items", list)
            }

        private fun ItemStack.gift(): CompoundTag? =
            get(DataComponents.CUSTOM_DATA)?.copyTag()?.getCompound(KEY)?.orElse(null)
    }

    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResult {
        val stack = player.getItemInHand(usedHand)
        val gift = stack.gift()

        if (gift == null || !(gift.contains("items") || gift.contains("table")))
            return InteractionResult.SUCCESS

        if (level !is ServerLevel) return InteractionResult.FAIL

        gift.getList("items").ifPresent { list ->
            val ops = level.registryAccess().createSerializationContext(NbtOps.INSTANCE)
            list.forEach { elem ->
                ItemStack.CODEC.parse(ops, elem).result().ifPresent { s ->
                    if (!player.inventory.add(s)) player.drop(s, false)
                }
            }
        }

        gift.getString("table").ifPresent { name ->
            val key = ResourceKey.create(Registries.LOOT_TABLE, Identifier.parse(name))
            val ctx = LootParams.Builder(level)
                .withLuck(player.luck)
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .create(LootContextParamSets.EMPTY)
            level.server.reloadableRegistries().getLootTable(key).getRandomItems(ctx).forEach { itemStack ->
                if (!player.inventory.add(itemStack)) player.drop(itemStack, false)
            }
        }

        stack.shrink(1)
        return InteractionResult.CONSUME
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        display: TooltipDisplay,
        builder: Consumer<Component>,
        flag: TooltipFlag
    ) {
        val tooltip = stack.gift()?.getString("tooltip")?.orElse(null)
        if (tooltip == null) builder.accept(Component.translatable("tooltip.vs_tournament.gift.none"))
        else builder.accept(Component.translatable(tooltip))
    }
}
