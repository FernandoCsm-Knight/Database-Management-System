package err;

public class DatabaseValidationException extends RuntimeException {

    public DatabaseValidationException() {

    }

    public DatabaseValidationException(String message) {
        super(message);
    }

}
