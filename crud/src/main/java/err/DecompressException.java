package err;

public class DecompressException extends RuntimeException {
    public DecompressException() {
        super();
    }

    public DecompressException(String message) {
        super(message);
    }
}
