package com.askel.coursesplatform.service.impl;

import com.askel.coursesplatform.exception.InvalidInputException;
import com.askel.coursesplatform.exception.ResourceNotFoundException;
import com.askel.coursesplatform.model.entity.LogObj;
import com.askel.coursesplatform.service.LogService;
import com.askel.coursesplatform.utils.ErrorMessages;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class LogServiceImpl implements LogService {


    private static final String LOG_FILE_PATH = "logs/app.log";
    private static final DateTimeFormatter INPUT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter LOG_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AtomicLong idCounter = new AtomicLong(1);
    private final Map<Long, LogObj> tasks = new ConcurrentHashMap<>();
    private final LogServiceImpl self;

    public LogServiceImpl(@Lazy LogServiceImpl self) {
        this.self = self;
    }

    @Async("executor")
    public void createLogs(Long taskId, String date) {
        try {
            Thread.sleep(10000);

            LocalDate logDate = parseDate(date);
            String formattedDate = logDate.format(LOG_DATE_FORMATTER);

            Path path = Paths.get(LOG_FILE_PATH);
            if (!Files.exists(path)) {
                LogObj logObject = tasks.get(taskId);
                if (logObject != null) {
                    logObject.setStatus("FAILED");
                    logObject.setErrorMessage("Log file does not exist: " + LOG_FILE_PATH);
                }
                throw new ResourceNotFoundException("Log file does not exist: " + LOG_FILE_PATH);
            }

            List<String> logLines = Files.readAllLines(path);
            List<String> currentLogs = logLines.stream()
                    .filter(line -> line.startsWith(formattedDate))
                    .toList();

            if (currentLogs.isEmpty()) {
                LogObj logObject = tasks.get(taskId);
                if (logObject != null) {
                    logObject.setStatus("FAILED");
                    logObject.setErrorMessage("No logs found for date: " + formattedDate);
                }
                throw new ResourceNotFoundException("No logs found for date: " + formattedDate);
            }

            Path logFile;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                logFile = Files.createTempFile("logs-" + formattedDate, ".log");
            } else {
                FileAttribute<Set<PosixFilePermission>> attr =
                        PosixFilePermissions.asFileAttribute(
                                PosixFilePermissions.fromString("rwx------")
                        );
                logFile = Files.createTempFile("logs-" + formattedDate, ".log", attr);
            }

            Files.write(logFile, currentLogs);
            logFile.toFile().deleteOnExit();

            LogObj task = tasks.get(taskId);
            if (task != null) {
                task.setStatus("COMPLETED");
                task.setFilePath(logFile.toString());
            }
        } catch (IOException e) {
            LogObj task = tasks.get(taskId);
            if (task != null) {
                task.setStatus("FAILED");
                task.setErrorMessage(e.getMessage());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public Long createLogAsync(String date) {
        Long id = idCounter.getAndIncrement();
        LogObj logObject = new LogObj(id, "IN_PROGRESS");
        tasks.put(id, logObject);
        self.createLogs(id, date);
        return id;
    }

    @Override
    public LogObj getStatus(Long id) {
        LogObj logObject = tasks.get(id);
        if (logObject == null) {
            throw new ResourceNotFoundException("Log task not found: " + id);
        }
        return logObject;
    }

    @Override
    public ResponseEntity<Resource> downloadCreatedLogs(Long taskId) throws IOException {
        LogObj logObject = getStatus(taskId);
        if (logObject == null) {
            throw new EntityNotFoundException(ErrorMessages.FILE_NOT_FOUND);
        }
        if (!"COMPLETED".equals(logObject.getStatus())) {
            throw new IllegalStateException("The logs are not ready yet");
        }

        Path path = Paths.get(logObject.getFilePath());
        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("Log file not found: " + logObject.getFilePath());
        }

        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date, INPUT_DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new InvalidInputException("Incorrect date format. Use dd.MM.yyyy.");
        }
    }
}
