/**

@author Fernando Campos Silva Dal Maria & Bruno Santiago de Oliveira
@version 1.0.0
*/

package components.interfaces;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The interface {@code DateFormatter} represents a date formatter for crud entities.
 * @author Fernando Campos Silva Dal Maria & Bruno Santiago de Oliveira
 * @version 1.0.0
 */
public interface DateFormatter {

    /**
     * Attribute that holds a {@link SimpleDateFormat} initialized with a default pattern.
     */
    final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    /**
     * Map that corelates a month name with it`s number.
     */
    final Map<String, String> months = new HashMap<String, String>(){{
        put("January","01");
        put("February","02");
        put("March","03");
        put("April","04");
        put("May","05");
        put("June","06");
        put("July","07");
        put("August","08");
        put("September","09");
        put("October","10");
        put("November","11");
        put("December","12");
    }};

    /**
     * Returns a {@link Date} object from a given date string.
     * @param date the date string.
     * @return a {@link Date} object from a given date string.
     */
    Date dateParser(String date);
}