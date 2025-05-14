package com.askel.coursesplatform.model.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogObj {

    private Long id;
    private String status;
    private String filePath;
    private String errorMessage;

    public LogObj(Long id, String status) {
        this.id = id;
        this.status = status;
    }
}
