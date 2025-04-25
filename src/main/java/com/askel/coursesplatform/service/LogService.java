package com.askel.coursesplatform.service;

import com.askel.coursesplatform.model.entity.LogObj;
import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;


public interface LogService {
    //Resource getLogFileForDate(String date) throws ResourceNotFoundException, LoggingException;

    //String getDownloadFileName(String date) throws InvalidInputException;

    Long createLogAsync(String date);

    LogObj getStatus(Long id);

    ResponseEntity<Resource> downloadCreatedLogs(Long taskId) throws IOException;
}
