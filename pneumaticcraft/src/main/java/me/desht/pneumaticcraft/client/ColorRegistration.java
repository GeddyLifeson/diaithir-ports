package me.desht.pneumaticcraft.client;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.block.AbstractCamouflageBlock;
import me.desht.pneumaticcraft.common.block.entity.CamouflageableBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 26.1 tint registration: item tints are declared in item JSON with the "pneumaticcraft:tint" source, which asks the
 * item (or block item) for its colour; block tints are registered per block as indexed sources.
 */
@EventBusSubscriber(modid = Names.MOD_ID, value = Dist.CLIENT)
public class ColorRegistration {
    private static final int MAX_TINTS = 4;

    public record PncItemTint(int index) implements ItemTintSource {
        public static final MapCodec<PncItemTint> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                com.mojang.serialization.Codec.INT.fieldOf("index").forGetter(PncItemTint::index)
        ).apply(i, PncItemTint::new));

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            int c = 0xFFFFFFFF;
            if (stack.getItem() instanceof ColorHandlers.ITintableItem t) {
                c = t.getTintColor(stack, index);
            } else if (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ColorHandlers.ITintableBlock tb) {
                c = tb.getTintColor(bi.getBlock().defaultBlockState(), null, null, index);
            }
            return 0xFF000000 | c;
        }

        @Override
        public MapCodec<PncItemTint> type() {
            return MAP_CODEC;
        }
    }

    @SubscribeEvent
    public static void registerItemTints(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(Identifier.fromNamespaceAndPath(Names.MOD_ID, "tint"), PncItemTint.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerBlockTints(RegisterColorHandlersEvent.BlockTintSources event) {
        for (var entry : ModBlocks.BLOCKS.getEntries()) {
            Block block = entry.get();
            if (block instanceof ColorHandlers.ITintableBlock tintable) {
                List<BlockTintSource> list = new ArrayList<>();
                for (int i = 0; i < MAX_TINTS; i++) {
                    final int idx = i;
                    list.add(new BlockTintSource() {
                        @Override
                        public int color(BlockState state) {
                            return 0xFF000000 | tintable.getTintColor(state, null, null, idx);
                        }

                        @Override
                        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                            return 0xFF000000 | tintable.getTintColor(state, level, pos, idx);
                        }
                    });
                }
                event.register(list, block);
            }
        }
    }
}
