package err;

/**
 * The class {@code InsufficientMemoryException} represents an exception that is thrown when there is not enough memory to perform a certain operation.
 * @author Fernando Campos Silva Dal Maria & Bruno Santiago de Oliveira
 * @version 1.0.0
 * 
 * @see {@link java.lang.RuntimeException}
 */
public class InsufficientMemoryException extends RuntimeException {
    
    /**
     * Constructs a new {@code InsufficientMemoryException} with no message.
     */
    public InsufficientMemoryException() {
        super();
    }

    /**
     * Constructs a new {@code InsufficientMemoryException} with the specified message.
     *
     * @param s the detail message.
     */
    public InsufficientMemoryException(String s) {
        super(s);
    }

}
