package com.diaithir.overworld_biomes.realms;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Lights a realm portal: right-click a frame block with the activator item and a two-high portal column appears on top. */
public final class RealmPortals {
    record Rule(Identifier activator, Identifier frame, Identifier portal) {}
    private static final List<Rule> RULES = new ArrayList<>();
    private static boolean hooked;

    private RealmPortals() {}

    static void add(String modId, String activator, String frame, String portal) {
        RULES.add(new Rule(Identifier.parse(activator), Identifier.parse(frame), Identifier.fromNamespaceAndPath(modId, portal)));
        if (!hooked) {
            hooked = true;
            NeoForge.EVENT_BUS.addListener(RealmPortals::onUse);
        }
    }

    private static void onUse(PlayerInteractEvent.RightClickBlock e) {
        if (e.getHand() != InteractionHand.MAIN_HAND) return;
        Level level = e.getLevel();
        ItemStack held = e.getItemStack();
        Identifier item = BuiltInRegistries.ITEM.getKey(held.getItem());
        BlockPos clicked = e.getPos();
        Identifier frame = BuiltInRegistries.BLOCK.getKey(level.getBlockState(clicked).getBlock());
        for (Rule r : RULES) {
            if (!r.activator.equals(item) || !r.frame.equals(frame)) continue;
            BlockPos a = clicked.above();
            if (!level.getBlockState(a).isAir() || !level.getBlockState(a.above()).isAir()) return;
            Block portal = BuiltInRegistries.BLOCK.getValue(r.portal);
            BlockState s = portal.defaultBlockState();
            if (!level.isClientSide()) {
                level.setBlockAndUpdate(a, s);
                level.setBlockAndUpdate(a.above(), s);
                Player p = e.getEntity();
                if (!p.isCreative()) held.shrink(1);
            }
            e.setCanceled(true);
            return;
        }
    }
}
