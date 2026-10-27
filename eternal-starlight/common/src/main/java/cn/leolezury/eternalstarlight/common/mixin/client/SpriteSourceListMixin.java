package cn.leolezury.eternalstarlight.common.mixin.client;

import cn.leolezury.eternalstarlight.common.data.ESTrimMaterials;
import cn.leolezury.eternalstarlight.common.data.ESTrimPatterns;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceList;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(SpriteSourceList.class)
public abstract class SpriteSourceListMixin {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void init(List<SpriteSource> list, CallbackInfo ci) {
		for (SpriteSource source : list) {
			if (source instanceof PalettedPermutationsAccessor permutations && permutations.getPaletteKey().getPath().equals("trims/color_palettes/trim_palette")) {
				List<Identifier> textures = new ArrayList<>(permutations.getTextures());
				for (ResourceKey<TrimPattern> key : ESTrimPatterns.TRIM_PATTERNS) {
					textures.add(key.location().withPrefix("trims/models/armor/"));
					textures.add(key.location().withPrefix("trims/models/armor/").withSuffix("_leggings"));
				}
				permutations.setTextures(textures);
				Map<String, Identifier> map = new HashMap<>(permutations.getPermutations());
				for (ResourceKey<TrimMaterial> key : ESTrimMaterials.TRIM_MATERIALS) {
					map.put(key.location().getPath(), key.location().withPrefix("trims/color_palettes/"));
				}
				permutations.setPermutations(map);
			}
		}
	}

	@Mixin(PalettedPermutations.class)
	private interface PalettedPermutationsAccessor {
		@Accessor
		List<Identifier> getTextures();

		@Accessor("textures")
		void setTextures(List<Identifier> value);

		@Accessor
		Map<String, Identifier> getPermutations();

		@Accessor("permutations")
		void setPermutations(Map<String, Identifier> value);

		@Accessor
		Identifier getPaletteKey();
	}
}
