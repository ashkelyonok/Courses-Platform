package com.askel.coursesplatform.service.impl;

import com.askel.coursesplatform.exception.InvalidInputException;
import com.askel.coursesplatform.exception.LoggingException;
import com.askel.coursesplatform.exception.ResourceNotFoundException;
import com.askel.coursesplatform.model.entity.LogObj;
import com.askel.coursesplatform.service.LogService;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public class LogServiceImpl implements LogService {


    private static final String LOGS_DIR = "logs";
    private static final DateTimeFormatter INPUT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter LOG_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Resource getLogFileForDate(String date) {
        LocalDate parsedDate = parseDate(date);
        String formattedDate = parsedDate.format(LOG_DATE_FORMATTER);

        Path logFilePath = Paths.get(LOGS_DIR, "app.log");
        if (!Files.exists(logFilePath)) {
            throw new ResourceNotFoundException("Log file does not exist.");
        }

        List<String> filteredLines = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(logFilePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(formattedDate)) {
                    filteredLines.add(line);
                }
            }
        } catch (IOException ex) {
            throw new LoggingException("Error reading log file");
        }

        if (filteredLines.isEmpty()) {
            throw new ResourceNotFoundException("No logs found for this date.");
        }

        String fileContent = String.join(System.lineSeparator(), filteredLines);
        return new ByteArrayResource(fileContent.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getDownloadFileName(String date) {
        LocalDate parsedDate = parseDate(date);
        return String.format("app-%s.log", parsedDate.format(LOG_DATE_FORMATTER));
    }

    @Override
    public Long createLogAssync(String date) {
        return 0L;
    }

    @Override
    public LogObj getStatus(Long id) {
        return null;
    }

    @Override
    public ResponseEntity<Resource> downloadCreatedLogs(Long taskId) throws IOException {
        return null;
    }

    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date, INPUT_DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new InvalidInputException("Incorrect date format. Use dd.MM.yyyy.");
        }
    }
}
