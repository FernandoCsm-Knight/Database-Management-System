package crud.core.types;

import components.interfaces.Register;

/**
 * Response class represents a response object with success status, a message,
 * and additional attributes like old address, new address, current address,
 * and a body of type T.
 *
 * @param <T> The type of the body of the response.
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */
public class Response<T extends Register<T>> {
    
    // Attributes

    public boolean success;         // Indicates if the operation was successful.
    public String message;          // A message describing the response.
    public long oldAddress;         // The old address associated with the response.
    public long newAddress;         // The new address associated with the response.
    public long currentAddress;     // The current address associated with the response.
    public T body;                  // The body of the response.

    // Constructor

    /**
     * Default constructor initializes a response with default values.
     */
    public Response() {
        this(false, "", -1, -1, -1, null);
    }

    /**
     * Parameterized constructor to create a response with specific values.
     *
     * @param success        Indicates if the operation was successful.
     * @param message        A message describing the response.
     * @param oldAddress     The old address associated with the response.
     * @param currentAddress The current address associated with the response.
     * @param newAddress     The new address associated with the response.
     * @param body           The body of the response.
     */
    public Response(boolean success, String message, long oldAddress, long currentAddress, long newAddress, T body) {
        this.success = success;
        this.message = message;
        this.oldAddress = oldAddress;
        this.currentAddress = currentAddress;
        this.newAddress = newAddress;
        this.body = body;
    }

    // Public Methods

    /**
     * Returns a string representation of the response object.
     *
     * @return A string containing response details.
     */
    @Override
    public String toString() {
        return "Response [message=" + message + ", success=" + success + "]";
    }
}
