package dev.shadowsoffire.apothic_enchanting.mixin;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

import dev.shadowsoffire.apothic_enchanting.table.ApothEnchantingTableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * In 26.1 {@code Blocks.ENCHANTING_TABLE} is declared as
 * {@code register("enchanting_table", EnchantingTableBlock::new, properties)}.
 * The factory is a {@link Function} method reference — there is no direct {@code new EnchantingTableBlock}
 * call in {@code <clinit>} anymore — so the old {@code @Redirect(target = "NEW")} finds zero targets.
 *
 * <p>We instead intercept the {@code register(String, Function, Properties)} call and swap the factory argument,
 * scoped via {@link Slice} to the bytecode region between the {@code "enchanting_table"} and
 * {@code "brewing_stand"} string constants so we don't accidentally replace any other block's factory.
 */
@Mixin(value = Blocks.class, remap = false)
public class BlocksMixin {

    @ModifyArg(
        method = "<clinit>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Blocks;register(Ljava/lang/String;Ljava/util/function/Function;Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)Lnet/minecraft/world/level/block/Block;"),
        slice = @Slice(
            from = @At(value = "CONSTANT", args = "stringValue=enchanting_table"),
            to = @At(value = "CONSTANT", args = "stringValue=brewing_stand")),
        index = 1,
        require = 1)
    private static Function<BlockBehaviour.Properties, Block> apoth_overrideEnchTableFactory(Function<BlockBehaviour.Properties, Block> original) {
        return ApothEnchantingTableBlock::new;
    }

}
