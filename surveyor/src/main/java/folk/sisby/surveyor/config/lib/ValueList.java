package folk.sisby.surveyor.config.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Mutable list factory, replacing Kaleido's ValueList. The first argument is the element default, as in Kaleido. */
public final class ValueList {
	private ValueList() {
	}

	@SafeVarargs
	public static <T> List<T> create(T defaultValue, T... values) {
		return new ArrayList<>(Arrays.asList(values));
	}
}
