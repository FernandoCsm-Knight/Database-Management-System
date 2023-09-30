/**
 * An interface for date formatting and parsing.
 * 
 * <p>
 * The date format used for formatting and parsing dates.
 * </p>
 * 
 * <p>
 * A map to convert month names to their corresponding numerical values.
 * </p>
 * 
 * <p>
 * Parses the input date string and returns a Date object.
 * </p>
 * 
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */

package components.interfaces;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A interface for date formatting and parsing.
 */
public interface DateFormatter {
    /**
     * The date format used for formatting and parsing dates.
     */
    final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    /**
     * A map to convert month names to their corresponding numerical values.
     */
    final Map<String, String> months = new HashMap<String, String>() {{
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
     * Parses the input date string and returns a Date object.
     *
     * @param date The date string to be parsed.
     * @return A Date object representing the parsed date.
     */
    Date dateParser(String date);
}