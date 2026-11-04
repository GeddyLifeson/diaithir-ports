package net.diaithir.butchery.nutrition.commands;

import net.diaithir.butchery.nutrition.api.INutritionalBalancePlayer;
import net.diaithir.butchery.nutrition.network.ModNetworkHandler;
import net.diaithir.butchery.nutrition.network.PlayerSync;
import net.diaithir.butchery.nutrition.nutrients.PlayerNutritionData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class CommandSyncClient implements Command<CommandSourceStack> {
    private static final CommandSyncClient CMD = new CommandSyncClient();

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (context.getSource().getEntity() instanceof Player player) {
            INutritionalBalancePlayer iNutritionalBalancePlayer = PlayerNutritionData.getWorldNutritionData().getNutritionalBalancePlayer(player);
            PlayerSync playerSync = new PlayerSync(iNutritionalBalancePlayer);
            ModNetworkHandler.sendToClient(playerSync, context.getSource().getPlayerOrException());
        }
        return 0;
    }

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("sync").executes(CMD);
    }
}
