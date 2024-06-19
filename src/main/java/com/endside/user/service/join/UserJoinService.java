package com.endside.user.service.join;

import com.endside.user.model.UserSimple;
import com.endside.user.param.UserJoinParam;

public interface UserJoinService {
    UserSimple joinUser(UserJoinParam userJoinParam);
}
