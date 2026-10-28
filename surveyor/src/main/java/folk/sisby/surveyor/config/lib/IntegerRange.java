package folk.sisby.surveyor.config.lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Clamps an int config value. Replaces the Kaleido/Quilt Config annotation of the same name. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IntegerRange {
	long min();

	long max();
}
