package in.zeta.qa.utils.hmac;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DateTimeUtils {

    public static final DateTimeFormatter DATE_TIME_FORMATTER_WITHOUT_YEAR = DateTimeFormatter.ofPattern("MMddHHmmss");
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss").withZone(ZoneId.of("Asia/Kolkata"));

    public static String getCurrentDateTimeFor(DateTimeFormatter formatter) {
        long epochSecond = java.time.Instant.now().getEpochSecond();
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(
                        java.time.Instant.ofEpochSecond(epochSecond), ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
        return formatter.format(dateTime);
    }

    public static long getDueByTimeMillis() {
        long epochMillis = java.time.Instant.now().toEpochMilli();
        return epochMillis + 2 * 60 * 1000;
    }

    public static String getCurrentClosestDateTime(String dateTimeWithoutYear) {
        ZonedDateTime currentInstant = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        int currentYear = currentInstant.getYear();
        String lastYearTimeStamp = (currentYear - 1) % 100 + dateTimeWithoutYear;
        String currentYearTimeStamp = currentYear % 100 + dateTimeWithoutYear;
        String nextYearTimeStamp = (currentYear + 1) % 100 + dateTimeWithoutYear;
        ZonedDateTime lastYearTime = ZonedDateTime.from(FORMATTER.parse(lastYearTimeStamp));
        ZonedDateTime currentYearTime = ZonedDateTime.from(FORMATTER.parse(currentYearTimeStamp));
        ZonedDateTime nextYearTime = ZonedDateTime.from(FORMATTER.parse(nextYearTimeStamp));
        Map<String, ZonedDateTime> timeMap = new HashMap();
        timeMap.put(lastYearTimeStamp, lastYearTime);
        timeMap.put(currentYearTimeStamp, currentYearTime);
        timeMap.put(nextYearTimeStamp, nextYearTime);
        long minDiff = Long.MAX_VALUE;
        String closestDateTimeLocalTxn = null;
        Iterator var15 = timeMap.entrySet().iterator();
        while(var15.hasNext()) {
            Map.Entry<String, ZonedDateTime> entry = (Map.Entry)var15.next();
            long tempDiff = Math.abs(currentInstant.toInstant().toEpochMilli() - ((ZonedDateTime)entry.getValue()).toInstant().toEpochMilli());
            if (tempDiff < minDiff) {
                minDiff = tempDiff;
                closestDateTimeLocalTxn = (String)entry.getKey();
            }
        }

        return closestDateTimeLocalTxn;
    }
}
