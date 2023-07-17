package crud.indexes.types.interfaces;

import java.io.IOException;

public interface INode<T> {
    public void setKey(Object key);
    public void setValue(Object value);
    public Object getKey();
    public Object getValue();
    public int getBytes();

    public void fromByteArray(byte[] buffer) throws IOException;
    public byte[] toByteArray() throws IOException;
    public int compareTo(Object other);
    public T clone();
}
