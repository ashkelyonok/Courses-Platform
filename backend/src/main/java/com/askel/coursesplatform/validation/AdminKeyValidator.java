package com.askel.coursesplatform.validation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class AdminKeyValidator {

    @Value("${app.security.admin-key}")
    private String validAdminKey;

    public void validate(String providedKey) {
        if (!validAdminKey.equals(providedKey)) {
            throw new AccessDeniedException("Invalid admin registration key");
        }
    }
}
