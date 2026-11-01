package net.diaithir.fauna;

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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** A fossil fragment (as dug) or a cleaned fossil; a cleaned one is studied by using it. The animal is in the stack's custom data. */
public class FossilItem extends Item {
    private final boolean cleaned;

    public FossilItem(Properties props, boolean cleaned) {
        super(props);
        this.cleaned = cleaned;
    }

    @Override
    public Component getName(ItemStack stack) {
        Species s = Fossils.speciesOf(stack);
        return s == null ? super.getName(stack)
                : Component.translatable(cleaned ? "item.diaithirfauna.cleaned_fossil.named" : "item.diaithirfauna.fossil_fragment.named", Component.translatable("species.diaithirfauna." + s.id));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, TooltipDisplay display, Consumer<Component> out, TooltipFlag flag) {
        Species s = Fossils.speciesOf(stack);
        if (s == null) return;
        out.accept(Component.literal(s.per + ", " + s.form + " Formation").withStyle(net.minecraft.ChatFormatting.GRAY));
        out.accept(Component.literal(Math.round(s.maTo) + "-" + Math.round(s.maFrom) + " million years old").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
        if (!cleaned) out.accept(Component.translatable("item.diaithirfauna.fossil_fragment.hint").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Species s = Fossils.speciesOf(stack);
        if (!cleaned || s == null) return InteractionResult.PASS;
        if (player instanceof ServerPlayer sp) {
            List<String> done = new ArrayList<>(sp.getData(Fossils.STUDIED.get()));
            boolean fresh = !done.contains(s.id);
            if (fresh) {
                done.add(s.id);
                sp.setData(Fossils.STUDIED.get(), done);
            }
            sp.giveExperiencePoints(fresh ? 10 + 5 * s.dna : 2);
            sp.sendSystemMessage(Component.translatable(fresh ? "message.diaithirfauna.studied.new" : "message.diaithirfauna.studied.known",
                    Component.translatable("species.diaithirfauna." + s.id), done.size()));
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
