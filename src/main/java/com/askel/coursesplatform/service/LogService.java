package com.askel.coursesplatform.service;

import com.askel.coursesplatform.exception.InvalidInputException;
import com.askel.coursesplatform.exception.LoggingException;
import com.askel.coursesplatform.exception.ResourceNotFoundException;
import com.askel.coursesplatform.model.entity.LogObj;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface LogService {
    Resource getLogFileForDate(String date) throws ResourceNotFoundException, LoggingException;

    String getDownloadFileName(String date) throws InvalidInputException;

    Long createLogAssync(String date);

    LogObj getStatus(Long id);

    ResponseEntity<Resource> downloadCreatedLogs(Long taskId) throws IOException;
}
