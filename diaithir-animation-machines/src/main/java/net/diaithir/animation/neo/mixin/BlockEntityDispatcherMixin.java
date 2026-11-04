package net.diaithir.animation.neo.mixin;

import net.diaithir.animation.neo.DiaithirAnimation;
import net.diaithir.animation.neo.client.AnimatedBlockRenderer;
import net.diaithir.animation.neo.client.AnimatedBlocks;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** After renderers are built, gives every renderer-less, model-rendered block entity type with an animation rule our renderer. */
@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityDispatcherMixin {
    private static final Logger LOG = LoggerFactory.getLogger("diaithiranimation");
    @Shadow private Map<BlockEntityType<?>, BlockEntityRenderer<?, ?>> renderers;

    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void diaithiranimation$install(ResourceManager rm, CallbackInfo ci) {
        AnimatedBlocks.clear();
        Map<BlockEntityType<?>, BlockEntityRenderer<?, ?>> all = new HashMap<>(this.renderers);
        AnimatedBlockRenderer shared = new AnimatedBlockRenderer();
        int types = 0;
        for (BlockEntityType<?> type : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            if (all.containsKey(type)) continue;
            Set<Block> mine = new HashSet<>();
            boolean allModel = true;
            for (Block b : type.getValidBlocks()) {
                if (b.defaultBlockState().getRenderShape() != RenderShape.MODEL) { allModel = false; break; }
                var rule = DiaithirAnimation.rules().match(BuiltInRegistries.BLOCK.getKey(b).toString());
                if (rule.isPresent() && AnimatedBlockRenderer.supports(rule.get().spec())) mine.add(b);
            }
            // A type is taken over only when every one of its blocks is animated, so none loses its model.
            if (allModel && !mine.isEmpty() && mine.size() == type.getValidBlocks().size()) {
                all.put(type, shared);
                mine.forEach(AnimatedBlocks::add);
                types++;
            }
        }
        this.renderers = Map.copyOf(all);
        LOG.info("Animated block renderer installed for {} block entity types ({} blocks)", types, AnimatedBlocks.count());
    }
}
