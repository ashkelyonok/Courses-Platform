package com.askel.coursesplatform.cache;

import com.askel.coursesplatform.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserCache extends LfuCache<User> {

    public UserCache() {
        super(4);
    }
}
