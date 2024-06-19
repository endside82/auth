package com.endside.main;

import com.endside.main.model.Now;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Slf4j
@RestController
public class MainController {

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public void ping() {}

    @GetMapping("/auth/hello")
    @ResponseBody
    public ResponseEntity<?> hello(){
        return ResponseEntity.ok("HELLO!");
    }

    @GetMapping("/auth/hello/log")
    @ResponseBody
    public ResponseEntity<?> helloLog(){
        log.info("HELLO!");
        return ResponseEntity.ok("HELLO!");
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public void root() {}

    @GetMapping("/auth/now")
    @ResponseBody
    public ResponseEntity<?> now(){
        return ResponseEntity.ok(new Now(Instant.now().toEpochMilli()));
    }

}
