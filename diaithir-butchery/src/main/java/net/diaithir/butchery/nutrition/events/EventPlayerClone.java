package net.diaithir.butchery.nutrition.events;

import net.diaithir.butchery.nutrition.Config;
import net.diaithir.butchery.nutrition.api.INutritionalBalancePlayer;
import net.diaithir.butchery.nutrition.api.IPlayerNutrient;
import net.diaithir.butchery.nutrition.nutrients.PlayerNutritionData;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class EventPlayerClone {
    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        Player playerEntity = event.getEntity();
        if (playerEntity != null && event.isWasDeath()) {
            INutritionalBalancePlayer iNutritionalBalancePlayer = PlayerNutritionData.getWorldNutritionData().getNutritionalBalancePlayer(playerEntity);
            for (IPlayerNutrient playerNutrient : iNutritionalBalancePlayer.getPlayerNutrients()) {
                float newNutrientValue = Math.max(Config.NUTRIENT_INITIAL.get().floatValue(), playerNutrient.getValue() - Config.NUTRIENT_DEATH_LOSS.get().floatValue());
                iNutritionalBalancePlayer.getPlayerNutrientByName(playerNutrient.getNutrientName()).setValue(newNutrientValue);
            }
            PlayerNutritionData.getWorldNutritionData().setDirty();
        }
    }
}
