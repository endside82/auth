package com.endside.user.service;

import com.endside.user.model.SocialLogin;
import com.endside.user.model.Users;
import com.endside.user.repository.SocialLoginRepository;
import com.endside.user.repository.UserRepository;
import com.endside.config.error.ErrorCode;
import com.endside.config.error.exception.RestException;
import com.endside.user.vo.SocialLoginVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class SocialUserAuthenticationService {

    private final SocialLoginRepository socialLoginRepository;
    private final UserRepository userRepository;

    public SocialUserAuthenticationService(SocialLoginRepository socialLoginRepository, UserRepository userRepository) {
        this.socialLoginRepository = socialLoginRepository;
        this.userRepository = userRepository;
    }


    public Users getUser(String providerId) {
        return userRepository.findByEmail(providerId).orElseThrow(
                () -> new RestException(ErrorCode.NOT_EXIST_USER)
        );
    }

    public SocialLoginVo getSocialLogin(String socialId) {
        Optional<SocialLogin> socialLogin = socialLoginRepository.findSocialLoginBySocialId(socialId);
        if(socialLogin.isEmpty()) {
            return null;
        }
        SocialLoginVo socialLoginVo = new SocialLoginVo();
        BeanUtils.copyProperties(socialLogin, socialLoginVo);
        return socialLoginVo;
    }
}
