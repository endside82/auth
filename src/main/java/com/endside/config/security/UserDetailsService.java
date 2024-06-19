package com.endside.config.security;

import com.endside.user.model.LoginAddInfo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserDetailsService {
    UserDetails loadUserByUsernameAndDomain(String id, LoginAddInfo loginAddInfo) throws UsernameNotFoundException;
}
