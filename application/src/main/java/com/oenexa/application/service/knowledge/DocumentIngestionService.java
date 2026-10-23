package com.oenexa.application.service.knowledge;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import com.oenexa.domain.model.chat.ConversationCoreDomain.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Enterprise Multi-Format Document Ingestion & Parsing Service.
 * Why: Pure sovereign Java parsing for PDF, DOCX, XLSX, CSV, JSON, XML, ZIP, and Images
 * without bulky third-party dependencies, CVE exposure, or memory bloat.
 */
@Service
public class DocumentIngestionService {

    private static final Set<String> RECOGNIZED_CODE_EXT = Set.of(
            "java", "kt", "ts", "js", "py", "go", "rs", "cpp", "c", "cs", "php", "sql", "html", "css", "json", "xml", "yml", "yaml", "md"
    );

    public ParsedDocument parse(String fileName, byte[] bytes) {
        Objects.requireNonNull(fileName, "fileName must not be null");
        byte[] safeBytes = (bytes != null) ? bytes : new byte[0];
        AttachmentType type = detectType(fileName);

        return switch (type) {
            case TXT, CODE -> parseText(fileName, type, safeBytes);
            case CSV -> parseCsv(fileName, safeBytes);
            case JSON -> parseJson(fileName, safeBytes);
            case XML -> parseXml(fileName, safeBytes);
            case DOCX -> parseDocx(fileName, safeBytes);
            case XLSX -> parseXlsx(fileName, safeBytes);
            case ZIP -> parseZip(fileName, safeBytes);
            case PDF -> parsePdf(fileName, safeBytes);
            case IMAGE -> parseImage(fileName, safeBytes);
        };
    }

    public AttachmentType detectType(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) return AttachmentType.PDF;
        if (lower.endsWith(".docx")) return AttachmentType.DOCX;
        if (lower.endsWith(".xlsx")) return AttachmentType.XLSX;
        if (lower.endsWith(".csv")) return AttachmentType.CSV;
        if (lower.endsWith(".json")) return AttachmentType.JSON;
        if (lower.endsWith(".xml")) return AttachmentType.XML;
        if (lower.endsWith(".zip") || lower.endsWith(".tar.gz")) return AttachmentType.ZIP;
        if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".webp") || lower.endsWith(".svg")) return AttachmentType.IMAGE;
        String ext = getExtension(lower);
        if (RECOGNIZED_CODE_EXT.contains(ext)) return AttachmentType.CODE;
        return AttachmentType.TXT;
    }

    private ParsedDocument parseText(String fileName, AttachmentType type, byte[] bytes) {
        String content = new String(bytes, StandardCharsets.UTF_8);
        int lines = content.split("\r\n|\r|\n").length;
        String summary = String.format("Text file with %d lines, %d characters.", lines, content.length());
        return new ParsedDocument(fileName, type, bytes.length, content, summary);
    }

    private ParsedDocument parseCsv(String fileName, byte[] bytes) {
        String raw = new String(bytes, StandardCharsets.UTF_8);
        String[] lines = raw.split("\r\n|\r|\n");
        StringBuilder table = new StringBuilder();
        int rowLimit = Math.min(lines.length, 50);

        for (int i = 0; i < rowLimit; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            String[] cols = line.split("[,;\\t]");
            table.append("| ").append(String.join(" | ", cols)).append(" |\n");
            if (i == 0) {
                table.append("| ").append(String.join(" | ", Collections.nCopies(cols.length, "---"))).append(" |\n");
            }
        }
        String summary = String.format("CSV dataset with %d total rows (rendered first %d rows in markdown table).", lines.length, rowLimit);
        return new ParsedDocument(fileName, AttachmentType.CSV, bytes.length, table.toString(), summary);
    }

    private ParsedDocument parseJson(String fileName, byte[] bytes) {
        String raw = new String(bytes, StandardCharsets.UTF_8).trim();
        String summary = "Structured JSON document with " + raw.length() + " bytes.";
        return new ParsedDocument(fileName, AttachmentType.JSON, bytes.length, raw, summary);
    }

    private ParsedDocument parseXml(String fileName, byte[] bytes) {
        String raw = new String(bytes, StandardCharsets.UTF_8);
        List<String> tags = new ArrayList<>();
        Matcher m = Pattern.compile("<([A-Za-z0-9_-]+)[^>]*>").matcher(raw);
        while (m.find() && tags.size() < 10) {
            String tag = m.group(1);
            if (!tag.startsWith("/") && !tag.startsWith("?")) {
                tags.add(tag);
            }
        }
        String summary = "XML Document with root tags: " + String.join(", ", tags);
        return new ParsedDocument(fileName, AttachmentType.XML, bytes.length, raw, summary);
    }

    private ParsedDocument parseDocx(String fileName, byte[] bytes) {
        StringBuilder extracted = new StringBuilder();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if ("word/document.xml".equalsIgnoreCase(entry.getName())) {
                    String docXml = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                    Matcher pMatcher = Pattern.compile("<w:p[ >](.*?)</w:p>").matcher(docXml);
                    while (pMatcher.find()) {
                        String pContent = pMatcher.group(1);
                        Matcher tMatcher = Pattern.compile("<w:t[^>]*>(.*?)</w:t>").matcher(pContent);
                        StringBuilder pText = new StringBuilder();
                        while (tMatcher.find()) {
                            pText.append(tMatcher.group(1));
                        }
                        if (pText.length() > 0) {
                            extracted.append(pText).append("\n\n");
                        }
                    }
                    break;
                }
            }
        } catch (IOException e) {
            extracted.append("Error parsing DOCX file: ").append(e.getMessage());
        }

        String content = extracted.length() > 0 ? extracted.toString().trim() : "Empty DOCX document.";
        String summary = "Microsoft Word (DOCX) document extracted text: " + Math.min(100, content.length()) + " chars preview.";
        return new ParsedDocument(fileName, AttachmentType.DOCX, bytes.length, content, summary);
    }

    private ParsedDocument parseXlsx(String fileName, byte[] bytes) {
        StringBuilder extracted = new StringBuilder();
        List<String> sharedStrings = new ArrayList<>();
        byte[] sheetBytes = null;

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if ("xl/sharedStrings.xml".equalsIgnoreCase(entry.getName())) {
                    String ssXml = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                    Matcher tMatcher = Pattern.compile("<t[^>]*>(.*?)</t>").matcher(ssXml);
                    while (tMatcher.find()) {
                        sharedStrings.add(tMatcher.group(1));
                    }
                } else if ("xl/worksheets/sheet1.xml".equalsIgnoreCase(entry.getName())) {
                    sheetBytes = zis.readAllBytes();
                }
            }
        } catch (IOException ignored) {}

        if (sheetBytes != null) {
            String sheetXml = new String(sheetBytes, StandardCharsets.UTF_8);
            Matcher rowMatcher = Pattern.compile("<row[^>]*>(.*?)</row>").matcher(sheetXml);
            int rowCount = 0;
            while (rowMatcher.find() && rowCount < 50) {
                String rowData = rowMatcher.group(1);
                Matcher cMatcher = Pattern.compile("<c([^>]*)>(.*?)</c>").matcher(rowData);
                List<String> cells = new ArrayList<>();
                while (cMatcher.find()) {
                    String cAttrs = cMatcher.group(1);
                    String cBody = cMatcher.group(2);
                    boolean isShared = cAttrs.contains("t=\"s\"");
                    Matcher vMatcher = Pattern.compile("<v>([0-9.]+)</v>").matcher(cBody);
                    if (vMatcher.find()) {
                        String val = vMatcher.group(1);
                        if (isShared) {
                            try {
                                int idx = Integer.parseInt(val);
                                cells.add(idx < sharedStrings.size() ? sharedStrings.get(idx) : val);
                            } catch (NumberFormatException e) {
                                cells.add(val);
                            }
                        } else {
                            cells.add(val);
                        }
                    } else {
                        Matcher tMatcher = Pattern.compile("<t[^>]*>(.*?)</t>").matcher(cBody);
                        if (tMatcher.find()) {
                            cells.add(tMatcher.group(1));
                        }
                    }
                }
                if (!cells.isEmpty()) {
                    extracted.append("| ").append(String.join(" | ", cells)).append(" |\n");
                    if (rowCount == 0) {
                        extracted.append("| ").append(String.join(" | ", Collections.nCopies(cells.size(), "---"))).append(" |\n");
                    }
                    rowCount++;
                }
            }
        }

        String content = extracted.length() > 0 ? extracted.toString().trim() : "Empty or non-standard XLSX spreadsheet.";
        String summary = "Excel (XLSX) workbook tabular representation preview.";
        return new ParsedDocument(fileName, AttachmentType.XLSX, bytes.length, content, summary);
    }

    private ParsedDocument parseZip(String fileName, byte[] bytes) {
        StringBuilder tree = new StringBuilder("# Project ZIP Archive Structure\n\n");
        StringBuilder sampledCode = new StringBuilder("\n## Key Source Files\n\n");
        int fileCount = 0;

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String path = entry.getName();
                tree.append("- `").append(path).append("` (").append(entry.getSize() > 0 ? entry.getSize() : "unknown").append(" bytes)\n");
                fileCount++;

                String ext = getExtension(path.toLowerCase(Locale.ROOT));
                if (RECOGNIZED_CODE_EXT.contains(ext) && fileCount <= 10) {
                    byte[] entryBytes = zis.readAllBytes();
                    String codeSnippet = new String(entryBytes, StandardCharsets.UTF_8);
                    if (codeSnippet.length() > 2000) {
                        codeSnippet = codeSnippet.substring(0, 2000) + "\n... [truncated for context window]";
                    }
                    sampledCode.append("### `").append(path).append("`\n```").append(ext).append("\n").append(codeSnippet).append("\n```\n\n");
                }
            }
        } catch (IOException e) {
            tree.append("Error unpacking ZIP archive: ").append(e.getMessage());
        }

        String content = tree.toString() + sampledCode.toString();
        String summary = String.format("ZIP Project Archive containing %d files.", fileCount);
        return new ParsedDocument(fileName, AttachmentType.ZIP, bytes.length, content, summary);
    }

    private ParsedDocument parsePdf(String fileName, byte[] bytes) {
        String raw = new String(bytes, StandardCharsets.ISO_8859_1);
        StringBuilder extracted = new StringBuilder();
        Matcher streamMatcher = Pattern.compile("stream[\\r\\n]+([\\s\\S]*?)[\\r\\n]+endstream").matcher(raw);
        while (streamMatcher.find() && extracted.length() < 10000) {
            String block = streamMatcher.group(1);
            Matcher textMatcher = Pattern.compile("\\(([^\\)]+)\\)\\s*Tj").matcher(block);
            while (textMatcher.find()) {
                extracted.append(textMatcher.group(1)).append(" ");
            }
        }

        String content = extracted.length() > 0
                ? extracted.toString().trim()
                : "PDF Binary Document (" + bytes.length + " bytes). Direct text stream parsed.";
        String summary = "PDF Document with " + bytes.length + " bytes.";
        return new ParsedDocument(fileName, AttachmentType.PDF, bytes.length, content, summary);
    }

    private ParsedDocument parseImage(String fileName, byte[] bytes) {
        String base64 = Base64.getEncoder().encodeToString(bytes);
        String ext = getExtension(fileName.toLowerCase(Locale.ROOT));
        String dataUrl = "data:image/" + ext + ";base64," + base64;
        String summary = String.format("Image (%s) with %d bytes prepared for multimodal vision analysis.", ext.toUpperCase(), bytes.length);
        return new ParsedDocument(fileName, AttachmentType.IMAGE, bytes.length, dataUrl, summary);
    }

    private String getExtension(String name) {
        int dot = name.lastIndexOf('.');
        return (dot >= 0 && dot < name.length() - 1) ? name.substring(dot + 1) : "";
    }
}


