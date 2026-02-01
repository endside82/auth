package com.endside.user.service.join;

import com.endside.user.vo.UserSimpleVo;
import com.endside.user.param.UserJoinParam;

public interface UserJoinService {
    UserSimpleVo joinUser(UserJoinParam userJoinParam);
}
