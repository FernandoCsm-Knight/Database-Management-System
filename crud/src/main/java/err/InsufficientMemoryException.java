package err;

public class InsufficientMemoryException extends RuntimeException {

    public InsufficientMemoryException() {
        super();
    }

    public InsufficientMemoryException(String s) {
        super(s);
    }

}
