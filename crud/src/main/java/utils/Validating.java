package utils;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Classe para a validação de strings de acordo com padrões específicos.
 */
public final class Validating {
   
   private final String label;
   private String s;

   public Validating(String s) {
      this(s, null);
   }

   public Validating(String s, String label) {
      this.s = s;
      this.label = label;
   }

   public String getLabel() {
      return label;
   }

   public Boolean isEmail() {
      Pattern reg = Pattern.compile("^[a-z0-9%$._+-]+@[a-z0-9.-]+\\.[a-z]{2,6}$", Pattern.CASE_INSENSITIVE);
      Matcher m = reg.matcher(s);
      return m.matches();
   }

   public Boolean isHttp() {
      return s.startsWith("http://");
   }

   public Boolean isHttps() {
      return s.startsWith("https://");
   }

   public Boolean isLink() {
      return this.isHttp() || this.isHttps();
   }

}
