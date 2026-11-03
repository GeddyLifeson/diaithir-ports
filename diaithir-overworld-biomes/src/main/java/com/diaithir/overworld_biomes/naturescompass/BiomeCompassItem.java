package com.diaithir.overworld_biomes.naturescompass;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.Consumer;

public class BiomeCompassItem extends Item {
    public BiomeCompassItem(Properties p) { super(p); }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            net.neoforged.fml.loading.FMLEnvironment.getDist();
            ClientHooks.openScreen(hand);
        } else if (player instanceof ServerPlayer sp) {
            PacketDistributor.sendToPlayer(sp, new CompassNetwork.OpenList(CompassSearch.biomeList(sp), hand == InteractionHand.MAIN_HAND));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, TooltipDisplay display, Consumer<Component> out, TooltipFlag flag) {
        var id = stack.get(BiomeCompassMod.TARGET_BIOME.get());
        if (id != null) {
            out.accept(Component.translatable("tooltip.diaithir_overworld_biomes.target",
                    Component.translatable("biome." + id.getNamespace() + "." + id.getPath())));
        }
        var pos = stack.get(BiomeCompassMod.LOCATED_POS.get());
        if (pos != null) {
            out.accept(Component.translatable("tooltip.diaithir_overworld_biomes.located",
                    pos.pos().getX(), pos.pos().getZ()));
        }
    }
}
