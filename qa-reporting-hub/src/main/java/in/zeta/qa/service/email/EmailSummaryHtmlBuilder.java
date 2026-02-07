package in.zeta.qa.service.email;

import in.zeta.qa.entity.email.EmailJobSummary;
import in.zeta.qa.entity.email.EmailSummary;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class EmailSummaryHtmlBuilder {

    public static Document buildHtml(EmailSummary summary) {
        Document doc = createBaseDocument();
        buildHead(doc, summary);
        buildBody(doc, summary);
        writeHtmlToFile(doc, "email_summary_report.html");
        return doc;
    }

    // ---------------------------------------------------------------------
    // Create base HTML
    // ---------------------------------------------------------------------
    private static Document createBaseDocument() {
        Document doc = Jsoup.parse("<!doctype html><html><head></head><body></body></html>");
        doc.outputSettings().prettyPrint(true);
        return doc;
    }

    // ---------------------------------------------------------------------
    // Head and CSS
    // ---------------------------------------------------------------------
    private static void buildHead(Document doc, EmailSummary summary) {
        Element head = doc.head();
        head.appendElement("meta").attr("charset", "utf-8");
        head.appendElement("title").text(summary.getSubject());
        head.appendElement("style").attr("type", "text/css").appendText(getCss());
    }

    private static String getCss() {
        return """
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial;
            margin: 20px;
            color: #111;
            background: #fafafa;
        }
        .container {
            max-width: 1100px;
            margin: 0 auto;
            background: #fff;
            border-radius: 8px;
            padding: 20px;
            box-shadow: 0 1px 4px rgba(0,0,0,0.1);
        }
        h1 { font-size: 1.6rem; margin-bottom: 0.2rem; }
        .meta { color: #666; margin-bottom: 1rem; }
        .section {
            margin-top: 24px;
            border-radius: 8px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.06);
            padding: 12px;
            background: #fff;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
        }
        th, td {
            padding: 8px 10px;
            text-align: left;
            border-bottom: 1px solid #eee;
        }
        th {
            background: #f7f7f8;
            font-weight: 600;
        }
        td:not(:last-child), th:not(:last-child) {
            border-right: 1px solid #eee;
        }

        /* Status badges */
        .status-PASS {
            background-color: #0b7a11;
            color: #fff;
            font-weight: 500;
            text-align: center;
            border-radius: 5px;
            padding: 3px 6px;
            display: inline-block;
            min-width: 50px;
        }
        .status-FAIL {
            background-color: #c62828;
            color: #fff;
            font-weight: 500;
            text-align: center;
            border-radius: 5px;
            padding: 3px 6px;
            display: inline-block;
            min-width: 50px;
        }

        /* Pass % + Trend combined cell */
        .pass-trend {
            font-weight: 600;
            color: #4a2a7a;
            white-space: nowrap;
        }
        .pass-value {
            margin-right: 4px;
        }
        .trend-icon {
            font-size: 0.9em;
            font-weight: bold;
            vertical-align: middle;
        }
        .trend-up   { color: #0a7f2a; }  /* green for UP (▲) */
        .trend-down { color: #c43f3f; }  /* red for DOWN (▼) */
        .trend-same { color: #e0b000; }  /* yellow for SAME (↔) */
        .trend-new  { color: #1fa345; }  /* bright green dot for NEW (●) */

        /* Misc */
        .no-jobs { color: #777; padding: 12px 0; }
        a.report-link {
            color: #1565c0;
            text-decoration: none;
        }
        a.report-link:hover {
            text-decoration: underline;
        }
        """;
    }

    // ---------------------------------------------------------------------
    // Body
    // ---------------------------------------------------------------------
    private static void buildBody(Document doc, EmailSummary summary) {
        Element body = doc.body();
        Element container = body.appendElement("div").addClass("container");

        container.appendElement("h1")
                .attr("style", "color:#5e2ca5; font-weight:700; font-family:Arial, sans-serif; margin-bottom:10px;")
                .text(summary.getSubject());
        if (summary.getSections() != null && !summary.getSections().isEmpty()) {
            for (EmailSummary.Section section : summary.getSections()) {
                renderSection(container, section);
            }
        } else {
            container.appendElement("div").text("No sections available.");
        }
    }

    // ---------------------------------------------------------------------
    // Section and Table
    // ---------------------------------------------------------------------
    private static void renderSection(Element parent, EmailSummary.Section section) {
        Element sect = parent.appendElement("div").addClass("section");
        sect.appendElement("h2")
                .attr("style", "color:#5e2ca5; font-weight:600; font-family:Arial, sans-serif; margin-top:15px;")
                .text("\uD83D\uDCC8 Daily Test Summary - " + section.getSection());

        if (section.getJobs() == null || section.getJobs().isEmpty()) {
            sect.appendElement("div").addClass("no-jobs").text("No jobs found.");
            return;
        }
        renderJobTable(sect, section);
    }

    private static void renderJobTable(Element sect, EmailSummary.Section section) {
        Element table = sect.appendElement("table");
        renderTableHeader(table);
        Element tbody = table.appendElement("tbody");
        for (EmailJobSummary job : section.getJobs()) {
            renderJobRow(tbody, job);
        }
    }

    private static void renderTableHeader(Element table) {
        Element headerRow = table.appendElement("thead").appendElement("tr");
        String headerStyle = "background-color:#f3e8ff; color:#5e2ca5; font-weight:bold; "
                + "text-align:left; padding:8px; border-bottom:2px solid #5e2ca5;";
        headerRow.appendElement("th").attr("style", headerStyle).text("Feature");
        headerRow.appendElement("th").attr("style", headerStyle).text("Environment");
        headerRow.appendElement("th").attr("style", headerStyle).text("Status");
        headerRow.appendElement("th").attr("style", headerStyle).text("Pass %");
        headerRow.appendElement("th").attr("style", headerStyle).text("Exec Time");
        headerRow.appendElement("th").attr("style", headerStyle).text("Report");
    }

    // ---------------------------------------------------------------------
    // Job Row
    // ---------------------------------------------------------------------
    private static void renderJobRow(Element tbody, EmailJobSummary job) {
        String cellStyle = "color:#5e2ca5; font-weight:500; padding:6px 10px; "
                + "border-bottom:1px solid #ddd; text-align:left;";
        Element row = tbody.appendElement("tr");
        // Feature Name
        String jobName = Objects.toString(job.getName(), "N/A");
        row.appendElement("td").attr("style", cellStyle).text(jobName);
        // Environment
        String env = Objects.toString(job.getEnvironment(), "UNKNOWN");
        row.appendElement("td").attr("style", cellStyle).text(env);
        // Status
        String status = Objects.toString(job.getStatus(), "UNKNOWN").toUpperCase();
        renderStatusCell(row, status);
        // Pass %
        String passPct = job.getPassPercentage() != null ? job.getPassPercentage().toString() : "-";
        String trend = Objects.toString(job.getTrend(), "none");
        renderPassWithTrend(row, passPct, trend);
        // Execution Time
        String execTime = job.getExecutionTime() != null ? job.getExecutionTime() : "-";
        row.appendElement("td").attr("style", cellStyle).text(execTime);
        // Report Link
        String reportLink = Objects.toString(job.getReportLink(), "#");
        renderReportLinkCell(row, reportLink);
    }

    private static void renderStatusCell(Element row, String status) {
        // ✅ Colored status cell (green/red/orange with white text)
        String statusColor = switch (status) {
            case "PASS" -> "#28a745";   // Green
            case "FAIL" -> "#dc3545";   // Red
            case "UNSTABLE" -> "#f39c12"; // Orange
            default -> "#6c757d";        // Gray
        };

        String statusStyle = "background-color:" + statusColor + "; color:#fff; "
                + "font-weight:bold; text-align:center; border-radius:5px; padding:4px 8px;";
        row.appendElement("td").attr("style", statusStyle).text(status);
    }

    private static void renderPassWithTrend(Element row, String passPercent, String trend) {
        String symbol = switch (trend.toUpperCase()) {
            case "UP"   -> "▲";   // Up arrow
            case "DOWN" -> "▼";   // Down arrow
            case "SAME" -> "↔";   // Neutral (yellow)
            case "NEW"  -> "●";   // New (optional bright green)
            default     -> "-";
        };

        Element passTd = row.appendElement("td").addClass("pass-trend");
        passTd.appendElement("span").addClass("pass-value").text(passPercent);
        passTd.appendText(" ");
        passTd.appendElement("span")
                .addClass("trend-icon trend-" + trend.toLowerCase()).text(symbol);
    }


    private static void renderReportLinkCell(Element row, String link) {
        Element linkCell = row.appendElement("td");
        linkCell.appendElement("a")
                .addClass("report-link")
                .attr("href", link)
                .attr("target", "_blank")
                .text("View");
    }

    // ---------------------------------------------------------------------
    // File Writing Utility
    // ---------------------------------------------------------------------
    public static void writeHtmlToFile(Document doc, String fileName) {
        try {
            Path currentDir = Path.of(System.getProperty("user.dir"));
            File outputFile = currentDir.resolve(fileName).toFile();

            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.write(doc.outerHtml());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write HTML report file", e);
        }
    }
}
