package ch.puzzle.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sample Annotation. Default for documenting Use Cases.
 * 
 * This annotation defines the annotated class as a use case in an application.
 * Use Cases have a unique ID and a name. Furthermore they can be references by
 * {@link TestCase}s. The {@link UseCase} annotation can only be placed on
 * types.
 * 
 * @author Brigitte Hulliger, <hulliger@puzzle.ch>
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface UseCase {

	String id();

}
