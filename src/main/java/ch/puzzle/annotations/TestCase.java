package ch.puzzle.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A testcase defines one scenario to test a {@link UseCase}, which must always
 * be referenced in the annotation. The {@link TestCase} annotation is intended
 * to be placed on JUnit Test Methods.
 * 
 * @author Brigitte Hulliger, <hulliger@puzzle.ch>
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface TestCase {

	/**
	 * corresponding UseCase class.
	 */
	Class<?> useCase();

}
