package err;

public class IndexCreationError extends RuntimeException {
   
    public IndexCreationError() {
        super();
    }

    public IndexCreationError(String s) {
        super(s);
    }

}
