/**
 * 
 */
package ch.puzzle.util;

/**
 * @author Brigitte Hulliger, <hulliger@puzzle.ch>
 * 
 */
public class MissingCommandLineParameterException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8466000716445464253L;

	public MissingCommandLineParameterException(final String message) {
		super(message);
	}

}
