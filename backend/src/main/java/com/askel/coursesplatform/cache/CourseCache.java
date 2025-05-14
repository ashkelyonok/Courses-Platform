package com.askel.coursesplatform.cache;

import com.askel.coursesplatform.model.entity.Course;
import org.springframework.stereotype.Component;

@Component
public class CourseCache extends LfuCache<Course> {

    public CourseCache() {
        super(100);
    }
}
