package com.askel.coursesplatform.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Course {
    private Long id;
    private String name;
    private String description;
}
