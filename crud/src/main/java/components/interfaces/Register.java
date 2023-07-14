package components.interfaces;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import components.Show;

public abstract class Register<T> {

   // Variables

   public static int MAX_REGISTER_SIZE = 1024; 
   public static Map<String, Comparator<?>> properties = new HashMap<>();

   // Methods

   public abstract int getId();

   public abstract void setId(int id);

   public abstract void from(String... arr);

   public abstract byte[] toByteArray() throws IOException;

   public abstract void fromByteArray(byte[] b) throws IOException;

   public abstract void set(String key, Object value);

   @SuppressWarnings("unchecked")
   public int compare(String key, Object obj) {
      int res = this.getId() - ((Show)obj).getId();

      try {
         res = ((Comparable<Object>)this.getClass().getDeclaredField(key).get(this)).compareTo(obj);
      } catch(IllegalAccessException e) {
         System.out.println("It is not possible to access the field " + key + " of the class " + this.getClass().getName());
         e.printStackTrace();
      } catch(NoSuchFieldException e) {
         System.out.println("The field " + key + " does not exist in the class " + this.getClass().getName());
         e.printStackTrace();
      }

      return res;
   }

   public abstract T clone();
}