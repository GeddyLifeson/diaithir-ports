package rearth.belts;

import rearth.belts.blocks.ChuteBlock;
import rearth.belts.blocks.ConveyorSupportBlock;
import rearth.belts.foundry.FoundryLoaderBlock;
import rearth.belts.foundry.FoundrySplitterBlock;
import rearth.belts.foundry.FoundryTunnelBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockContent {
    
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Belts.MOD_ID, Registries.BLOCK);
    
    public static final RegistrySupplier<Block> CHUTE_BLOCK = BLOCKS.register("chute", () -> new ChuteBlock(
      BlockBehaviour.Properties.ofLegacyCopy(Blocks.GLASS).sound(SoundType.POINTED_DRIPSTONE).noOcclusion().setId(ResourceKey.create(Registries.BLOCK, Belts.id("chute")))));
    public static final RegistrySupplier<Block> CONVEYOR_MODEL = BLOCKS.register("conveyor_model", () -> new Block(
      BlockBehaviour.Properties.ofLegacyCopy(Blocks.GLASS).noOcclusion().setId(ResourceKey.create(Registries.BLOCK, Belts.id("conveyor_model")))));
    public static final RegistrySupplier<Block> CONVEYOR_SUPPORT_BLOCK = BLOCKS.register("conveyor_support", () -> new ConveyorSupportBlock(
      BlockBehaviour.Properties.ofLegacyCopy(Blocks.GLASS).sound(SoundType.POINTED_DRIPSTONE).noOcclusion().setId(ResourceKey.create(Registries.BLOCK, Belts.id("conveyor_support")))));
    
    public static final RegistrySupplier<Block> FOUNDRY_SPLITTER_BLOCK = BLOCKS.register("foundry_splitter", () -> new FoundrySplitterBlock(foundryProperties("foundry_splitter")));
    public static final RegistrySupplier<Block> FOUNDRY_TUNNEL_BLOCK = BLOCKS.register("foundry_tunnel", () -> new FoundryTunnelBlock(foundryProperties("foundry_tunnel")));
    public static final RegistrySupplier<Block> FOUNDRY_LOADER_BLOCK = BLOCKS.register("foundry_loader", () -> new FoundryLoaderBlock(foundryProperties("foundry_loader")));
    
    private static BlockBehaviour.Properties foundryProperties(String path) {
        return BlockBehaviour.Properties.ofLegacyCopy(Blocks.GLASS).sound(SoundType.METAL).strength(2f).setId(ResourceKey.create(Registries.BLOCK, Belts.id(path)));
    }
    
}
