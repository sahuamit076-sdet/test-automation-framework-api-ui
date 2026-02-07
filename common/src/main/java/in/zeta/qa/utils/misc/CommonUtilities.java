package in.zeta.qa.utils.misc;

import in.zeta.qa.constants.CommonConstants;
import in.zeta.qa.constants.CountryData;
import in.zeta.qa.utils.fileUtils.PropertyFileReader;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;

public class CommonUtilities {

    public static void waitInSeconds(int seconds) {
        try {
            Thread.sleep(Duration.ofSeconds(seconds).toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static String constructFieldAsPerLengthWithCharInPrefix(String value, int fieldLength, char charToAppend) {
        if (value == null) return null;
        return StringUtils.leftPad(value, fieldLength, charToAppend);
    }

    public static String constructFieldAsPerLengthWithChar(String value, int fieldLength, char charToAppend) {
        if (value == null) return null;
        return StringUtils.right(StringUtils.rightPad(value, fieldLength, charToAppend), fieldLength);
    }

    public static String generateEmail() throws IOException {
        return StringUtils.isNotEmpty(System.getProperty("emailId")) ?
                System.getProperty("emailId") :
                "COA__" + PropertyFileReader.getPropertyValue("coa") + "__" +
                        RandomStringUtils.randomNumeric(6) + "@test.zeta.tech";
    }

    public static int convertFromInrToAmount(int amount, String rate, String currencyCode) {
        double j = StringUtils.isNotEmpty(rate) ? rateCalculator(rate, currencyCode) : 1;
        return (int) (amount / j);
    }

    public static int convertToInr(int amount, String rate, String currencyCode) {
        double j = StringUtils.isNotEmpty(rate) ? rateCalculator(rate, currencyCode) : 1;
        return (int) (amount * j);
    }

    private static double rateCalculator(String rate, String currencyCode) {
        int i = Integer.parseInt(String.valueOf(rate.charAt(0)));
        double rateValue = Integer.parseInt(rate.substring(1)) / Math.pow(10, i);
        if (StringUtils.isNotEmpty(currencyCode)) {
            String finalCurrencyCode = CountryData.getEnglishCurrencyCode(currencyCode);
            int d = Arrays.stream(CountryData.values()).filter(cc -> cc.name().equalsIgnoreCase(finalCurrencyCode))
                    .findFirst().map(CountryData::getNoOfDecimalSupported).orElse(0);
            return rateValue * (Math.pow(10, 2 - d));
        }
        return rateValue;
    }

    public static String getPhoneNumWithExt(String phoneNumWithoutExt, CountryData... country) {
        String ext = (country.length > 0) && Objects.nonNull(country[0]) ? country[0].getPhoneExt() : CountryData.INDIA.getPhoneExt();
        // Normalize input (remove spaces, trim)
        String phone = phoneNumWithoutExt.trim().replaceAll("\\s+", "");
        // Case 1: already fully qualified
        if (phone.startsWith(CommonConstants.PLUS)) {
            return phone;
        }
        // Case 2: already has ext (check length too, e.g. ext + 10 digits for India)
        if (phone.startsWith(ext) && phone.length() == (ext.length() + 10)) {
            return CommonConstants.PLUS + phone;
        }
        // Case 3: otherwise, prepend +ext
        return CommonConstants.PLUS + ext + phone;
    }

    public static String removePhoneExt(String phoneNumWithExt, CountryData... country) {
        String ext = (country.length > 0) && Objects.nonNull(country[0]) ? country[0].getPhoneExt() : CountryData.INDIA.getPhoneExt();
        // Normalize input (remove spaces, trim)
        String phone = phoneNumWithExt.trim().replaceAll("\\s+", "");
        // Case 1: starts with +ext → remove
        if (phone.startsWith(CommonConstants.PLUS + ext)) {
            return phone.substring((CommonConstants.PLUS + ext).length());
        }
        // Case 2: starts with ext (without +) → remove
        if (phone.startsWith(ext)) {
            return phone.substring(ext.length());
        }
        // Case 3: no ext → return as is
        return phone;
    }

}
