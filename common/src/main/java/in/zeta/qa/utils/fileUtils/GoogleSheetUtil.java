package in.zeta.qa.utils.fileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.gson.Gson;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GoogleSheetUtil {
    private static final Logger LOG = LogManager.getLogger(GoogleSheetUtil.class);
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static String APPLICATION_NAME;
    private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE);
    private static final String SERVICE_ACCOUNT_CREDENTIALS_FILE_PATH = "src/test/resources/serviceaccountcredentials.json";

    static Sheets.Spreadsheets spreadsheets;

    @SneakyThrows
    public GoogleSheetUtil() {
        APPLICATION_NAME = PropertyFileReader.getPropertyValue("application.name");
    }

    private static HttpCredentialsAdapter getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        GoogleCredentials credentials = ServiceAccountCredentials
                .fromStream(new FileInputStream(SERVICE_ACCOUNT_CREDENTIALS_FILE_PATH))
                .createScoped(SCOPES);
        return new HttpCredentialsAdapter(credentials);
    }

    //###############################################################################################
    //################################### GOOGLE SHEET ##############################################
    //###############################################################################################

    @SneakyThrows
    private Sheets getGoogleSheetObject() {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public String extractIdFromUrl(String spreadSheetUrl) {
        String SHEET_ID = null;
        if (Objects.nonNull(spreadSheetUrl) && spreadSheetUrl.contains("docs.google.com")) {
            Matcher matcher = Pattern.compile("/d/([a-zA-Z0-9-_]+)").matcher(spreadSheetUrl);
            if (matcher.find()) {
                SHEET_ID = matcher.group(1);
                LOG.info("SHEET ID :: " + SHEET_ID);
            } else {
                throw new RuntimeException("GOOGLE SHEET URL IS NOT CORRECT");
            }
        }
        return SHEET_ID;
    }

    @SneakyThrows
    public List<List<Object>> readGoogleSheetsDataForGivenSheetName(String spreadsheetId, String sheetName) {
        Sheets service = getGoogleSheetObject();

        try {
            String range = sheetName + "!A:ZZ"; // Adjust the column range as needed
            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                LOG.info("No data found in sheet: " + sheetName);
            } else {
                return values;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @SneakyThrows
    public String readGoogleSheetsJsonDataForGivenSheetName(String spreadSheetUrl, String sheetName) {
        String sheetId = extractIdFromUrl(spreadSheetUrl);
        return readGoogleSheetsDataForGivenSheetNameForAutomationTestData(sheetId, sheetName);
    }

    @SneakyThrows
    public String readAllGoogleSheets(String spreadsheetId) {
        Sheets service = getGoogleSheetObject();
        List<String> allSheets = service.spreadsheets().get(spreadsheetId).execute()
                .getSheets().stream().map(s -> s.getProperties().getTitle())
                .toList();
        List<String> jsonList = allSheets.stream()
                .map(sheetName -> readGoogleSheetsDataForGivenSheetNameForAutomationTestData(spreadsheetId, sheetName))
                .collect(Collectors.toList());
        return new Gson().toJson(jsonList);
    }

    @SneakyThrows
    private String readGoogleSheetsDataForGivenSheetNameForAutomationTestData(String spreadsheetId, String sheetName) {
        List<List<Object>> values = readGoogleSheetsDataForGivenSheetName(spreadsheetId, sheetName);
        LOG.info(values);
        if (values == null || values.isEmpty()) {
            LOG.info("No data found in sheet: " + sheetName);
        } else {
            List<Object> headers = values.get(0);
            List<List<Object>> sheetData = values.stream()
                    .skip(1) // Skip the first row (headers)
                    .collect(Collectors.toList());
            return convertToJSON(sheetName, headers, sheetData);
        }
        return "{}";
    }

    @SneakyThrows
    public String readAllGoogleSheetsForAutomationTestData(String spreadsheetId) {
        Sheets service = getGoogleSheetObject();
        List<String> allSheets = service.spreadsheets().get(spreadsheetId).execute()
                .getSheets().stream().map(s -> s.getProperties().getTitle())
                .toList();

        List<String> jsonList = allSheets.stream()
                .map(sheetName -> readGoogleSheetsDataForGivenSheetNameForAutomationTestData(spreadsheetId, sheetName))
                .collect(Collectors.toList());
        return new Gson().toJson(jsonList);
    }

    @SneakyThrows
    public String readSpecificGoogleSheetsForAutomationTestData(String spreadsheetId, List<String> sheetNames) {
        List<String> jsonList = sheetNames.stream()
                .map(sheetName -> readGoogleSheetsDataForGivenSheetNameForAutomationTestData(spreadsheetId, sheetName))
                .collect(Collectors.toList());
        return new Gson().toJson(jsonList);
    }

    private String convertToJSON(String sheetName, List<Object> headers, List<List<Object>> values) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode sheetObject = JsonNodeFactory.instance.objectNode();
        sheetObject.put("sheetName", sheetName);

        ArrayNode sheetDataArray = sheetObject.putArray("sheetData");
        for (List<Object> row : values) {
            ObjectNode rowData = JsonNodeFactory.instance.objectNode();
            IntStream.range(0, Math.min(row.size(), headers.size()))
                    .forEach(j -> {
                        String header = headers.get(j).toString();
                        Object value = j < row.size() ? row.get(j) : null;
                        rowData.put(header, value != null ? value.toString() : null);
                    });
            sheetDataArray.add(rowData);
        }

        return objectMapper.writeValueAsString(sheetObject);
    }

    @SneakyThrows
    public static void getSpreadsheetInstance() {
        if (Objects.isNull(spreadsheets)) {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            spreadsheets = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(), getCredentials(HTTP_TRANSPORT)).setApplicationName(APPLICATION_NAME).build().spreadsheets();
        }
    }


    public void createNewSpreadSheet(String workSheetName, String sheetName, String sheetAndRange, ArrayList<Object> data) throws Exception {
        Spreadsheet createdResponse = null;
        try {
            Sheets service = getGoogleSheetObject();
            SpreadsheetProperties spreadsheetProperties = new SpreadsheetProperties();
            spreadsheetProperties.setTitle(workSheetName);
            SheetProperties sheetProperties = new SheetProperties();
            sheetProperties.setTitle(sheetName);
            Sheet sheet = new Sheet().setProperties(sheetProperties);
            Spreadsheet spreadsheet = new Spreadsheet().setProperties(spreadsheetProperties)
                    .setSheets(Collections.singletonList(sheet));
            createdResponse = service.spreadsheets().create(spreadsheet).execute();

            // Print new Spreadsheet ID
            LOG.info("Spreadsheet URL: {}", createdResponse.getSpreadsheetUrl());

            getSpreadsheetInstance();
            // write google sheet
            writeDataGoogleSheets(sheetName, data, createdResponse.getSpreadsheetId());

        } catch (GoogleJsonResponseException e) {
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 404) {
                LOG.info("Spreadesheet not found with id - {}", createdResponse.getSpreadsheetId());
            } else {
                throw e;
            }
        }
    }

    public static void writeSheet(List<Object> inputData, String sheetAndRange, String existingSpreadSheetID) throws IOException {
        @SuppressWarnings("unchecked")
        List<List<Object>> values = Arrays.asList(inputData);
        ValueRange body = new ValueRange().setValues(values);
        UpdateValuesResponse result = spreadsheets.values().update(existingSpreadSheetID, sheetAndRange, body)
                .setValueInputOption("RAW").execute();
        LOG.info("% cells updated. \n{}", result.getUpdatedCells());
    }

    public void writeDataGoogleSheets(String sheetName, List<Object> data, String existingSpreadSheetID) throws Exception {
        int nextRow = getRows(sheetName, existingSpreadSheetID) + 1;
        writeSheet(data, sheetName + "!A" + nextRow, existingSpreadSheetID);
    }

    public void deleteRowGoogleSheets(String spreadSheetID, String sheetName, String uniqueValue) throws Exception {
        int sheet = getSheetIndex(spreadSheetID, sheetName);
        int row = getRowIndex(spreadSheetID, sheetName, uniqueValue);
        if (row == 0) return;

        Request request = new Request().setDeleteDimension(new DeleteDimensionRequest()
                .setRange(new DimensionRange()
                        .setSheetId(sheet).setDimension("ROWS")
                        .setStartIndex(row).setEndIndex(row + 1)));
        getSpreadsheetInstance();
        spreadsheets.batchUpdate(spreadSheetID,
                new BatchUpdateSpreadsheetRequest().setRequests(List.of(request))).execute();
    }

    @SneakyThrows
    public static int getRows(String sheetName, String existingSpreadSheetID) {
        getSpreadsheetInstance();
        List<List<Object>> values = spreadsheets.values().get(existingSpreadSheetID, sheetName).execute().getValues();
        int numRows = values != null ? values.size() : 0;
        LOG.info("% rows retrieved. in {} In {}", sheetName, numRows);
        return numRows;
    }

    @SneakyThrows
    public int getSheetIndex(String spreadSheetID, String sheetName) {
        getSpreadsheetInstance();
        return spreadsheets.get(spreadSheetID).execute()
                .getSheets().stream().map(s -> s.getProperties().getTitle())
                .toList().indexOf(sheetName);
    }

    @SneakyThrows
    public int getRowIndex(String spreadSheetID, String sheetName, String uniqueValue) {
        getSpreadsheetInstance();
        List<List<Object>> values = spreadsheets.values().get(spreadSheetID, sheetName).execute().getValues();
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).stream().anyMatch(d -> d.equals(uniqueValue))) {
                return i;
            }
        }
        return 0;
    }
}
