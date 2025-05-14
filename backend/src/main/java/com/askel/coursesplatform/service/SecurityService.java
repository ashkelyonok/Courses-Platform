package com.askel.coursesplatform.service;

import com.askel.coursesplatform.model.entity.User;

public interface SecurityService {
    boolean isSelf(Long userId);

    boolean isCourseInstructor(Long courseId);

    User getCurrentUser();
}
