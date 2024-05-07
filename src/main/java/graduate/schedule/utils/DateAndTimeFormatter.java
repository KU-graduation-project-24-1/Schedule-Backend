package graduate.schedule.utils;

import org.springframework.stereotype.Component;

import java.sql.Time;
import java.text.SimpleDateFormat;

@Component
public class DateAndTimeFormatter {
    private static final String SECOND = ":00";
    private static final String TIME_FORMAT = "HH:mm";

    public static String timeDeleteSeconds(Time time) {
        // HH:mm:ss -> HH:mm
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);
        return dateFormat.format(time);
    }

    public static Time timeWithSeconds(String time) {
        return Time.valueOf(time + SECOND);
    }

}
