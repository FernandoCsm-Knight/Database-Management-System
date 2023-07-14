package err;

public class NonEmptyFileException extends RuntimeException {

    public NonEmptyFileException() {
        super();
    }

    public NonEmptyFileException(String s) {
        super(s);
    }

}
