package net.javaguids.lost_and_found.utils;

import net.javaguids.lost_and_found.analytics.ActivityLog;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Utility class for file operations including CSV export and image handling.
public class FileHandler {

    // Directory path for storing uploaded images */
    private static final String IMAGE_DIRECTORY = "uploads/images/";

    // Date formatter for CSV timestamp exports */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Exports a list of activity logs to a CSV file. Creates a CSV with columns: LogID, UserID, Action, Details, Timestamp.

    public static boolean exportLogsToCSV(List<ActivityLog> logs, String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            // Write CSV header
            bw.write("LogID,UserID,Action,Details,Timestamp\n");

            // Write each log entry as a CSV row
            for (ActivityLog log : logs) {
                StringBuilder line = new StringBuilder();
                line.append(log.getLogId()).append(",");
                line.append(log.getUserId()).append(",");
                line.append(log.getAction()).append(",");
                line.append(log.getDetails()).append(",");
                line.append(log.getTimestamp().format(DATE_FORMATTER));
                line.append("\n");
                bw.write(line.toString());
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Saves an uploaded image file to the application's image directory. Generates a unique filename using itemId and timestamp. Creates the upload directory if it doesn't exist.
    public static String saveImage(File sourceFile, String itemId) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadDir = Paths.get(IMAGE_DIRECTORY);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Generate unique filename with item ID and timestamp
            String fileExtension = getFileExtension(sourceFile.getName());
            String fileName = itemId + "_" + System.currentTimeMillis() + fileExtension;
            Path targetPath = uploadDir.resolve(fileName);

            // Copy file to upload directory
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return targetPath.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Extracts the file extension from a filename.
    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot);
        }
        return "";
    }
}