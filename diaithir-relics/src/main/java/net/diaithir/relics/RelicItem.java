package net.diaithir.relics;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/** A relic: the item itself holds the numbers (components); the active power fires from use(), cooldown from the item's use_cooldown. */
public class RelicItem extends Item {
    private final DiaithirRelics.Def def;

    public RelicItem(Properties props, DiaithirRelics.Def def) {
        super(props);
        this.def = def;
    }

    public DiaithirRelics.Def def() { return def; }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        // spears keep the vanilla charge on a plain use; their power fires when sneaking
        if (def.active() != null && (!def.kind().equals("spear") || player.isShiftKeyDown())) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            ItemStack stack = player.getItemInHand(hand);
            if (player instanceof ServerPlayer sp && Abilities.fire(def, sp, stack)) {
                player.getCooldowns().addCooldown(stack, (int) (def.cooldown() * 20));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> out, TooltipFlag flag) {
        String k = "item." + DiaithirRelics.ID + "." + def.id();
        if (net.minecraft.locale.Language.getInstance().has(k + ".lore")) {
            out.accept(Component.translatable(k + ".lore").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
        }
        out.accept(Component.translatable(k + ".age").withStyle(ChatFormatting.GOLD));
        if (def.active() != null) {
            out.accept(Component.translatable("tooltip.diaithirrelics.active").append(": ").withStyle(ChatFormatting.AQUA)
                .append(Component.translatable(k + ".active").withStyle(ChatFormatting.WHITE)));
        }
        out.accept(Component.translatable("tooltip.diaithirrelics.passive").append(": ").withStyle(ChatFormatting.GREEN)
            .append(Component.translatable(k + ".passive").withStyle(ChatFormatting.WHITE)));
        if (def.trinket()) {
            out.accept(Component.translatable("tooltip.diaithirrelics.worn", def.slot()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
