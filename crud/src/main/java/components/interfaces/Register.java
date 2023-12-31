/**
 * Interface for a generic register.
 * 
 * <p>
 * A register is a block of bytes in the disk. It has a fixed size.
 * The register is used to store data in the disk.
 * </p>
 * 
 * <p>
 * The register is used to store data in the disk. The register is
 * used to store data in the disk. The register is used to store data
 * in the disk. 
 * </p>
 * 
 * @param <T> Type of the register
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */

package components.interfaces;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Map;

import crud.core.security.RSA;

/**
 * An abstract meta class representing a generic register.
 *
 * @param <T> The type of the register.
 */
public abstract class Register<T> {

   // Variables

   /**
    * The RSA object used to encript and decript the register.
    */
   protected RSA rsa; 

   /**
    * The maximum size of the register.
    */
   public static int MAX_REGISTER_SIZE = 64; 

   // Standard contructor

   public Register() {
      rsa = new RSA(1024); // ou qualquer outro tamanho de bit desejado
   }

   // Methods

   /**
    * Get the ID of the register.
    *
    * @return The ID of the register.
    */
   public abstract int getId();

   /**
    * Set the ID of the register.
    *
    * @param id The ID to set.
    */
   public abstract void setId(int id);

   /**
    * Initialize the register from an array of strings.
    *
    * @param arr The array of strings to initialize from.
    */
   public abstract void from(String... arr);

   /**
    * Convert the register to a byte array.
    *
    * @return A byte array representation of the register.
    * @throws IOException If an I/O error occurs.
    */
   public abstract byte[] toByteArray() throws IOException;

   /**
    * Initialize the register from a byte array.
    *
    * @param b The byte array to initialize from.
    * @throws IOException If an I/O error occurs.
    */
   public abstract void fromByteArray(byte[] b) throws IOException;

   /**
    * Get the value associated with the specified key.
    *
    * @param key The key to look up.
    * @return The value associated with the key.
    */
   public abstract Object get(String key);

   /**
    * Set the value associated with the specified key.
    *
    * @param key   The key to set.
    * @param value The value to associate with the key.
    */
   public abstract void set(String key, Object value);

   /**
    * Get a map of properties with their corresponding comparators.
    *
    * @return A map of properties and their comparators.
    */
   public abstract Map<String, Comparator<T>> getProperties();

   /**
    * Get the B+ tree attribute.
    *
    * @return The B+ tree attribute.
    */
   public abstract String getBPlusTreeAttribute();

   /**
    * Get the extensible hash attribute.
    *
    * @return The extensible hash attribute.
    */
   public abstract String getExtensibleHashAttribute();

   /**
    * Get an array of attributes for inverted indexing.
    *
    * @return An array of attributes for inverted indexing.
    */
   public abstract String[] getInvertedIndexAttributes();

   /**
    * Encript the appropriate attributes of the register.
    *
    * @return The encripted register.
    */
   public abstract T encrypt(); 

   /**
    * Decript the appropriate attributes of the register.
    *
    * @return The decripted register.
    */
   public abstract T decript();

   /**
    * Get the public exponent of the RSA object.
    *
    * @return The public exponent of the RSA object.
    */
   public BigInteger getPublicExponent() {
      return rsa.getPublicExponent();
   }

   /**
    * Get the modulus of the RSA object.
    *
    * @return The modulus of the RSA object.
    */
   public BigInteger getModulus() {
      return rsa.getModulus();
   }

   /**
    * Get the private exponent of the RSA object.
    *
    * @return The private exponent of the RSA object.
    */
   public BigInteger getPrivateExponent() {
      return rsa.getPrivateExponent();
   }

   /**
    * Compare the register based on a specified key and an object.
    *
    * @param key The key to compare.
    * @param obj The object to compare against.
    * @return The result of the comparison.
    */
   @SuppressWarnings("unchecked")
   public int compare(String key, Object obj) {
      int res = (obj instanceof Integer) ? this.getId() - (int)obj : -1;

      try {
         Field field = this.getClass().getDeclaredField(key);
         field.setAccessible(true);
         
         res = ((Comparable<Object>)field.get(this)).compareTo(obj);
      } catch(IllegalAccessException e) {
         System.out.println("It is not possible to access the field " + key + " of the class " + this.getClass().getName());
         e.printStackTrace();
      } catch(NoSuchFieldException e) {
         System.out.println("The field " + key + " does not exist in the class " + this.getClass().getName());
         e.printStackTrace();
      }

      return res;
   }

   /**
    * Clone the register.
    *
    * @return A cloned instance of the register.
    */
   public abstract T clone();
}