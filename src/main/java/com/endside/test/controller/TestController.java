package com.endside.test.controller;

import com.endside.config.util.AmazonSmsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;


@Slf4j
@RestController
public class TestController {

    private final AmazonSmsUtil amazonSmsUtil;

    public TestController(AmazonSmsUtil amazonSmsUtil) {
        this.amazonSmsUtil = amazonSmsUtil;
    }

    // auth token refresh
    @RequestMapping(value = {"/auth/test/header"}, method = {RequestMethod.POST})
    @ResponseBody
    public ResponseEntity<String> headerTest(
            @RequestHeader(name = "test") String testString,
            HttpServletResponse response ) {
        // HEADER
        response.addHeader("test", testString);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = {"/auth/test/sms"}, method = {RequestMethod.POST})
    @ResponseBody
    public ResponseEntity<String> smsTest( HttpServletResponse response ) {
        // HEADER
        amazonSmsUtil.pubTextSMS("+8201099467432" , "test message");
        return ResponseEntity.ok().build();
    }



}
