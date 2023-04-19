package err;

/**
 * The class {@code NonEmptyFileException} represents an exception that is thrown when a file is not empty.
 * @author Fernando Campos Silva Dal Maria & Bruno Santiago de Oliveira
 * @version 1.0.0
 * 
 * @see {@link java.lang.RuntimeException}
 */
public class NonEmptyFileException extends RuntimeException {
    
    /**
     * Constructs a new {@code NonEmptyFileException} with no message.
     */
    public NonEmptyFileException() {
        super();
    }

    /**
     * Constructs a new {@code NonEmptyFileException} with the specified message.
     *
     * @param s the detail message.
     */
    public NonEmptyFileException(String s) {
        super(s);
    }

}
