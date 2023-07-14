package err;

public class JsonValidationException extends RuntimeException {
    
    public JsonValidationException() {
        super();
    }

    public JsonValidationException(String s) {
        super(s);
    }

}
