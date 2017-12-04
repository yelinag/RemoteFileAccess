
public class IndexOutOfFileException extends Exception{
	/**
	 * This class is to create new kind Exception for file reading/writing errors
	 * While reading/writing the file, if operation is unsuccessful, it will return -1
	 * instead of error.
	 * Therefore, new exception type is necessary to handle that kind of error.
	 */
	public IndexOutOfFileException() { super(); }
	public IndexOutOfFileException(String message) { super(message); }
	public IndexOutOfFileException(String message, Throwable cause) { super(message, cause); }
	public IndexOutOfFileException(Throwable cause) { super(cause); }
}
