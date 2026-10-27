package com.diaithir.addons.oreexcavation;

import java.util.stream.Stream;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import com.google.common.base.Stopwatch;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import com.diaithir.addons.oreexcavation.recipe.OreRecipes;
import com.diaithir.addons.oreexcavation.recipe.VeinRecipe;
import com.diaithir.addons.oreexcavation.util.RandomSpreadGenerator;

public class COECommand {
	private static final DynamicCommandExceptionType ERROR_VEIN_NOT_FOUND = new DynamicCommandExceptionType(
			o -> Component.translatable("command.coe.locate.failed", o));

	public static void init() {}

	public static void register(RegisterCommandsEvent evt) {
		LiteralArgumentBuilder<CommandSourceStack> l = Commands.literal("coe");
		l.then(Commands.literal("setvein").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).
				then(Commands.argument("pos", BlockPosArgument.blockPos()).
						then(Commands.argument("recipe", IdentifierArgument.id()).suggests(ALL_RECIPES).
								executes(c -> {
									return doSetVein(c.getSource(), BlockPosArgument.getLoadedBlockPos(c, "pos"), IdentifierArgument.getId(c, "recipe"), 0.8F);
								}).
								then(Commands.argument("multiplier", FloatArgumentType.floatArg(0, 1000)).
										executes(c -> {
											return doSetVein(c.getSource(), BlockPosArgument.getLoadedBlockPos(c, "pos"), IdentifierArgument.getId(c, "recipe"),
													FloatArgumentType.getFloat(c, "multiplier"));
										})))));
		l.then(Commands.literal("removevein").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).
				then(Commands.argument("pos", BlockPosArgument.blockPos()).
						executes(c -> {
							setVein(c.getSource(), BlockPosArgument.getLoadedBlockPos(c, "pos"), null, 0F);
							c.getSource().sendSuccess(() -> Component.translatable("command.coe.setvein.success", Component.translatable("chat.coe.veinFinder.nothing")), true);
							return 1;
						})));
		l.then(Commands.literal("locate").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).
				then(Commands.argument("recipe", IdentifierArgument.id()).suggests(ALL_RECIPES).
						executes(c -> {
							Identifier id = IdentifierArgument.getId(c, "recipe");
							if (OreRecipes.vein(c.getSource().getLevel(), id) == null)
								throw ERROR_VEIN_NOT_FOUND.create(id.toString());
							BlockPos blockpos = BlockPos.containing(c.getSource().getPosition());
							Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
							BlockPos at = OreVeinGenerator.getPicker(c.getSource().getLevel()).locate(id, blockpos, c.getSource().getLevel(), 100);
							stopwatch.stop();
							if (at == null)
								throw ERROR_VEIN_NOT_FOUND.create(id.toString());
							int i = Mth.floor(RandomSpreadGenerator.distance2d(at, blockpos));
							Component component = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", at.getX(), "~", at.getZ())).withStyle(tc -> {
								return tc.withColor(ChatFormatting.GREEN).
										withClickEvent(new ClickEvent.SuggestCommand("/tp @s " + at.getX() + " ~ " + at.getZ())).
										withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.coordinates.tooltip")));
							});
							c.getSource().sendSuccess(() -> Component.translatable("command.coe.locate.success", id.toString(), component, i), false);
							CreateOreExcavation.LOGGER.info("Locating element " + id + " took " + stopwatch.elapsed().toMillis() + " ms");
							return i;
						})));
		evt.getDispatcher().register(l);
	}

	private static int doSetVein(CommandSourceStack src, BlockPos p, Identifier id, float mul) {
		if (OreRecipes.vein(src.getLevel(), id) == null)
			return 0;
		setVein(src, p, id, mul);
		src.sendSuccess(() -> Component.translatable("command.coe.setvein.success", id.toString()), true);
		return 1;
	}

	private static void setVein(CommandSourceStack css, BlockPos pos, Identifier rl, float mul) {
		ChunkPos p = ChunkPos.containing(pos);
		var chunk = css.getLevel().getChunk(p.x(), p.z());
		OreData data = OreDataAttachment.getData(chunk);
		data.setRecipe(rl);
		data.setLoaded(true);
		data.setRandomMul(mul);
		data.setExtractedAmount(0);
		chunk.markUnsaved();
	}

	public static final SuggestionProvider<CommandSourceStack> ALL_RECIPES = (ctx, builder) -> {
		Stream<Identifier> rl = ctx.getSource().getServer() == null ? Stream.empty()
				: OreRecipes.veins(ctx.getSource().getLevel()).stream().map(OreRecipes::idOf);
		return SharedSuggestionProvider.suggestResource(rl, builder);
	};
}
