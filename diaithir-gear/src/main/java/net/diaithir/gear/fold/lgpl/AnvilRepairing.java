/*
 * Anvil repair. Behaviour of "Anvil Repairing" by Darkhax (LGPL-2.1-only, MC 26.1.2 NeoForge build 26.1.2.3), rewritten on NeoForge events
 * instead of a mixin and with this module's config. This package is the LGPL part of diaithir-gear: it is kept apart from the MIT code,
 * licence text in META-INF/licenses/LICENSE-AnvilRepairing.txt, and this file may be taken out and reused under LGPL-2.1.
 */
package net.diaithir.gear.fold.lgpl;

import net.diaithir.gear.DiaithirGear;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class AnvilRepairing {
    public static final TagKey<Item> REPAIR_ITEMS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DiaithirGear.ID, "anvil_repair_items"));
    public static final Identifier ADVANCEMENT = Identifier.fromNamespaceAndPath(DiaithirGear.ID, "story/repair");

    /** Chipped becomes plain, damaged becomes chipped; null for any other block. */
    public static Block better(BlockState s) {
        if (s.is(Blocks.CHIPPED_ANVIL)) return Blocks.ANVIL;
        if (s.is(Blocks.DAMAGED_ANVIL)) return Blocks.CHIPPED_ANVIL;
        return null;
    }

    /** Try the repair with the player's main hand item. Returns SUCCESS when an item was used. */
    public static InteractionResult attempt(Player player, Level level, BlockState state, BlockPos pos, double chance) {
        if (level.isClientSide() || !(player instanceof ServerPlayer sp) || player.isSpectator()) return InteractionResult.PASS;
        Block next = better(state);
        if (next == null || !player.getMainHandItem().is(REPAIR_ITEMS)) return InteractionResult.PASS;
        if (!player.getAbilities().instabuild) player.getMainHandItem().shrink(1);
        if (level.getRandom().nextDouble() < chance) {
            level.setBlock(pos, next.defaultBlockState().setValue(AnvilBlock.FACING, state.getValue(AnvilBlock.FACING)), 2);
            level.levelEvent(1030, pos, 0);
            level.levelEvent(2012, pos, 10);
            award(sp);
        } else {
            level.levelEvent(1029, pos, 0);
            level.levelEvent(2000, pos, 1);
        }
        return InteractionResult.SUCCESS;
    }

    private static void award(ServerPlayer p) {
        AdvancementHolder h = p.level().getServer().getAdvancements().get(ADVANCEMENT);
        if (h == null) return;
        var prog = p.getAdvancements().getOrStartProgress(h);
        if (!prog.isDone()) for (String c : prog.getRemainingCriteria()) p.getAdvancements().award(h, c);
    }

    @SubscribeEvent
    public void use(PlayerInteractEvent.RightClickBlock e) {
        if (e.getHand() != InteractionHand.MAIN_HAND) return;
        Level level = e.getLevel();
        InteractionResult r = attempt(e.getEntity(), level, level.getBlockState(e.getPos()), e.getPos(), net.diaithir.gear.GearConfig.ANVIL_REPAIR_CHANCE.getAsDouble());
        if (r == InteractionResult.SUCCESS) { e.setCancellationResult(InteractionResult.SUCCESS); e.setCanceled(true); }
    }
}
