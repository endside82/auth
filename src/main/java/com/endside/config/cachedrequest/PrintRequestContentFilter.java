package com.endside.config.cachedrequest;

import com.endside.config.security.constants.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("ALL")
@Slf4j
@Order
@Component
@Profile({"default","dev"})
@WebFilter(filterName = "printRequestContentFilter", urlPatterns = "/auth/*")
public class PrintRequestContentFilter extends OncePerRequestFilter {
    @SuppressWarnings("NullableProblems")
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        String requestUrl = httpServletRequest.getRequestURI();
        if(!requestUrl.startsWith("/hello")) {
            String authorization = httpServletRequest.getHeader(JwtProperties.HEADER_AUTH);
            log.debug("authorization is: " + authorization);
            log.debug("Request URL is: " + requestUrl);
            InputStream inputStream = httpServletRequest.getInputStream();
            byte[] body = StreamUtils.copyToByteArray(inputStream);
            log.debug("Request body is: " + new String(body));
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}