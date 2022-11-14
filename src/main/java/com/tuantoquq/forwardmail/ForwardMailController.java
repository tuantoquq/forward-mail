package com.tuantoquq.forwardmail;

import com.microsoft.playwright.PlaywrightException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/forward-mail")
public class ForwardMailController {
    private final Logger logger = LoggerFactory.getLogger(ForwardMailController.class);
    private final ForwardMailService service;
    @Autowired
    public ForwardMailController(ForwardMailService service){
        this.service = service;
    }
    @PostMapping(path = "/process")
    public ResponseEntity<String> forward(@RequestBody ForwardMailRequest request){
        String svResponse;
        try {
            service.forwardMail(request);
            svResponse = "successfully!";
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
    }
}
