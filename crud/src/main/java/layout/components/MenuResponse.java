package layout.components;

/**
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */
public class MenuResponse<T> {
    public boolean success;
    public long executionTime;
    public T body;

    public MenuResponse() {
        this(false, 0, null);
    }

    public MenuResponse(boolean success, long executionTime, T body) {
        this.success = success;
        this.executionTime = executionTime;
        this.body = body;
    }

    @Override
    public String toString() {
        return "MenuResponse [body=" + body + ", executionTime=" + executionTime + ", success=" + success + "]";
    }
}
