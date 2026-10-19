package com.oenexa.application;

import com.oenexa.domain.ConversationCoreDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DocumentIngestionService Zero-Mock Unit Tests")
class DocumentIngestionServiceTest {

    private final DocumentIngestionService service = new DocumentIngestionService();

    @Test
    @DisplayName("Should detect file types based on extension")
    void testDetectType() {
        assertEquals(AttachmentType.PDF, service.detectType("doc.pdf"));
        assertEquals(AttachmentType.DOCX, service.detectType("spec.docx"));
        assertEquals(AttachmentType.XLSX, service.detectType("sheet.xlsx"));
        assertEquals(AttachmentType.CSV, service.detectType("data.csv"));
        assertEquals(AttachmentType.JSON, service.detectType("config.json"));
        assertEquals(AttachmentType.XML, service.detectType("pom.xml"));
        assertEquals(AttachmentType.ZIP, service.detectType("repo.zip"));
        assertEquals(AttachmentType.IMAGE, service.detectType("mockup.png"));
        assertEquals(AttachmentType.CODE, service.detectType("App.java"));
        assertEquals(AttachmentType.CODE, service.detectType("main.py"));
        assertEquals(AttachmentType.TXT, service.detectType("notes.txt"));
    }

    @Test
    @DisplayName("Should parse plaintext and code attachments")
    void testParseTextAndCode() {
        String code = "public class Hello { public static void main(String[] args) {} }";
        ParsedDocument doc = service.parse("Hello.java", code.getBytes(StandardCharsets.UTF_8));
        assertEquals("Hello.java", doc.fileName());
        assertEquals(AttachmentType.CODE, doc.type());
        assertEquals(code, doc.extractedContent());
        assertTrue(doc.tokenCount() > 0);
    }

    @Test
    @DisplayName("Should parse CSV into markdown table")
    void testParseCsv() {
        String csv = "id,name,role\n1,Alice,Engineer\n2,Bob,Architect\n";
        ParsedDocument doc = service.parse("team.csv", csv.getBytes(StandardCharsets.UTF_8));
        assertEquals(AttachmentType.CSV, doc.type());
        assertTrue(doc.extractedContent().contains("| id | name | role |"));
        assertTrue(doc.extractedContent().contains("| 1 | Alice | Engineer |"));
    }

    @Test
    @DisplayName("Should parse JSON content")
    void testParseJson() {
        String json = "{\"name\":\"Oenexa\",\"version\":\"2.0\"}";
        ParsedDocument doc = service.parse("meta.json", json.getBytes(StandardCharsets.UTF_8));
        assertEquals(AttachmentType.JSON, doc.type());
        assertEquals(json, doc.extractedContent());
    }

    @Test
    @DisplayName("Should parse XML content and extract root tags")
    void testParseXml() {
        String xml = "<project><modelVersion>4.0.0</modelVersion><name>Oenexa</name></project>";
        ParsedDocument doc = service.parse("pom.xml", xml.getBytes(StandardCharsets.UTF_8));
        assertEquals(AttachmentType.XML, doc.type());
        assertTrue(doc.summary().contains("project"));
    }

    @Test
    @DisplayName("Should parse in-memory DOCX archive")
    void testParseDocx() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry("word/document.xml"));
            String docXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body><w:p><w:t>OENEXA Architecture Blueprint</w:t></w:p></w:body></w:document>";
            zos.write(docXml.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        ParsedDocument doc = service.parse("blueprint.docx", baos.toByteArray());
        assertEquals(AttachmentType.DOCX, doc.type());
        assertTrue(doc.extractedContent().contains("OENEXA Architecture Blueprint"));
    }

    @Test
    @DisplayName("Should parse in-memory XLSX workbook")
    void testParseXlsx() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry("xl/sharedStrings.xml"));
            String ssXml = "<sst><si><t>Q1 Revenue</t></si><si><t>1000000</t></si></sst>";
            zos.write(ssXml.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));
            String sheetXml = "<worksheet><sheetData><row r=\"1\"><c r=\"A1\" t=\"s\"><v>0</v></c><c r=\"B1\" t=\"s\"><v>1</v></c></row></sheetData></worksheet>";
            zos.write(sheetXml.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        ParsedDocument doc = service.parse("finance.xlsx", baos.toByteArray());
        assertEquals(AttachmentType.XLSX, doc.type());
        assertTrue(doc.extractedContent().contains("Q1 Revenue"));
    }

    @Test
    @DisplayName("Should unpack ZIP archive project and sample code files")
    void testParseZip() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry("src/Main.java"));
            zos.write("class Main {}".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("README.md"));
            zos.write("# Project Docs".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        ParsedDocument doc = service.parse("repo.zip", baos.toByteArray());
        assertEquals(AttachmentType.ZIP, doc.type());
        assertTrue(doc.extractedContent().contains("src/Main.java"));
        assertTrue(doc.extractedContent().contains("README.md"));
        assertTrue(doc.summary().contains("2 files"));
    }

    @Test
    @DisplayName("Should encode Image into base64 Data URL")
    void testParseImage() {
        byte[] fakeImage = new byte[]{(byte) 0x89, 'P', 'N', 'G'};
        ParsedDocument doc = service.parse("diagram.png", fakeImage);
        assertEquals(AttachmentType.IMAGE, doc.type());
        assertTrue(doc.extractedContent().startsWith("data:image/png;base64,"));
    }

    @Test
    @DisplayName("Should handle PDF content gracefully")
    void testParsePdf() {
        byte[] fakePdf = "%PDF-1.4 stream\n(Hello World) Tj\nendstream\n%%EOF".getBytes(StandardCharsets.ISO_8859_1);
        ParsedDocument doc = service.parse("whitepaper.pdf", fakePdf);
        assertEquals(AttachmentType.PDF, doc.type());
        assertTrue(doc.extractedContent().contains("Hello World"));
    }
}
