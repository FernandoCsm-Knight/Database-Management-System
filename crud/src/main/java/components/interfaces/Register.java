package components.interfaces;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
/**
 * The interface {@code Register} represents a binary register for a {@link crud.base.BinaryArchive}.
 * @author Fernando Campos Silva Dal Maria & Bruno Santiago de Oliveira
 * @version 1.0.0
 */
public interface Register<T> {

   // Variables

   int MAX_REGISTER_SIZE = 1024; // maximum size of a register in bytes
   Map<String, Comparator<?>> properties = new HashMap<>();

   // Methods

   /**
    * Returns the id of the register.
    * @return the id of the register.
    */
   int getId();

   /**
    * Sets the id of the register.
    * @param id the id of the register.
    */
   void setId(int id);

   /**
    * Populates a register with the given array values.
    * @param arr array of values to populate the register.
    */
   void from(String... arr);

   /**
    * Returns the register as an array of {@code bytes}.
    * @return the register as an array of {@code bytes}.
    */
   byte[] toByteArray() throws IOException;

   /**
    * Populates a register with the given array of {@code bytes}.
    * @param b array of {@code bytes} to populate the register.
    */
   void fromByteArray(byte[] b) throws IOException;

   /**
    * Sets the specified attribute with the given value.
    * @param key the attribute to set
    * @param value the value to set
    * @throws IOException if an I/O error occurs.
    */
   void set(String key, Object value);

   /**
    * Compare a Object with the specified Register attribute.
    * @param key the Register attribute to compare
    * @param obj the object to compare
    */
   int compare(String key, Object obj);

   /**
    * Returns a clone of the register.
    * @return a clone of the register.
    */
   T clone();
}