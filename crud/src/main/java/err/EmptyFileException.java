package err;

/**
 * The class {@code EmptyFileException} represents an exception that is thrown when a file is empty.
 * @author Fernando Campos Silva Dal Maria & Bruno Santiago de Oliveira
 * @version 1.0.0
 * 
 * @see {@link java.lang.RuntimeException}
 */
public class EmptyFileException extends RuntimeException {
   
    /**
     * Constructs a new {@code EmptyFileException} with no message.
     */
    public EmptyFileException() {
        super();
    }

    /**
     * Constructs a new {@code EmptyFileException} with the specified message.
     *
     * @param s the detail message.
     */
    public EmptyFileException(String s) {
        super(s);
    }

}
