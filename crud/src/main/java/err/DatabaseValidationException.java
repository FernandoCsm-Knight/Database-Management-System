package err;

/**
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */
public class DatabaseValidationException extends RuntimeException {

    public DatabaseValidationException() {

    }

    public DatabaseValidationException(String message) {
        super(message);
    }

}
