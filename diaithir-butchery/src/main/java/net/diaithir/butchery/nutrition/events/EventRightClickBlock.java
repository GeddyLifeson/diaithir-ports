package net.diaithir.butchery.nutrition.events;

import net.diaithir.butchery.nutrition.Config;
import net.diaithir.butchery.nutrition.api.INutritionalBalancePlayer;
import net.diaithir.butchery.nutrition.nutrients.Nutrient;
import net.diaithir.butchery.nutrition.nutrients.PlayerNutritionData;
import net.diaithir.butchery.nutrition.nutrients.WorldNutrients;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.List;

public class EventRightClickBlock {
    @SubscribeEvent
    public void rightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockState blockState = event.getLevel().getBlockState(event.getPos());
        Block block = blockState.getBlock();

        if (block instanceof CakeBlock) {
            Player player = event.getEntity();
            if (player instanceof ServerPlayer && player.canEat(false)) {
                INutritionalBalancePlayer iNutritionalBalancePlayer = PlayerNutritionData.getWorldNutritionData().getNutritionalBalancePlayer(player);
                Item cakeItem = block.asItem();

                List<Nutrient> nutrients = WorldNutrients.getNutrients(cakeItem, player.level());
                for (Nutrient nutrient : nutrients) {
                    float nutrientunits = 2.4f * Config.NUTRIENT_INCREMENT_RATE.get().floatValue() / nutrients.size();
                    iNutritionalBalancePlayer.getPlayerNutrientByName(nutrient.name).changeValue(nutrientunits);
                    PlayerNutritionData.getWorldNutritionData().setDirty();
                }
            }
        }
    }
}
