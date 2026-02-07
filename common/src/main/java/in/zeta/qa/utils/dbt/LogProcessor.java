package in.zeta.qa.utils.dbt;

import lombok.AllArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LogProcessor is responsible for parsing log content and extracting test case results,
 * including test names, execution status (pass/fail), execution time or query, and module name.
 */
public class LogProcessor {

    /**
     * Represents a single test case result parsed from the log.
     */
    @AllArgsConstructor
    public static class TestCase {
        public String name;
        public String module;
        public String queryOrTime;
        public boolean passed;
    }

    /**
     * Parses the log content and extracts a list of TestCase objects,
     * identifying both passed and failed tests.
     *
     * @param logContents The full contents of the log file as a single string.
     * @return A list of extracted test cases with associated metadata.
     */
    public static List<TestCase> extractTestCases(String logContents) throws IOException {
        List<TestCase> results = new ArrayList<>();
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new StringReader(logContents))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);

                if (line.contains("Warning in test") || line.contains("Failure in test")) { // Indicates a failed test
                    String module = ModuleFinder.findModuleInLine(line);
                    if (module != null) {
                        Matcher matcher = Pattern.compile("(?:Warning|Failure) in test (\\S+)").matcher(line) ;
                        if (matcher.find()) {
                            String testName = matcher.group(1);
                            String query = extractQuery(lines, testName); // Get SQL query associated with failure
                            results.add(new TestCase(testName, module, query, false));
                        }
                    }
                } else if (line.contains("PASS")) { // Indicates a passed test
                    String module = ModuleFinder.findModuleInLine(line);
                    if (module != null) {
                        Matcher matcher = Pattern.compile("PASS (\\S+) .* in ([\\d.]+)s").matcher(line);
                        if (matcher.find()) {
                            String testName = matcher.group(1);
                            String execTime = matcher.group(2); // Get execution time
                            results.add(new TestCase(testName, module, execTime, true));
                        }
                    }
                }
            }
        }
        return results;
    }

    /**
     * Extracts the SQL query associated with a failed test from the log lines.
     * It begins at the line that contains the test name and `select` keyword, and ends when a line contains `dbt_internal_test`.
     *
     * @param lines    The list of lines from the log.
     * @param testName The name of the test to extract the query for.
     * @return The cleaned SQL query as a single string.
     */
    private static String extractQuery(List<String> lines, String testName) {
        StringBuilder query = new StringBuilder();
        boolean capturing = false;

        Pattern startPattern = Pattern.compile(Pattern.quote(testName) + ".*\\bselect\\b", Pattern.CASE_INSENSITIVE);
        Pattern endPattern = Pattern.compile("dbt_internal_test", Pattern.CASE_INSENSITIVE);

        for (String line : lines) {
            String cleaned = line.replaceAll("\u001B\\[[;\\d]*m", "").trim(); // Remove ANSI escape codes

            if (!capturing && startPattern.matcher(cleaned).find()) {
                capturing = true;
                query.append(cleanLogLine(cleaned)).append(" ");
            } else if (capturing) {
                query.append(cleanLogLine(cleaned)).append(" ");
                if (endPattern.matcher(cleaned).find()) {
                    break;
                }
            }
        }

        return query.toString().trim().replaceAll(" +", " ");
    }

    /**
     * Cleans an individual log line by removing unnecessary metadata such as timestamps,
     * logger information, and other noise.
     *
     * @param line The raw log line.
     * @return The cleaned log line.
     */
    private static String cleanLogLine(String line) {
        return line.replaceAll("\\[.*?\\]\\s*\\{.*?\\}\\s*INFO\\s*-\\s*", "") // Remove log prefix
                .replaceAll("^\\d{2}:\\d{2}:\\d{2}\\s+On\\s+test\\.[^:]+:\\s*", "") // Remove timestamped test context
                .trim();
    }
}
