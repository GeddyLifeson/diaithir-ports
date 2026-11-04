package net.diaithir.druidry.ars.common.items;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.item.ICasterTool;
import net.diaithir.druidry.ars.api.spell.*;
import net.diaithir.druidry.ars.common.capability.IPlayerCap;
import net.diaithir.druidry.ars.setup.registry.CapabilityRegistry;
import net.diaithir.druidry.ars.setup.registry.DataComponentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class SpellBook extends ModItem implements ICasterTool {

    public SpellTier tier;

    public SpellBook(SpellTier tier) {
        this(net.diaithir.druidry.ars.setup.registry.ItemsRegistry.defaultItemProperties().stacksTo(1)
                .component(DataComponents.BASE_COLOR, DyeColor.PURPLE)
                .component(DataComponentRegistry.SPELL_CASTER, new SpellCaster(10)), tier);
    }

    public SpellBook(Properties properties, SpellTier tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level worldIn, Player playerIn, @NotNull InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
//        var caster2 = SpellCasterRegistry.from(stack);
//        caster2.getSpell().particleTimeline().debugPrintHash(caster2.getSpell(), worldIn);
//        var timeline = caster2.getSpell().particleTimeline().get(ParticleTimelineRegistry.PROJECTILE_TIMELINE.get());
//        var element = IParticleTimeline.CODEC.encodeStart(JsonOps.INSTANCE, timeline);
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        System.out.println(gson.toJson(element.result().get()));
        if (playerIn instanceof ServerPlayer) {
            if (tier != SpellTier.CREATIVE) {
                var iMana = CapabilityRegistry.getMana(playerIn);
                if (iMana != null) {
                    boolean shouldSync = false;
                    if (iMana.getBookTier() < this.tier.value) {
                        iMana.setBookTier(this.tier.value);
                        shouldSync = true;
                    }
                    IPlayerCap cap = CapabilityRegistry.getPlayerDataCap(playerIn);
                    if (iMana.getGlyphBonus() < cap.getKnownGlyphs().size()) {
                        iMana.setGlyphBonus(cap.getKnownGlyphs().size());
                        shouldSync = true;
                    }
                    if (shouldSync && playerIn instanceof ServerPlayer player) {
                        iMana.syncToClient(player);
                    }
                }
            }

            return InteractionResult.PASS;
        }

        var caster = this.getSpellCaster(stack);
        if (caster == null) {
            return InteractionResult.PASS;
        }
        caster.castOnServer(handIn, Component.translatable("ars_nouveau.invalid_spell"));

        return InteractionResult.PASS;
    }

    @Override
    public boolean doesSneakBypassUse(@NotNull ItemStack stack, @NotNull LevelReader world, @NotNull BlockPos pos, @NotNull Player player) {
        return true;
    }

    @Override
    public void appendHoverText(final @NotNull ItemStack stack, final @NotNull TooltipContext world, final @NotNull TooltipDisplay display, final @NotNull Consumer<Component> tooltip, final @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, world, display, tooltip, flag);
        // Port note: key-binding hints removed (client key mappings not ported).
        tooltip.accept(Component.translatable("tooltip.ars_nouveau.caster_level", getTier().value).setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)));
    }

    public SpellTier getTier() {
        return this.tier;
    }

    @Override
    public boolean canQuickCast() {
        return true;
    }
}
