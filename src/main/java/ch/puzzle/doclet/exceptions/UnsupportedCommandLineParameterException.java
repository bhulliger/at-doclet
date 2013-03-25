package ch.puzzle.doclet.exceptions;

/**
 * @author Brigitte Hulliger, <hulliger@puzzle.ch>
 * 
 */
public class UnsupportedCommandLineParameterException extends Exception {

	/** */
	private static final long serialVersionUID = -8693071757333532305L;

	/**
	 * @param message
	 *            the exception message
	 */
	public UnsupportedCommandLineParameterException(final String message) {
		super(message);
	}

}
