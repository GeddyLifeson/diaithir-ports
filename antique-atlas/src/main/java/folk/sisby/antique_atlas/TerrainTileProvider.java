package folk.sisby.antique_atlas;

import folk.sisby.antique_atlas.util.MathUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record TerrainTileProvider(Identifier id, Map<TileElevation, List<TileTexture>> textures) {
	public static TerrainTileProvider DEFAULT = new TerrainTileProvider(AntiqueAtlas.id("default"), List.of(TileTexture.DEFAULT));

	public TerrainTileProvider(Identifier id, List<TileTexture> textures) {
		this(id, Arrays.stream(TileElevation.values()).collect(Collectors.toMap(e -> e, e -> textures)));
	}

	public TileTexture getTexture(ChunkPos pos, @Nullable TileElevation elevation) {
		int variation = (int) (MathUtil.hash3(pos.x(), pos.z(), pos.x() * pos.z()) & 0x7FFFFFFF);
		TileElevation usedElevation = elevation == null ? TileElevation.VALLEY : elevation;
		return textures.get(usedElevation).get(variation % textures.get(usedElevation).size());
	}
}
