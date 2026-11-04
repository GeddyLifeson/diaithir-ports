package net.diaithir.crashtools.testbot;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Clean-room test-bot lane. A FakePlayer (no network connection, no tick load) that scripts and
 * in-game tests can drive: /diaithirbot hold|use|break|status. Protocol-level join tests are done
 * externally (Mineflayer) and are out of scope for the mod. Operator-only.
 */
public final class Feature {
    private static final GameProfile PROFILE = new GameProfile(UUID.fromString("d1a17415-0000-4000-8000-00000000b07b"), "[DiaithirBot]");

    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> {
            var root = Commands.literal("diaithirbot")
                    .requires(s -> s.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));
            root.then(Commands.literal("status").executes(c -> status(c)));
            root.then(Commands.literal("hold").then(Commands.argument("item", ItemArgument.item(e.getBuildContext()))
                    .executes(c -> {
                        FakePlayer bot = bot(c);
                        ItemStack stack = ItemArgument.getItem(c, "item").createItemStack(1);
                        bot.setItemInHand(InteractionHand.MAIN_HAND, stack);
                        return say(c, "Bot holds " + stack.getHoverName().getString());
                    })));
            root.then(Commands.literal("use").then(Commands.argument("pos", BlockPosArgument.blockPos())
                    .then(Commands.argument("face", StringArgumentType.word()).executes(c -> use(c)))));
            root.then(Commands.literal("break").then(Commands.argument("pos", BlockPosArgument.blockPos())
                    .executes(c -> breakBlock(c))));
            e.getDispatcher().register(root);
        });
    }

    private static FakePlayer bot(CommandContext<CommandSourceStack> c) {
        ServerLevel level = c.getSource().getLevel();
        FakePlayer bot = FakePlayerFactory.get(level, PROFILE);
        bot.teleportTo(c.getSource().getPosition().x, c.getSource().getPosition().y, c.getSource().getPosition().z);
        return bot;
    }

    private static int say(CommandContext<CommandSourceStack> c, String msg) {
        c.getSource().sendSuccess(() -> Component.literal(msg), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int status(CommandContext<CommandSourceStack> c) {
        FakePlayer bot = bot(c);
        return say(c, "Bot at " + bot.blockPosition().toShortString() + " holding " + bot.getMainHandItem().getHoverName().getString());
    }

    private static int use(CommandContext<CommandSourceStack> c) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        FakePlayer bot = bot(c);
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(c, "pos");
        Direction face = Direction.byName(StringArgumentType.getString(c, "face"));
        if (face == null) face = Direction.UP;
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), face, pos, false);
        InteractionResult r = bot.gameMode.useItemOn(bot, bot.level(), bot.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
        return say(c, "useItemOn " + pos.toShortString() + " -> " + r);
    }

    private static int breakBlock(CommandContext<CommandSourceStack> c) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        FakePlayer bot = bot(c);
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(c, "pos");
        boolean ok = bot.gameMode.destroyBlock(pos);
        return say(c, "destroyBlock " + pos.toShortString() + " -> " + ok);
    }
}
