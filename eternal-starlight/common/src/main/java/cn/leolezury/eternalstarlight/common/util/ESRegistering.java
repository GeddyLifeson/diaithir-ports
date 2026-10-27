package cn.leolezury.eternalstarlight.common.util;

import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

/**
 * PORT: 26.1 requires Block/Item Properties to know their registry id when the object is constructed.
 * The 1.21.1 call sites build Properties inside register suppliers without an id, so the registration
 * provider publishes the id being registered here and the Properties mixins read it.
 */
public class ESRegistering {
	private static final ThreadLocal<ResourceKey<?>> CURRENT = new ThreadLocal<>();

	public static <T> Supplier<T> wrap(ResourceKey<?> key, Supplier<? extends T> supplier) {
		return () -> {
			ResourceKey<?> previous = CURRENT.get();
			CURRENT.set(key);
			try {
				return supplier.get();
			} finally {
				if (previous == null) {
					CURRENT.remove();
				} else {
					CURRENT.set(previous);
				}
			}
		};
	}

	@SuppressWarnings("unchecked")
	public static <T> ResourceKey<T> current() {
		return (ResourceKey<T>) CURRENT.get();
	}
}
