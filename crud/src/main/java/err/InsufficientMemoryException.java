package err;

/**
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */
public class InsufficientMemoryException extends RuntimeException {

    public InsufficientMemoryException() {
        super();
    }

    public InsufficientMemoryException(String s) {
        super(s);
    }

}
