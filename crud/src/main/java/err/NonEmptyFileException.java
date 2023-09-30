package err;

/**
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */
public class NonEmptyFileException extends RuntimeException {

    public NonEmptyFileException() {
        super();
    }

    public NonEmptyFileException(String s) {
        super(s);
    }

}
