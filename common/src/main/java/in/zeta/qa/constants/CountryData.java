package in.zeta.qa.constants;

import in.zeta.qa.utils.fileUtils.PropertyFileReader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum CountryData {

    ALBANIA("ALL", "008", "AL", 2, "355", "6", 9),
    ALGERIA("DZD", "012", "DZ", 2, "213", "5", 9),
    ARGENTINA("ARP", "032", "AR", 2, "54", "9", 10),
    AUSTRALIA("AUD", "036", "AU", 2, "61", "4", 9),
    BAHAMAS("BSD", "044", "BS", 2, "1-242", "2", 7),
    BAHRAIN("BHD", "048", "BH", 3, "973", "3", 8),
    BANGLADESH("BDT", "050", "BD", 2, "880", "1", 10),
    BARBADOS("BBD", "052", "BB", 2, "1-246", "2", 7),
    BERMUDA("BMD", "060", "BM", 2, "1-441", "3", 7),
    BHUTAN("BTN", "064", "BT", 2, "975", "1", 8),
    BOLIVIA("BOB", "068", "BO", 2, "591", "7", 8),
    BOTSWANA("BWP", "072", "BW", 2, "267", "7", 8),
    BELIZE("BZD", "084", "BZ", 2, "501", "6", 7),
    SOLOMON_ISLANDS("SBD", "090", "SB", 2, "677", "7", 7),
    BRUNEI("BND", "096", "BN", 2, "673", "8", 7),
    MYANMAR("MMK", "104", "MM", 2, "95", "9", 9),
    BURUNDI("BIF", "108", "BI", 0, "257", "7", 8),
    CAMBODIA("KHR", "116", "KH", 2, "855", "1", 9),
    CANADA("CAD", "124", "CA", 2, "1", "4", 10),
    CAPE_VERDE("CVE", "132", "CV", 2, "238", "9", 7),
    CAYMAN_ISLANDS("KYD", "136", "KY", 2, "1-345", "3", 7),
    SRI_LANKA("LKR", "144", "LK", 2, "94", "7", 9),
    CHILE("CLP", "152", "CL", 0, "56", "9", 9),
    CHINA("CNY", "156", "CN", 2, "86", "1", 11),
    COLOMBIA("COP", "170", "CO", 2, "57", "3", 10),
    COMOROS("KMF", "174", "KM", 0, "269", "7", 9),
    COSTA_RICA("CRC", "188", "CR", 2, "506", "6", 8),
    CUBA("CUP", "192", "CU", 2, "53", "5", 8),
    CZECH_REPUBLIC("CZK", "203", "CZ", 2, "420", "7", 9),
    DENMARK("DKK", "208", "DK", 2, "45", "3", 8),
    DOMINICAN_REPUBLIC("DOP", "214", "DO", 2, "1-809 / 1-829 / 1-849", "2", 10),
    EL_SALVADOR("SVC", "222", "SV", 2, "503", "2", 8),
    ETHIOPIA("ETB", "230", "ET", 2, "251", "9", 10),
    FALKLAND_ISLANDS("FKP", "238", "FK", 2, "500", "5", 8),
    FIJI("FJD", "242", "FJ", 2, "679", "9", 7),
    DJIBOUTI("DJF", "262", "DJ", 0, "253", "7", 8),
    GAMBIA("GMD", "270", "GM", 2, "220", "2", 8),
    GIBRALTAR("GIP", "292", "GI", 2, "350", "5", 8),
    GUATEMALA("GTQ", "320", "GT", 2, "502", "2", 8),
    GUINEA("GNF", "324", "GN", 0, "224", "7", 9),
    GUYANA("GYD", "328", "GY", 2, "592", "6", 7),
    HAITI("HTG", "332", "HT", 2, "509", "3", 8),
    HONDURAS("HNL", "340", "HN", 2, "504", "9", 8),
    HONG_KONG("HKD", "344", "HK", 2, "852", "5", 8),
    HUNGARY("HUF", "348", "HU", 0, "36", "1", 8),
    ICELAND("ISK", "352", "IS", 0, "354", "6", 7),
    INDIA("INR", "356", "IN", 2, "91", "7", 10),
    INDONESIA("IDR", "360", "ID", 2, "62", "8", 10),
    IRAN("IRR", "364", "IR", 2, "98", "9", 10),
    IRAQ("IQD", "368", "IQ", 3, "964", "7", 8),
    ISRAEL("ILS", "376", "IL", 2, "972", "5", 9),
    JAMAICA("JMD", "388", "JM", 2, "1-876", "2", 7),
    JAPAN("JPY", "392", "JP", 0, "81", "0", 10),
    KAZAKHSTAN("KZT", "398", "KZ", 2, "7", "7", 10),
    JORDAN("JOD", "400", "JO", 3, "962", "7", 8),
    KENYA("KES", "404", "KE", 2, "254", "7", 10),
    NORTH_KOREA("KPW", "408", "KP", 2, "850", "2", 9),
    SOUTH_KOREA("KRW", "410", "KR", 0, "82", "1", 11),
    KUWAIT("KWD", "414", "KW", 3, "965", "5", 8),
    KYRGYZSTAN("KGS", "417", "KG", 2, "996", "7", 9),
    LAOS("LAK", "418", "LA", 2, "856", "7", 8),
    LEBANON("LBP", "422", "LB", 2, "961", "7", 8),
    LESOTHO("LSL", "426", "LS", 2, "266", "5", 9),
    LIBERIA("LRD", "430", "LR", 2, "231", "2", 7),
    LIBYA("LYD", "434", "LY", 3, "218", "5", 8),
    MACAU("MOP", "446", "MO", 2, "853", "6", 8),
    MALAWI("MWK", "454", "MW", 2, "265", "7", 9),
    MALAYSIA("MYR", "458", "MY", 2, "60", "1", 10),
    MALDIVES("MVR", "462", "MV", 2, "960", "7", 8),
    MAURITIUS("MUR", "480", "MU", 2, "230", "5", 8),
    MEXICO("MXN", "484", "MX", 2, "52", "1", 10),
    MONGOLIA("MNT", "496", "MN", 2, "976", "9", 8),
    MOLDOVA("MDL", "498", "MD", 2, "373", "2", 8),
    MOROCCO("MAD", "504", "MA", 2, "212", "6", 9),
    OMAN("OMR", "512", "OM", 2, "968", "7", 8),
    NAMIBIA("NAD", "516", "NA", 2, "264", "8", 9),
    NEPAL("NPR", "524", "NP", 2, "977", "9", 10),
    NETHERLANDS_ANTILLES("ANG", "532", "NL", 2, "31", "6", 9),
    ARUBA("AWG", "533", "AW", 2, "297", "5", 7),
    VANUATU("VUV", "548", "VU", 2, "678", "7", 7),
    NEW_ZEALAND("NZD", "554", "NZ", 2, "64", "2", 9),
    NICARAGUA("NIO", "558", "NI", 2, "505", "5", 8),
    NIGERIA("NGN", "566", "NG", 2, "234", "7", 10),
    NORWAY("NOK", "578", "NO", 2, "47", "4", 8),
    PAKISTAN("PKR", "586", "PK", 2, "92", "3", 9),
    PANAMA("PAB", "590", "PA", 2, "507", "6", 8),
    PAPUA_NEW_GUINEA("PGK", "598", "PG", 2, "675", "7", 8),
    PARAGUAY("PYG", "600", "PY", 2, "595", "9", 8),
    PHILIPPINES("PHP", "608", "PH", 2, "63", "9", 10),
    QATAR("QAR", "634", "QA", 2, "974", "3", 8),
    RUSSIA("RUB", "643", "RU", 2, "7", "9", 10),
    RWANDA("RWF", "646", "RW", 2, "250", "7", 9),
    SAINT_HELENA("SHP", "654", "SH", 2, "290", "2", 7),
    SAUDI_ARABIA("SAR", "682", "SA", 2, "966", "5", 9),
    SEYCHELLES("SCR", "690", "SC", 2, "248", "2", 7),
    SIERRA_LEONE("SLL", "694", "SL", 2, "232", "7", 8),
    SINGAPORE("SGD", "702", "SG", 2, "65", "9", 8),
    VIETNAM("VND", "704", "VN", 2, "84", "9", 10),
    SOMALIA("SOS", "706", "SO", 2, "252", "7", 9),
    SOUTH_AFRICA("ZAR", "710", "ZA", 2, "27", "6", 9),
    SOUTH_SUDAN("SSP", "728", "SS", 2, "211", "9", 9),
    ESWATINI("SZL", "748", "SZ", 2, "268", "7", 9),
    SWEDEN("SEK", "752", "SE", 2, "46", "7", 9),
    SWITZERLAND("CHF", "756", "CH", 2, "41", "7", 9),
    THAILAND("THB", "764", "TH", 2, "66", "8", 9),
    TONGA("TOP", "776", "TO", 2, "676", "7", 9),
    TRINIDAD_AND_TOBAGO("TTD", "780", "TT", 2, "1-868", "2", 7),
    UNITED_ARAB_EMIRATES("AED", "784", "AE", 2, "971", "5", 9),
    TUNISIA("TND", "788", "TN", 2, "216", "7", 8),
    UGANDA("UGX", "800", "UG", 2, "256", "7", 9),
    NORTH_MACEDONIA("MKD", "807", "MK", 2, "389", "7", 8),
    EGYPT("EGP", "818", "EG", 2, "20", "1", 11),
    UNITED_KINGDOM("GBP", "826", "GB", 2, "44", "7", 10),
    TANZANIA("TZS", "834", "TZ", 2, "255", "7", 9),
    UNITED_STATES("USD", "840", "US", 2, "1", "1", 10),
    URUGUAY("UYU", "858", "UY", 2, "598", "7", 9),
    UZBEKISTAN("UZS", "860", "UZ", 2, "998", "9", 9),
    SAMOA("WST", "882", "WS", 2, "685", "7", 9),
    YEMEN("YER", "886", "YE", 2, "967", "7", 9),
    TAIWAN("TWD", "901", "TW", 2, "886", "9", 10),
    VENEZUELA("VES", "928", "VE", 2, "58", "4", 10),
    MAURITANIA("MRU", "929", "MR", 2, "222", "7", 8),
    SAO_TOME_AND_PRINCIPE("STN", "930", "ST", 2, "239", "9", 8),
    ZIMBABWE("ZWL", "932", "ZW", 2, "263", "9", 9),
    BELARUS("BYN", "933", "BY", 2, "375", "8", 9),
    TURKMENISTAN("TMT", "934", "TM", 2, "993", "7", 9),
    GHANA("GHS", "936", "GH", 2, "233", "2", 9),
    SERBIA("RSD", "941", "RS", 2, "381", "6", 8),
    MOZAMBIQUE("MZN", "943", "MZ", 2, "258", "8", 9),
    AZERBAIJAN("AZN", "944", "AZ", 2, "994", "9", 9),
    ROMANIA("RON", "946", "RO", 2, "40", "7", 9),
    TURKEY("TRY", "949", "TR", 2, "90", "5", 10),
    CAMEROON("XAF", "950", "CM", 2, "237", "6", 9),
    SAINT_KITTS_AND_NEVIS("XCD", "951", "KN", 2, "1-869", "2", 7),
    IVORY_COAST("XOF", "952", "CI", 2, "225", "2", 8),
    FRENCH_POLYNESIA("XPF", "953", "PF", 2, "689", "7", 9),
    ZAMBIA("ZMW", "967", "ZM", 2, "260", "9", 9),
    SURINAME("SRD", "968", "SR", 2, "597", "9", 9),
    MADAGASCAR("MGA", "969", "MG", 2, "261", "3", 9),
    AFGHANISTAN("AFN", "971", "AF", 2, "93", "7", 9),
    TAJIKISTAN("TJS", "972", "TJ", 2, "992", "9", 9),
    ANGOLA("AOA", "973", "AO", 2, "244", "9", 9),
    BULGARIA("BGN", "975", "BG", 2, "359", "9", 9),
    CONGO("CDF", "976", "CD", 2, "243", "9", 9),
    BOSNIA_AND_HERZEGOVINA("BAM", "977", "BA", 2, "387", "6", 8),
    EUROPEAN_UNION("EUR", "978", "EU", 2, "", "", 0),
    ABC("ABC", "000", "AB", 2, "", "", 0),
    DEFAULT("DEFAULT", "", "", 2, "", "", 0);

    private final String currencyCode;
    private final String numericCurrencyCode;
    private final String countryCode;
    private final Integer noOfDecimalSupported;
    private final String phoneExt;
    private final String phoneStartDigit;
    private final int phoneNumberLength;

    /**
     * PASS ANY CURRENCY CODE LIKE [INR or 356]
     *
     * @param currencyCode
     * @return [356]
     */
    public static String getNumericCurrencyCode(String currencyCode) {
        if (!StringUtils.isNumeric(currencyCode)) {
            CountryData currencyCodeObj = Arrays.stream(CountryData.values())
                    .filter(c -> c.getCurrencyCode().equals(currencyCode))
                    .findFirst().orElse(DEFAULT);
            return currencyCodeObj.getNumericCurrencyCode();
        }
        return currencyCode;
    }

    /**
     * PASS ANY CURRENCY CODE LIKE [INR or 356]
     *
     * @param currencyCode
     * @return [INR]
     */
    public static String getEnglishCurrencyCode(String currencyCode) {
        return StringUtils.isNumeric(currencyCode) ? Arrays.stream(CountryData.values())
                .filter(c -> c.getNumericCurrencyCode().equals(currencyCode))
                .findFirst().get().getCurrencyCode() : currencyCode;
    }

    /**
     * PASS ANY COUNTRY CODE LIKE [IN]
     *
     * @param countryCode
     * @return [356]
     */
    public static String getNumericCurrencyCodeByCountry(String countryCode) {
        return Arrays.stream(CountryData.values())
                .filter(c -> c.getCountryCode().equals(countryCode))
                .findFirst().map(CountryData::getNumericCurrencyCode).orElse("");
    }


    /**
     * PASS ANY COUNTRY CODE LIKE [IN]
     *
     * @param countryCode
     * @return [INR]
     */
    public static String getEnglishCurrencyCodeByCountry(String countryCode) {
        return Arrays.stream(CountryData.values())
                .filter(c -> c.getCountryCode().equals(countryCode))
                .findFirst().map(CountryData::getCurrencyCode).orElse("");
    }


    /**
     * @param phoneExt
     * @return
     */
    public static CountryData getByPhoneExt(String phoneExt) {
        Optional<CountryData> countryData = Arrays.stream(CountryData.values())
                .filter(c -> c.getPhoneExt().equals(phoneExt)).findFirst();
        return countryData.orElse(CountryData.INDIA);
    }


    public static CountryData getBillingCountry() {
        String billingCountryCode = PropertyFileReader.getPropertyValue("billing.country");
        Optional<CountryData> countryData = Arrays.stream(CountryData.values())
                .filter(c -> c.getCountryCode().equalsIgnoreCase(billingCountryCode))
                .findFirst();
        return countryData.orElse(CountryData.DEFAULT);
    }


}
