package com.tuantoquq.forwardmail;

import com.microsoft.playwright.PlaywrightException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/forward-mail")
public class ForwardMailController {
    private final Logger logger = LoggerFactory.getLogger(ForwardMailController.class);
    private final ForwardMailService service;
    private final Bucket bucket;
    @Autowired
    public ForwardMailController(ForwardMailService service){
        this.service = service;
        Bandwidth limit = Bandwidth.classic(30, Refill.greedy(30, Duration.ofMinutes(1)));
        this.bucket = Bucket4j.builder()
                .addLimit(limit)
                .build();
    }
    @PostMapping(path = "/process")
    public ResponseEntity<String> forward(@RequestBody ForwardRequest request){
        String svResponse;
        if(bucket.tryConsume(1)){
            try {
                service.forwardMail(request);
                svResponse = "successfully!";
                String updateForwardLog = service.updateMailForwardStatus(request.getEmail());
                logger.info("Update forward status logs: " + updateForwardLog);
                return ResponseEntity.ok(svResponse);
            } catch (PlaywrightException ex) {
                ex.printStackTrace();
                logger.error("Error when using playwright: " + ex.getMessage());
                svResponse = ex.getMessage();
                return ResponseEntity.badRequest().body(svResponse);
            } catch (Exception e){
                e.printStackTrace();
                logger.error("Error: " + e.getMessage());
                svResponse = e.getMessage();
                return ResponseEntity.internalServerError().body(svResponse);
            }
        }else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests!");
        }
    }
}
