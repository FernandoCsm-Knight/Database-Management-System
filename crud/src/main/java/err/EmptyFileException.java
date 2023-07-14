package err;

public class EmptyFileException extends RuntimeException {
   
    public EmptyFileException() {
        super();
    }

    public EmptyFileException(String s) {
        super(s);
    }

}
