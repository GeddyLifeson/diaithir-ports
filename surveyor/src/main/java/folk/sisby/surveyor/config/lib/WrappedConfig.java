package folk.sisby.surveyor.config.lib;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Small reflection-based TOML config, written for the NeoForge port to replace Kaleido Config.
 * Public instance fields are values; fields whose type implements {@link Section} become tables.
 * Supported value types: boolean, int, long, double, String, enums, List of String, Map of String to Boolean/Integer/String.
 */
public abstract class WrappedConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger("surveyor-config");

	public interface Section {
	}

	private transient Path path;

	public static <T extends WrappedConfig> T createToml(Path configDir, String family, String id, Class<T> clazz) {
		T config;
		try {
			config = clazz.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Cannot create config " + id, e);
		}
		((WrappedConfig) config).path = (family.isEmpty() ? configDir : configDir.resolve(family)).resolve(id + ".toml");
		((WrappedConfig) config).load();
		((WrappedConfig) config).save();
		return config;
	}

	public void load() {
		if (path == null || !Files.exists(path)) return;
		try {
			CommentedConfig parsed = TomlFormat.instance().createParser().parse(path, (p, f) -> false);
			read(parsed, this);
		} catch (Exception e) {
			LOGGER.error("Could not read config {}, using defaults", path, e);
		}
	}

	public void save() {
		if (path == null) return;
		try {
			Files.createDirectories(path.getParent());
			CommentedConfig out = TomlFormat.newConfig(LinkedHashMap::new);
			write(out, this);
			TomlFormat.instance().createWriter().write(out, path, WritingMode.REPLACE);
		} catch (Exception e) {
			LOGGER.error("Could not write config {}", path, e);
		}
	}

	private static List<Field> fields(Object o) {
		List<Field> list = new ArrayList<>();
		for (Field f : o.getClass().getFields()) {
			int m = f.getModifiers();
			if (Modifier.isStatic(m) || Modifier.isTransient(m)) continue;
			list.add(f);
		}
		return list;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void read(UnmodifiableConfig in, Object target) throws IllegalAccessException {
		for (Field f : fields(target)) {
			List<String> key = List.of(f.getName());
			if (!in.contains(key)) continue;
			Object raw = in.get(key);
			Class<?> type = f.getType();
			Object current = f.get(target);
			if (current instanceof Section && raw instanceof UnmodifiableConfig sub) {
				read(sub, current);
			} else if (type == boolean.class && raw instanceof Boolean b) {
				f.setBoolean(target, b);
			} else if (type == int.class && raw instanceof Number n) {
				long v = n.longValue();
				IntegerRange range = f.getAnnotation(IntegerRange.class);
				if (range != null) v = Math.max(range.min(), Math.min(range.max(), v));
				f.setInt(target, (int) v);
			} else if (type == long.class && raw instanceof Number n) {
				f.setLong(target, n.longValue());
			} else if (type == double.class && raw instanceof Number n) {
				f.setDouble(target, n.doubleValue());
			} else if (type == String.class && raw != null) {
				f.set(target, raw.toString());
			} else if (type.isEnum() && raw != null) {
				try {
					f.set(target, Enum.valueOf((Class) type, raw.toString().toUpperCase()));
				} catch (IllegalArgumentException e) {
					LOGGER.warn("Unknown value {} for config key {}", raw, f.getName());
				}
			} else if (List.class.isAssignableFrom(type) && raw instanceof List<?> l) {
				List<Object> list = (List<Object>) current;
				list.clear();
				for (Object o : l) list.add(o.toString());
			} else if (Map.class.isAssignableFrom(type) && raw instanceof UnmodifiableConfig sub) {
				Map<String, Object> map = (Map<String, Object>) current;
				Class<?> valueType = mapValueType(f);
				for (UnmodifiableConfig.Entry e : sub.entrySet()) {
					Object v = e.getValue();
					if (valueType == Boolean.class && v instanceof Boolean) map.put(e.getKey(), v);
					else if (valueType == Integer.class && v instanceof Number n) map.put(e.getKey(), n.intValue());
					else if (valueType == String.class && v != null) map.put(e.getKey(), v.toString());
				}
			}
		}
	}

	private static Class<?> mapValueType(Field f) {
		Type t = f.getGenericType();
		if (t instanceof ParameterizedType p && p.getActualTypeArguments().length == 2 && p.getActualTypeArguments()[1] instanceof Class<?> c) return c;
		return Object.class;
	}

	@SuppressWarnings("unchecked")
	private static void write(CommentedConfig out, Object source) throws IllegalAccessException {
		for (Field f : fields(source)) {
			List<String> key = List.of(f.getName());
			Object value = f.get(source);
			if (value instanceof Section) {
				CommentedConfig sub = out.createSubConfig();
				write(sub, value);
				out.set(key, sub);
			} else if (value instanceof Map<?, ?> map) {
				CommentedConfig sub = out.createSubConfig();
				for (Map.Entry<?, ?> e : map.entrySet()) sub.set(List.of(e.getKey().toString()), e.getValue());
				out.set(key, sub);
			} else if (value instanceof Enum<?> e) {
				out.set(key, e.name());
			} else if (value instanceof List<?> l) {
				out.set(key, new ArrayList<>(l));
			} else {
				out.set(key, value);
			}
			Comment[] comments = f.getAnnotationsByType(Comment.class);
			StringBuilder sb = new StringBuilder();
			for (Comment c : comments) {
				if (!sb.isEmpty()) sb.append('\n');
				sb.append(' ').append(c.value());
			}
			if (f.getType().isEnum()) {
				if (!sb.isEmpty()) sb.append('\n');
				sb.append(" Options: ");
				Object[] constants = f.getType().getEnumConstants();
				for (int i = 0; i < constants.length; i++) sb.append(i > 0 ? ", " : "").append(((Enum<?>) constants[i]).name());
			}
			if (!sb.isEmpty()) out.setComment(key, sb.toString());
		}
	}
}
