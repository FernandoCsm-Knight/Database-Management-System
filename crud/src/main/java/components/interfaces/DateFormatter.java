package components.interfaces;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public interface DateFormatter {
    final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

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

    Date dateParser(String date);
}