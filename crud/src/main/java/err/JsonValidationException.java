package err;

/**
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */
public class JsonValidationException extends RuntimeException {
    
    public JsonValidationException() {
        super();
    }

    public JsonValidationException(String s) {
        super(s);
    }

}
