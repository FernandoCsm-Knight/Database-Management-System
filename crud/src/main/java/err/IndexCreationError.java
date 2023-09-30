package err;

/**
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */
public class IndexCreationError extends RuntimeException {
   
    public IndexCreationError() {
        super();
    }

    public IndexCreationError(String s) {
        super(s);
    }

}
