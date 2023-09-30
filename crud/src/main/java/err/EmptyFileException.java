package err;

/**
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */
public class EmptyFileException extends RuntimeException {
   
    public EmptyFileException() {
        super();
    }

    public EmptyFileException(String s) {
        super(s);
    }

}
