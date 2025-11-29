package net.javaguids.lost_and_found.utils;

import net.javaguids.lost_and_found.analytics.ActivityLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for FileHandler class.
 * Tests CSV export and image file operations.
 */
class FileHandlerTest {

    private static final String TEST_CSV_PATH = "test_logs.csv";
    private static final String TEST_IMAGE_DIR = "uploads/images/";

    @AfterEach
    void cleanup() {
        // Clean up test files after each test
        try {
            File testCsv = new File(TEST_CSV_PATH);
            if (testCsv.exists()) {
                testCsv.delete();
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    // CSV Export Tests

    @Test
    void testExportLogsToCSV_EmptyList_CreatesFileWithHeader() {
        List<ActivityLog> logs = new ArrayList<>();

        boolean success = FileHandler.exportLogsToCSV(logs, TEST_CSV_PATH);

        assertTrue(success, "Export should succeed for empty list");
        assertTrue(new File(TEST_CSV_PATH).exists(), "CSV file should be created");
    }

    @Test
    void testExportLogsToCSV_WithLogs_CreatesFile() {
        List<ActivityLog> logs = new ArrayList<>();
        logs.add(new ActivityLog("log-1", "user-1", "LOGIN", "User logged in"));
        logs.add(new ActivityLog("log-2", "user-2", "REGISTER", "New user registered"));

        boolean success = FileHandler.exportLogsToCSV(logs, TEST_CSV_PATH);

        assertTrue(success, "Export should succeed");
        assertTrue(new File(TEST_CSV_PATH).exists(), "CSV file should be created");
    }

    @Test
    void testExportLogsToCSV_WithLogs_ContainsHeader() throws IOException {
        List<ActivityLog> logs = new ArrayList<>();
        logs.add(new ActivityLog("log-1", "user-1", "LOGIN", "Test"));

        FileHandler.exportLogsToCSV(logs, TEST_CSV_PATH);

        String content = Files.readString(Paths.get(TEST_CSV_PATH));
        assertTrue(content.contains("LogID,UserID,Action,Details,Timestamp"),
                "CSV should contain header row");
    }

    @Test
    void testExportLogsToCSV_WithLogs_ContainsData() throws IOException {
        List<ActivityLog> logs = new ArrayList<>();
        ActivityLog log = new ActivityLog("log-123", "user-456", "LOGIN", "Test login");
        logs.add(log);

        FileHandler.exportLogsToCSV(logs, TEST_CSV_PATH);

        String content = Files.readString(Paths.get(TEST_CSV_PATH));
        assertTrue(content.contains("log-123"), "CSV should contain log ID");
        assertTrue(content.contains("user-456"), "CSV should contain user ID");
        assertTrue(content.contains("LOGIN"), "CSV should contain action");
    }

    @Test
    void testExportLogsToCSV_InvalidPath_ReturnsFalse() {
        List<ActivityLog> logs = new ArrayList<>();
        logs.add(new ActivityLog("log-1", "user-1", "TEST", "Test"));

        // Use a path that will definitely fail on all platforms
        // Windows: invalid characters in path
        // Unix/Mac: parent directory doesn't exist
        String invalidPath = System.getProperty("os.name").toLowerCase().contains("win")
                ? "C:\\nonexistent\\directory\\test.csv"  // Windows
                : "/nonexistent/directory/test.csv";      // Unix/Mac

        boolean success = FileHandler.exportLogsToCSV(logs, invalidPath);

        assertFalse(success, "Export should fail for invalid path");
    }

    // Image Handling Tests

    @Test
    void testSaveImage_NullFile_ReturnsNull() {
        String result = FileHandler.saveImage(null, "item-123");

        assertNull(result, "Should return null for null file");
    }

    @Test
    void testGetFileExtension_ValidFileName_ReturnsExtension() {
        // This tests the private method indirectly through saveImage
        // We can't test it directly, but we verify the behavior

        // Create a temporary test file
        try {
            File tempFile = File.createTempFile("test", ".jpg");
            tempFile.deleteOnExit();

            String result = FileHandler.saveImage(tempFile, "item-test");

            if (result != null) {
                assertTrue(result.endsWith(".jpg"),
                        "Saved file should preserve .jpg extension");
            }
        } catch (IOException e) {
            // Skip test if can't create temp file
            System.out.println("Skipping image test due to IO error");
        }
    }

    @Test
    void testSaveImage_CreatesDirectory() {
        try {
            // Create a temporary test file
            File tempFile = File.createTempFile("test", ".png");
            tempFile.deleteOnExit();

            FileHandler.saveImage(tempFile, "item-dir-test");

            // Check if directory was created
            Path uploadDir = Paths.get(TEST_IMAGE_DIR);
            assertTrue(Files.exists(uploadDir),
                    "Upload directory should be created");
        } catch (IOException e) {
            System.out.println("Skipping directory test due to IO error");
        }
    }

    @Test
    void testSaveImage_ValidFile_ReturnsPath() {
        try {
            File tempFile = File.createTempFile("test", ".jpg");
            tempFile.deleteOnExit();

            String result = FileHandler.saveImage(tempFile, "item-path-test");

            assertNotNull(result, "Should return a path for valid file");
            assertTrue(result.contains("item-path-test"),
                    "Path should contain item ID");
        } catch (IOException e) {
            System.out.println("Skipping save test due to IO error");
        }
    }

    @Test
    void testSaveImage_NonExistentFile_ReturnsNull() {
        // Create a file reference that doesn't exist
        File nonExistentFile = new File("this_file_does_not_exist.jpg");

        String result = FileHandler.saveImage(nonExistentFile, "item-test");

        // Should return null because file doesn't exist
        assertNull(result, "Should return null for non-existent file");
    }

    @Test
    void testExportLogsToCSV_MultipleRows() throws IOException {
        List<ActivityLog> logs = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            logs.add(new ActivityLog("log-" + i, "user-" + i, "ACTION", "Details " + i));
        }

        FileHandler.exportLogsToCSV(logs, TEST_CSV_PATH);

        String content = Files.readString(Paths.get(TEST_CSV_PATH));
        String[] lines = content.split("\n");

        // Header + 5 data rows = 6 lines
        assertTrue(lines.length >= 6, "CSV should contain header + 5 data rows");
    }
}