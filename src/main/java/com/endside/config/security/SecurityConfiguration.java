package com.endside.config.security;

import com.endside.social.service.GoogleVerificationService;
import com.endside.social.service.SocialValidationService;
import com.endside.user.service.JwtAuthenticationService;
import com.endside.user.service.LoginAttemptService;
import com.endside.user.service.LoginSuccessAfterService;
import com.endside.user.service.SocialUserAuthenticationService;
import com.endside.social.service.TossVerificationService;
import com.endside.util.WebUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.util.Arrays;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationService jwtAuthenticationService;
    private final LoginAttemptService loginAttemptService;
    private final LoginSuccessAfterService loginSuccessAfterService;
    private final SocialUserAuthenticationService socialUserAuthenticationService;
    private final GoogleVerificationService googleVerificationService;
    private final Environment env;
    private final WebUtil webUtil;
    private final ApplicationEventPublisher applicationEventPublisher;

    private final TossVerificationService tossVerificationService;

    @Value("${jwt.secret}")
    private String secret;

    private final SocialValidationService socialValidationService;

    public SecurityConfiguration(UserDetailsService userDetailsService, JwtAuthenticationService jwtAuthenticationService,
                                 LoginAttemptService loginAttemptService, LoginSuccessAfterService loginSuccessAfterService,
                                 SocialUserAuthenticationService socialUserAuthenticationService,
                                 GoogleVerificationService googleVerificationService,
                                 WebUtil webUtil, Environment env,
                                 SocialValidationService socialValidationService,
                                 @Autowired ApplicationEventPublisher applicationEventPublisher, TossVerificationService tossVerificationService) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationService = jwtAuthenticationService;
        this.loginAttemptService = loginAttemptService;
        this.loginSuccessAfterService = loginSuccessAfterService;
        this.socialUserAuthenticationService = socialUserAuthenticationService;
        this.googleVerificationService = googleVerificationService;
        this.webUtil = webUtil;
        this.env = env;
        this.socialValidationService = socialValidationService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.tossVerificationService = tossVerificationService;
    }

    private void printEnvActiveProfile() {
        String[] profiles = env.getActiveProfiles();
        log.info("========================== Profile Info start ==========================");
        Arrays.stream(profiles).forEach(log::info);
        if (profiles.length == 0) {
            String[] dProfiles = env.getDefaultProfiles();
            Arrays.stream(dProfiles).forEach(log::info);
        }
        log.info("========================== Profile Info end==========================");
    }


    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationEventPublisher(defaultAuthenticationEventPublisher(applicationEventPublisher));
        http.authenticationManager(authenticationManagerBuilder.eraseCredentials(true).build());
        authenticationManagerBuilder.authenticationProvider(authenticationProvider());

        http
                // remove csrf and state in session because in jwt we do not need them
                .csrf(CsrfConfigurer::disable)
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // add jwt filters (1. authentication, 2. authorization)
                .addFilter(new JwtAuthenticationFilter(authenticationManagerBuilder.getObject(), loginAttemptService, jwtAuthenticationService, loginSuccessAfterService, webUtil))
                .addFilter(new JwtAuthorizationFilter(authenticationManagerBuilder.getObject(), jwtAuthenticationService, secret))
                .addFilter(new SocialAuthenticationFilter(authenticationManagerBuilder.getObject(), socialUserAuthenticationService, jwtAuthenticationService, loginSuccessAfterService, webUtil,
                        googleVerificationService, socialValidationService, tossVerificationService))
                .authorizeHttpRequests(authorize  -> authorize
                    .requestMatchers(// configure access rules
                            "/", "/error", "/401", "/403", "/404", "/hello", "/auth/hello", "/auth/hello/log", "/auth/now"// MAIN
                            , "/auth/join/**", "/auth/join", "/auth/token/refresh", "/auth/v1/email/available" , "/auth/v1/id/available"
                            , "/auth/test/**", "/auth/error"
                            , "/auth/v1/email/otp/**", "/auth/v1/sms/otp/**", "/auth/v1/user/password/noauth/**"
                    ).permitAll()
                    .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                    .anyRequest().authenticated()
                )
                .exceptionHandling(httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer.accessDeniedHandler(accessDeniedHandler()));

        return http.build();
    }

    @Bean
    CustomUserDetailsAuthenticationProvider authenticationProvider() {
        CustomUserDetailsAuthenticationProvider customUserDetailsAuthenticationProvider = new CustomUserDetailsAuthenticationProvider();
        customUserDetailsAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        customUserDetailsAuthenticationProvider.setUserDetailsService(this.userDetailsService);
        return customUserDetailsAuthenticationProvider;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    @Bean
    public DefaultAuthenticationEventPublisher defaultAuthenticationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return new DefaultAuthenticationEventPublisher(applicationEventPublisher);
    }
}