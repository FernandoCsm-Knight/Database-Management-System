package crud.indexes.types.interfaces;

import java.io.IOException;

/**
 * <strong> An interface to build index nodes. </strong>
 * 
 * <p>
 * The node is a key-value pair. The key is used to find the value.
 * The key can can be any object, so it is necessary to cast it to the correct type.
 * </p>
 * 
 * @author Fernando Campos Silva Dal Maria
 * @version 1.0.0
 */
public interface INode<T> {
    /**
     * Sets the key of the node.
     * The key can can be any object, so it is necessary 
     * to cast it to the correct type. To avoid {@code ClassCastException}, 
     * it is recommended to use the {@code instanceof} operator.
     * 
     * @param key Key of the node
     * @see java.lang.ClassCastException
     */
    public void setKey(Object key);

    /**
     * Sets the value of the node.
     * The value can can be any object, so it is necessary
     * to cast it to the correct type. To avoid {@code ClassCastException},
     * it is recommended to use the {@code instanceof} operator.
     * 
     * @param value Value of the node
     * @see java.lang.ClassCastException
     */
    public void setValue(Object value);

    /**
     * Returns the key of the node.
     * The key can can be any object, so it is necessary
     * to cast it to the correct type. To avoid {@code ClassCastException},
     * it is recommended to use the {@code instanceof} operator.
     * 
     * @return Key of the node
     * @see java.lang.ClassCastException
     */
    public Object getKey();

    /**
     * Returns the value of the node.
     * The value can can be any object, so it is necessary
     * to cast it to the correct type. To avoid {@code ClassCastException},
     * it is recommended to use the {@code instanceof} operator.
     * 
     * @return Value of the node
     * @see java.lang.ClassCastException
     */
    public Object getValue();

    /**
     * Returns the size of the node in bytes.
     * 
     * @return Size of the node in bytes
     */
    public int getBytes();

    /**
     * Reads the node from the specified buffer.
     * 
     * @param buffer Buffer to be read
     * @throws IOException 
     */
    public void fromByteArray(byte[] buffer) throws IOException;

    /**
     * Returns the {@code byte[]} representation of the node.
     * 
     * @return {@code byte[]} representation of the node
     * @throws IOException
     */
    public byte[] toByteArray() throws IOException;

    /**
     * Returns if the node is equal to the specified object.
     * The {@code Object other} can be any object, so it is necessary
     * to consider that the object can be from the same type of the node
     * or from the same type of the key. To avoid {@code ClassCastException},
     * it is recommended to use the {@code instanceof} operator.
     * 
     * @param other Object to be compared
     * @return True, if the node is equal to the specified object, false otherwise
     * @see java.lang.ClassCastException
     */
    public boolean equals(Object other);

    /**
     * Compares the node to the specified object.
     * The {@code Object other} can be any object, so it is necessary
     * to consider that the object can be from the same type of the node
     * or from the same type of the key. To avoid {@code ClassCastException},
     * it is recommended to use the {@code instanceof} operator.
     * 
     * @param other Object to be compared
     * @return 0 if the node is equal to the specified object,
     *         >0 if the node is greater than the specified object,
     *         <0 if the node is less than the specified object.
     * @see java.lang.ClassCastException
     */
    public int compareTo(Object other);

    /**
     * Returns a new instance of the node.
     * 
     * @return Copy of the node
     */
    public T clone();
}
