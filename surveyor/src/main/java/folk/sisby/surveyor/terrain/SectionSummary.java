package folk.sisby.surveyor.terrain;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerFactory;

public record SectionSummary(Palette<BlockState> blockPalette, int[] blockIndices, Palette<Holder<Biome>> biomePalette, int[] biomeIndices) {
	public static SectionSummary ofSection(PalettedContainerFactory factory, LevelChunkSection section) {
		if (section.hasOnlyAir()) {
			return null;
		} else {
			int[] blockIndices = new int[factory.blockStatesStrategy().entryCount()];
			section.getStates().data.storage().unpack(blockIndices);
			int[] biomeIndices = new int[factory.biomeStrategy().entryCount()];
			((PalettedContainer<Holder<Biome>>) section.getBiomes()).data.storage().unpack(biomeIndices);
			return new SectionSummary(
				section.getStates().data.palette(),
				blockIndices,
				((PalettedContainer<Holder<Biome>>) section.getBiomes()).data.palette(),
				biomeIndices
			);
		}
	}

	public BlockState getBlockState(PalettedContainerFactory factory, int relativeX, int y, int relativeZ) {
		return blockPalette().valueFor(blockIndices()[factory.blockStatesStrategy().getIndex(relativeX, y & 15, relativeZ)]);
	}

	public Holder<Biome> getBiomeEntry(PalettedContainerFactory factory, int relativeX, int y, int relativeZ, int bottomY, int topY) {
		return biomePalette().valueFor(biomeIndices()[factory.biomeStrategy().getIndex(QuartPos.fromBlock(relativeX) & 3, Mth.clamp(QuartPos.fromBlock(y), QuartPos.fromBlock(bottomY), QuartPos.fromBlock(topY) - 1) & 3, QuartPos.fromBlock(relativeZ) & 3)]);
	}
}
