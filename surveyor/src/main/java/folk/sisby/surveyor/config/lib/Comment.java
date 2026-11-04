package folk.sisby.surveyor.config.lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** A line of comment written above a config value. Replaces the Kaleido/Quilt Config annotation of the same name. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(Comment.List.class)
public @interface Comment {
	String value();

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface List {
		Comment[] value();
	}
}
