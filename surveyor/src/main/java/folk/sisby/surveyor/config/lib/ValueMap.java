package folk.sisby.surveyor.config.lib;

import java.util.LinkedHashMap;
import java.util.Map;

/** Ordered string-keyed map builder, replacing Kaleido's ValueMap. */
public final class ValueMap {
	private ValueMap() {
	}

	public static <V> Builder<V> builder(V defaultValue) {
		return new Builder<>();
	}

	public static final class Builder<V> {
		private final Map<String, V> map = new LinkedHashMap<>();

		public Builder<V> put(String key, V value) {
			map.put(key, value);
			return this;
		}

		public Map<String, V> build() {
			return map;
		}
	}
}
