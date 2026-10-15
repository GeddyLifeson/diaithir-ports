package sonar.fluxnetworks.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import sonar.fluxnetworks.api.FluxConstants;
import sonar.fluxnetworks.api.FluxDataComponents;
import sonar.fluxnetworks.client.gui.basic.GuiFluxCore;
import sonar.fluxnetworks.common.data.FluxDeviceConfigComponent;
import sonar.fluxnetworks.common.device.TileFluxDevice;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Render network color on blocks and items: tint layer 1 is the network color.
 */
public final class FluxColorHandler {

    public static final List<BlockTintSource> BLOCK_TINTS = List.of(BlockTintSources.constant(-1), new BlockTintSource() {
        @Override
        public int color(BlockState state) {
            return -1;
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            return level.getBlockEntity(pos) instanceof TileFluxDevice device ? ARGB.opaque(device.mClientColor) : -1;
        }
    });

    /** Item model tint {@code fluxnetworks:network_color}. */
    public record NetworkTint() implements ItemTintSource {
        public static final MapCodec<NetworkTint> CODEC = MapCodec.unit(new NetworkTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            return ARGB.opaque(getColorForItem(stack));
        }

        @Override
        public MapCodec<NetworkTint> type() {
            return CODEC;
        }
    }

    private FluxColorHandler() {
    }

    public static int getColorForItem(ItemStack stack) {
        // 1. Check if the color is defined in the stack
        Optional<Integer> forcedColor = stack.get(FluxDataComponents.FLUX_COLOR);
        if (forcedColor != null && forcedColor.isPresent()) {
            return forcedColor.get();
        }
        // 2. If the item has a saved configuration, use network color
        FluxDeviceConfigComponent data = stack.get(FluxDataComponents.FLUX_CONFIG);
        if (data != null) {
            return ClientCache.getNetwork(data.networkId()).getNetworkColor();
        }
        // 3. Try grabbing color from the current screen
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof GuiFluxCore gui) {
            return gui.getNetwork().getNetworkColor();
        }
        // 4. If all else fails, use the default color
        return FluxConstants.INVALID_NETWORK_COLOR;
    }
}