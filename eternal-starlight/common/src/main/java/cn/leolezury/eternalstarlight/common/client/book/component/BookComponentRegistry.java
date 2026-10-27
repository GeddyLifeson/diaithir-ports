package cn.leolezury.eternalstarlight.common.client.book.component;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BookComponentRegistry {
	private static final Map<Identifier, BookComponent<?>> COMPONENTS = new HashMap<>();
	public static final Codec<BookComponent<?>> BY_NAME_CODEC = Identifier.CODEC.xmap(id -> get(id).orElseThrow(), BookComponentRegistry::getKey);

	public static final TextBookComponent TEXT = register("text", new TextBookComponent());
	public static final DisplayBookComponent DISPLAY = register("display", new DisplayBookComponent());
	public static final IndexBookComponent INDEX = register("index", new IndexBookComponent());
	public static final MobInfoBookComponent MOB_INFO = register("mob_info", new MobInfoBookComponent());

	private static <T extends BookComponent<?>> T register(String location, T type) {
		return register(EternalStarlight.id(location), type);
	}

	public static <T extends BookComponent<?>> T register(Identifier location, T type) {
		COMPONENTS.put(location, type);
		return type;
	}

	public static Optional<BookComponent<?>> get(Identifier location) {
		return Optional.ofNullable(COMPONENTS.get(location));
	}

	public static Identifier getKey(BookComponent<?> type) {
		for (Map.Entry<Identifier, BookComponent<?>> entry : COMPONENTS.entrySet()) {
			if (entry.getValue() == type) {
				return entry.getKey();
			}
		}
		return Identifier.withDefaultNamespace("unregistered");
	}
}
