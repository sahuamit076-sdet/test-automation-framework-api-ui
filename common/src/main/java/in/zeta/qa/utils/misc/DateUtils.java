package in.zeta.qa.utils.misc;

import in.zeta.qa.constants.CommonConstants;
import in.zeta.qa.utils.exceptions.UtilsException;
import in.zeta.qa.utils.fileUtils.PropertyFileReader;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class DateUtils {

    // 🔹 Common Time Zones
    private static final String TIME_ZONE_IST = "Asia/Kolkata";
    private static final String TIME_ZONE_GMT = "GMT";

    // 🔹 Common Date-Time Patterns
    private static final String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String PURCHASE_DATE_PATTERN = "MMdd";
    private static final String VALUE_DATEBOOK_PATTERN = "yyyyMMdd";
    private static final String NEXT_RUN_INPUT_PATTERN = "HH:mm:ss";
    private static final String NEXT_RUN_OUTPUT_PATTERN = "dd MMM yyyy HH:mm:ss, 'GMT'XXX";


    public static Instant getInstantCurrentTimeStamp() {
        return Instant.now();
    }

    @SneakyThrows
    public static String getApplicableTimeZoneId() {
        return Optional.ofNullable(PropertyFileReader.getPropertyValue("time.zone"))
                .orElse(CommonConstants.UTC_TIME_ZONE);
    }

    public static long getDurationBetweenTimeStamps(Instant startDate, Instant endDate) {
        if (Objects.nonNull(startDate) && Objects.nonNull(endDate)) {
            return ChronoUnit.SECONDS.between(startDate, endDate);
        }
        throw new UtilsException("Date Utils - Calculation for Duration Between timestamps failed!");
    }

    public static String getCurrentTimeInString() {
        return Instant.now().toString().replace(":", "_");
    }

    public static String modifyCurrentDateFor(String days, String format) {
        int offset = Integer.parseInt(days);
        LocalDate date = LocalDate.now().plusDays(offset);
        return date.format(DateTimeFormatter.ofPattern(format));
    }

    public static String formatValueDateBookDateFromEpoch(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    public static String formatValueDateBookDateFromEpoch(Long dateTimeInEpochMillis) {
        LocalDate date = Instant.ofEpochMilli(dateTimeInEpochMillis)
                .atZone(ZoneId.of(getApplicableTimeZoneId()))
                .toLocalDate();
        return date.format(DateTimeFormatter.ofPattern(VALUE_DATEBOOK_PATTERN));
    }

    public static String formatValueDateBookDateFromDateTimeInIst(String dateTime) {
        LocalDateTime localDateTime = OffsetDateTime.parse(dateTime)
                .atZoneSameInstant(ZoneId.of(getApplicableTimeZoneId()))
                .toLocalDateTime();
        return localDateTime.format(DateTimeFormatter.ofPattern(VALUE_DATEBOOK_PATTERN));
    }

    public static String formatEpochTimeStamp(Long epochTimestamp, String zoneId, String dateTimeFormat) {
        return Instant.ofEpochSecond(epochTimestamp)
                .atZone(ZoneId.of(zoneId))
                .format(DateTimeFormatter.ofPattern(dateTimeFormat));
    }

    public static String getPurchaseDate(String dateInEpochMillis) {
        return Instant.ofEpochMilli(Long.parseLong(dateInEpochMillis))
                .atZone(ZoneId.of(TIME_ZONE_IST))
                .format(DateTimeFormatter.ofPattern(PURCHASE_DATE_PATTERN));
    }

    public static String getTargetDateFormat(String originalFormat, String targetFormat, String inputDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(originalFormat);
        LocalDate date = LocalDate.parse(inputDate, inputFormatter);
        return date.format(DateTimeFormatter.ofPattern(targetFormat));
    }

    public static String getCurrentDateTime(String format) {
        return ZonedDateTime.now(ZoneId.of(TIME_ZONE_IST))
                .format(DateTimeFormatter.ofPattern(format));
    }

    public static String convertToGMT(long timestamp) {
        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.of(TIME_ZONE_GMT))
                .format(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN));
    }

    public static String convertToDefault(long timestamp) {
        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN));
    }

    public static String convertToTimeZone(long timestamp, String timeZone) {
        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.of(timeZone))
                .format(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN));
    }

    public static String extractFromTimestamp(String timestamp, String pattern) {
        Instant instant = Instant.parse(timestamp);
        return instant.atZone(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String getFormattedDateFromEpoch(Long epochInMillis, String format, ZoneId zoneId) {
        return Instant.ofEpochMilli(epochInMillis)
                .atZone(zoneId)
                .format(DateTimeFormatter.ofPattern(format));
    }

    public static String getNextScheduleRun(String time, String timeZone) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(NEXT_RUN_INPUT_PATTERN);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(NEXT_RUN_OUTPUT_PATTERN);

        LocalTime scheduledTime = LocalTime.parse(time, timeFormatter);
        LocalTime now = LocalTime.now(ZoneId.of(timeZone));
        LocalDate today = LocalDate.now(ZoneId.of(timeZone));

        LocalDate nextRunDate = now.isBefore(scheduledTime) ? today : today.plusDays(1);
        ZonedDateTime nextRunDateTime = ZonedDateTime.of(nextRunDate, scheduledTime, ZoneId.of(timeZone));

        return nextRunDateTime.format(outputFormatter);
    }

    public static String getCurrentEpochTime() {
        return String.valueOf(System.currentTimeMillis());
    }

    public static String convertDateToFirstOfNextMonth(String statementDate) {
        LocalDate date = LocalDate.parse(statementDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate firstDayOfNextMonth = date.plusMonths(1).withDayOfMonth(1);
        return firstDayOfNextMonth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static Pair<String, String> getStartEndDateForPayment() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date endDateObj = calendar.getTime();
        String endDate = sdf.format(endDateObj);
        calendar.add(Calendar.YEAR, -1);
        Date startDateObj = calendar.getTime();
        String startDate = sdf.format(startDateObj);
        return Pair.of(startDate, endDate);
    }

    public static String convertDateToCompactFormat(String dateStr) {
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("ddMMyyyy")).
                format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }



}