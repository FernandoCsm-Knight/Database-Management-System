package err;

/**
 * The class {@code JsonValidationException} represents an exception that is thrown when a JSON file is invalid.
 * @author Fernando Campos Silva Dal Maria & Bruno Santiago de Oliveira
 * @version 1.0.0
 * 
 * @see {@link java.lang.RuntimeException}
 */
public class JsonValidationException extends RuntimeException {
    
    /**
     * Constructs a new {@code JsonValidationException} with no message.
     */
    public JsonValidationException() {
        super();
    }


    /**
     * Constructs a new {@code JsonValidationException} with the specified message.
     *
     * @param s the detail message.
     */
    public JsonValidationException(String s) {
        super(s);
    }

}
