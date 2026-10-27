package cn.leolezury.eternalstarlight.common.posteffect;

import cn.leolezury.eternalstarlight.common.registry.ESPostEffects;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class PostEffectType<T extends PostEffectData> {
	public static final int MAX_EFFECTS = 64;

	public static final String UNIFORM_POSITIONS = "uEffectPositions";
	public static final String UNIFORM_RADII = "uEffectRadii";
	public static final String UNIFORM_INTENSITIES = "uEffectIntensities";
	public static final String UNIFORM_AGES = "uEffectAges";
	public static final String UNIFORM_DURATIONS = "uEffectDurations";
	public static final String UNIFORM_COUNT = "uEffectCount";

	private static final float[] POSITIONS = new float[MAX_EFFECTS * 3];
	private static final float[] RADII = new float[MAX_EFFECTS];
	private static final float[] INTENSITIES = new float[MAX_EFFECTS];
	private static final float[] AGES = new float[MAX_EFFECTS];
	private static final float[] DURATIONS = new float[MAX_EFFECTS];

	private final MapCodec<T> codec;
	private final StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;
	private final Identifier postChainLocation;

	protected PostEffectType(Identifier postChainLocation, MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		this.postChainLocation = postChainLocation;
		this.codec = codec;
		this.streamCodec = streamCodec;
	}

	public Identifier id() {
		return ESPostEffects.POST_EFFECTS.registry().getKey(this);
	}

	public MapCodec<T> codec() {
		return codec;
	}

	public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
		return streamCodec;
	}

	public Identifier postChainLocation() {
		return postChainLocation;
	}





	@SuppressWarnings("unchecked")
	protected T getData(PostEffectInstance instance) {
		return (T) instance.getData();
	}
}
