package com.endside.config.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
@SuppressWarnings("ALL")
@Slf4j
public class ExceptionHandlerFilter extends OncePerRequestFilter {
    @SuppressWarnings("NullableProblems")
    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            setErrorResponse(response, e);
        }
    }

    public void setErrorResponse(HttpServletResponse response, Throwable ex){
        response.setContentType("application/json");
        // A class used for errors
        //ApiError apiError = new ApiError(status, ex);
        try {
            String json = "apiError.convertToJson()";
            System.out.println(json);
            response.getWriter().write(json);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}