/**
 * 
 */
package ch.puzzle.doclet.exceptions;

/**
 * @author Brigitte Hulliger, <hulliger@puzzle.ch>
 * 
 */
public class MissingCommandLineParameterException extends Exception {

	private static final long serialVersionUID = -8466000716445464253L;

	/**
	 * @param message
	 *            the exception message.
	 */
	public MissingCommandLineParameterException(final String message) {
		super(message);
	}

}
