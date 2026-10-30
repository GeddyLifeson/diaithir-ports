package net.diaithir.goety.magic;

import java.util.Set;
import java.util.function.Consumer;
import net.diaithir.goety.GoetyComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** The dark wand and the staves of each school. Right-click a focus onto the wand in the inventory to load it. */
public class WandItem extends Item {
    private final Set<String> schools;

    public WandItem(Properties props, String... schools) {
        super(props);
        this.schools = Set.of(schools);
    }

    public Set<String> schools() {
        return schools;
    }

    public static @Nullable SpellDef loaded(ItemStack wand) {
        Identifier id = wand.get(GoetyComponents.FOCUS.get());
        return id == null ? null : SpellDef.get(id.getPath());
    }

    // ---------------------------------------------------------------- loading foci
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess carried) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) return false;
        Identifier current = self.get(GoetyComponents.FOCUS.get());
        if (other.isEmpty()) {
            if (current == null) return false;
            carried.set(new ItemStack(BuiltInRegistries.ITEM.getValue(current)));
            self.remove(GoetyComponents.FOCUS.get());
            return true;
        }
        if (other.getItem() instanceof FocusItem f && f.spell() != null && schools.contains(f.spell().type())) {
            self.set(GoetyComponents.FOCUS.get(), BuiltInRegistries.ITEM.getKey(other.getItem()));
            other.shrink(1);
            if (current != null) carried.set(new ItemStack(BuiltInRegistries.ITEM.getValue(current)));
            return true;
        }
        return false;
    }

    // ---------------------------------------------------------------- casting
    @Override public ItemUseAnimation getUseAnimation(ItemStack s) { return ItemUseAnimation.BOW; }
    @Override public int getUseDuration(ItemStack s, LivingEntity user) { return 72000; }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        SpellDef d = loaded(stack);
        if (d == null) return InteractionResult.PASS;
        if (player.getCooldowns().isOnCooldown(stack)) return InteractionResult.FAIL;
        if (!player.isCreative() && Souls.total(player) < d.cost()) {
            if (!level.isClientSide()) player.sendOverlayMessage(Component.translatable("goety.msg.no_souls", d.cost()));
            return InteractionResult.FAIL;
        }
        if (d.cast() > 0 || d.hold()) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        if (level instanceof ServerLevel sl) fire(d, sl, player, stack);
        return InteractionResult.SUCCESS_SERVER;
    }

    private boolean fire(SpellDef d, ServerLevel level, Player player, ItemStack stack) {
        if (!player.isCreative() && Souls.total(player) < d.cost()) return false;
        if (!SpellEngine.cast(d, level, player)) return false;
        Souls.consume(player, d.cost());
        if (!d.hold()) player.getCooldowns().addCooldown(stack, Math.max(d.cooldown(), 1));
        return true;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (!(level instanceof ServerLevel sl) || !(user instanceof Player player)) return;
        SpellDef d = loaded(stack);
        if (d == null) { player.stopUsingItem(); return; }
        int elapsed = getUseDuration(stack, user) - remaining;
        if (elapsed < d.cast()) {
            if (elapsed % 5 == 0) SpellEngine.puff(sl, "enchant", player.getEyePosition().add(player.getLookAngle().scale(0.6)), 3, 0.2);
            return;
        }
        if (d.hold()) {
            if ((elapsed - d.cast()) % Math.max(d.interval(), 1) == 0 && !fire(d, sl, player, stack)) player.stopUsingItem();
        } else if (elapsed == d.cast()) {
            fire(d, sl, player, stack);
            player.stopUsingItem();
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        SpellDef d = loaded(stack);
        if (d != null && d.hold() && entity instanceof Player p) p.getCooldowns().addCooldown(stack, Math.max(d.cooldown(), 1));
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> out, TooltipFlag flag) {
        SpellDef d = loaded(stack);
        if (d == null) {
            out.accept(Component.translatable("goety.tooltip.empty_wand").withStyle(ChatFormatting.GRAY));
        } else {
            Identifier id = stack.get(GoetyComponents.FOCUS.get());
            out.accept(Component.translatable("goety.tooltip.loaded", new ItemStack(BuiltInRegistries.ITEM.getValue(id)).getHoverName()).withStyle(ChatFormatting.AQUA));
            out.accept(Component.translatable("goety.tooltip.cost", d.cost()).withStyle(ChatFormatting.DARK_AQUA));
        }
    }
}
