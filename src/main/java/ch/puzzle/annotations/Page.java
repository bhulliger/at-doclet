package ch.puzzle.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sample Annotation. Default for documenting Web Pages.
 * 
 * This annotation defines the annotated class as a page in an application.
 * Pages have a name and an ID and can be called by a request. The annotation
 * can only be placed on types, not methods.
 * 
 * @author Brigitte Hulliger, <hulliger@puzzle.ch>
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Page {

	/**
	 * @return Identifier of the page
	 */
	String id();

}
