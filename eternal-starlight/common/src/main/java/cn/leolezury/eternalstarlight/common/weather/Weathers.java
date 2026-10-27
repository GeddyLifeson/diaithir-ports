package cn.leolezury.eternalstarlight.common.weather;

import cn.leolezury.eternalstarlight.common.registry.ESWeathers;
import cn.leolezury.eternalstarlight.common.EternalStarlight;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.saveddata.SavedDataType;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Weathers extends SavedData {
	public static final Codec<Weathers> CODEC = Codec.unboundedMap(Codec.STRING, CompoundTag.CODEC).xmap(Weathers::fromData, Weathers::toData);
	// PORT: the dimension data type has no level context in the vanilla API, so the level is bound lazily via bind()
	public static final SavedDataType<Weathers> TYPE = new SavedDataType<>(EternalStarlight.id("weathers"), Weathers::new, CODEC, null);

	private ServerLevel serverLevel;
	private Map<String, CompoundTag> pending = new HashMap<>();
	private final List<WeatherInstance> weathers = new ArrayList<>();

	public Weathers() {
	}

	public Weathers bind(ServerLevel serverLevel) {
		if (this.serverLevel == null) {
			this.serverLevel = serverLevel;
			ESWeathers.WEATHERS.registry().forEach(weather -> {
				WeatherInstance instance = new WeatherInstance(serverLevel, weather);
				String id = Objects.requireNonNull(ESWeathers.WEATHERS.registry().getKey(weather)).toString();
				CompoundTag saved = pending.get(id);
				if (saved != null) {
					instance.load(saved);
				}
				weathers.add(instance);
			});
			pending = new HashMap<>();
		}
		return this;
	}

	private static Weathers fromData(Map<String, CompoundTag> data) {
		Weathers weathers = new Weathers();
		weathers.pending = new HashMap<>(data);
		return weathers;
	}

	private Map<String, CompoundTag> toData() {
		Map<String, CompoundTag> data = new HashMap<>(pending);
		for (WeatherInstance instance : weathers) {
			String id = Objects.requireNonNull(ESWeathers.WEATHERS.registry().getKey(instance.getWeather())).toString();
			CompoundTag weatherTag = new CompoundTag();
			instance.save(weatherTag);
			data.put(id, weatherTag);
		}
		return data;
	}

	public List<WeatherInstance> getWeathers() {
		return weathers;
	}

	public Optional<WeatherInstance> getActiveWeather() {
		for (WeatherInstance instance : getWeathers()) {
			if (instance.active) {
				return Optional.of(instance);
			}
		}
		return Optional.empty();
	}

	public void setActiveWeather(AbstractWeather weather, int duration) {
		for (WeatherInstance instance : getWeathers()) {
			if (!instance.active && instance.getWeather() == weather) {
				instance.start();
				instance.currentDuration = duration;
			}
			if (instance.active && !(instance.getWeather() == weather)) {
				instance.stop();
			}
		}
		setDirty();
	}

	public void clearAllWeathers(int duration) {
		for (WeatherInstance instance : getWeathers()) {
			instance.stop();
			instance.ticksUntilNext = duration;
		}
		setDirty();
	}

	public void tick() {
		List<WeatherInstance> canStartWeathers = new ArrayList<>();
		boolean hasActive = false;
		for (WeatherInstance instance : weathers) {
			if (instance.tick()) {
				canStartWeathers.add(instance);
			}
			hasActive = hasActive || instance.active;
		}
		if (!hasActive && !canStartWeathers.isEmpty()) {
			canStartWeathers.get(serverLevel.getRandom().nextInt(canStartWeathers.size())).start();
		}
		setDirty();
	}
}
