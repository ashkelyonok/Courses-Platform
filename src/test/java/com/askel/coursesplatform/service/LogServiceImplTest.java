package com.askel.coursesplatform.service;

import com.askel.coursesplatform.exception.InvalidInputException;
import com.askel.coursesplatform.exception.LoggingException;
import com.askel.coursesplatform.exception.ResourceNotFoundException;
import com.askel.coursesplatform.service.impl.LogServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogServiceImplTest {

    @Mock
    private BufferedReader reader;

    @InjectMocks
    private LogServiceImpl logService;

    @Test
    void getLogFileForDate_withValidDateAndLogs_returnsResource() throws IOException {
        // Arrange
        String date = "15.01.2023";
        Path logFilePath = Paths.get("logs", "app.log");
        List<String> logLines = List.of(
                "2023-01-15: Log entry 1",
                "2023-01-16: Log entry 2",
                "2023-01-15: Log entry 3"
        );
        try (var pathsMock = mockStatic(Paths.class); var filesMock = mockStatic(Files.class)) {
            pathsMock.when(() -> Paths.get("logs", "app.log")).thenReturn(logFilePath);
            filesMock.when(() -> Files.exists(logFilePath)).thenReturn(true);
            filesMock.when(() -> Files.newBufferedReader(eq(logFilePath), any())).thenReturn(reader);
            when(reader.readLine()).thenReturn(
                    logLines.get(0),
                    logLines.get(1),
                    logLines.get(2),
                    null
            );

            // Act
            Resource result = logService.getLogFileForDate(date);

            // Assert
            assertNotNull(result);
            assertInstanceOf(ByteArrayResource.class, result);
            byte[] content = ((ByteArrayResource) result).getByteArray();
            String contentStr = new String(content);
            assertTrue(contentStr.contains("2023-01-15: Log entry 1"));
            assertTrue(contentStr.contains("2023-01-15: Log entry 3"));
            assertFalse(contentStr.contains("2023-01-16"));
            filesMock.verify(() -> Files.exists(logFilePath));
            filesMock.verify(() -> Files.newBufferedReader(eq(logFilePath), any()));
            verify(reader, times(4)).readLine();
        }
    }

    @Test
    void getLogFileForDate_fileNotExists_throwsResourceNotFoundException() {
        // Arrange
        String date = "15.01.2023";
        Path logFilePath = Paths.get("logs", "app.log");
        try (var pathsMock = mockStatic(Paths.class); var filesMock = mockStatic(Files.class)) {
            pathsMock.when(() -> Paths.get("logs", "app.log")).thenReturn(logFilePath);
            filesMock.when(() -> Files.exists(logFilePath)).thenReturn(false);

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> logService.getLogFileForDate(date)
            );
            assertEquals("Log file does not exist.", exception.getMessage());
            filesMock.verify(() -> Files.exists(logFilePath));
        }
    }

    @Test
    void getLogFileForDate_noLogsForDate_throwsResourceNotFoundException() throws IOException {
        // Arrange
        String date = "15.01.2023";
        Path logFilePath = Paths.get("logs", "app.log");
        try (var pathsMock = mockStatic(Paths.class); var filesMock = mockStatic(Files.class)) {
            pathsMock.when(() -> Paths.get("logs", "app.log")).thenReturn(logFilePath);
            filesMock.when(() -> Files.exists(logFilePath)).thenReturn(true);
            filesMock.when(() -> Files.newBufferedReader(eq(logFilePath), any())).thenReturn(reader);
            when(reader.readLine()).thenReturn("2023-01-16: Log entry").thenReturn(null);

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> logService.getLogFileForDate(date)
            );
            assertEquals("No logs found for this date.", exception.getMessage());
            filesMock.verify(() -> Files.exists(logFilePath));
            filesMock.verify(() -> Files.newBufferedReader(eq(logFilePath), any()));
            verify(reader, times(2)).readLine();
        }
    }

    @Test
    void getLogFileForDate_ioException_throwsLoggingException() {
        // Arrange
        String date = "15.01.2023";
        Path logFilePath = Paths.get("logs", "app.log");
        try (var pathsMock = mockStatic(Paths.class); var filesMock = mockStatic(Files.class)) {
            pathsMock.when(() -> Paths.get("logs", "app.log")).thenReturn(logFilePath);
            filesMock.when(() -> Files.exists(logFilePath)).thenReturn(true);
            filesMock.when(() -> Files.newBufferedReader(eq(logFilePath), any())).thenThrow(new IOException("IO error"));

            // Act & Assert
            LoggingException exception = assertThrows(
                    LoggingException.class,
                    () -> logService.getLogFileForDate(date)
            );
            assertEquals("Error reading log file", exception.getMessage());
            filesMock.verify(() -> Files.exists(logFilePath));
            filesMock.verify(() -> Files.newBufferedReader(eq(logFilePath), any()));
        }
    }

    @Test
    void getLogFileForDate_invalidDateFormat_throwsInvalidInputException() {
        // Arrange
        String date = "2023-01-15";

        // Act & Assert
        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> logService.getLogFileForDate(date)
        );
        assertEquals("Incorrect date format. Use dd.MM.yyyy.", exception.getMessage());
        verifyNoInteractions(reader);
    }

    @Test
    void getDownloadFileName_withValidDate_returnsFileName() {
        // Arrange
        String date = "15.01.2023";

        // Act
        String result = logService.getDownloadFileName(date);

        // Assert
        assertEquals("app-2023-01-15.log", result);
        verifyNoInteractions(reader);
    }

    @Test
    void getDownloadFileName_invalidDateFormat_throwsInvalidInputException() {
        // Arrange
        String date = "2023-01-15";

        // Act & Assert
        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> logService.getDownloadFileName(date)
        );
        assertEquals("Incorrect date format. Use dd.MM.yyyy.", exception.getMessage());
        verifyNoInteractions(reader);
    }
}