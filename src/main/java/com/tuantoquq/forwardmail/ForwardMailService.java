package com.tuantoquq.forwardmail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.playwright.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ForwardMailService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mail.server}")
    private String mailServer;

    @Value("${api.get-code}")
    private String apiGetCodeUrl;
    public void forwardMail(ForwardMailRequest request) throws JsonProcessingException {
        try (Playwright playwright = Playwright.create()) {
            BrowserType browserType = playwright.firefox();
            BrowserType.LaunchOptions options = new BrowserType.LaunchOptions();
            options.setHeadless(false);
            try (Browser browser = browserType.launch(options)) {
                BrowserContext context = browser.newContext();
                Page page = context.newPage();
                page.navigate("http://mail.google.com/");
                page.locator("#identifierId").fill(request.getEmail());
                page.locator(".VfPpkd-LgbsSe-OWXEXe-k8QpJ > div:nth-child(1)").click();
                page.locator("#password > div:nth-child(1) > div:nth-child(1) > div:nth-child(1) > input:nth-child(1)").fill(request.getPassword());
                page.locator("#passwordNext >> role=button[name=\"Next\"]").click();
                page.waitForTimeout(2000);
                page.navigate("https://mail.google.com/mail/u/0/#settings/fwdandpop");
                page.locator("#\\:3z > input:nth-child(1)").click();
                page.locator("#\\:3s").fill(mailServer);
                Page popup = page.waitForPopup(() -> {
                    page.locator(".J-at1-auR").click();
                });
                popup.waitForLoadState();
                popup.locator("body > form:nth-child(3) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > input:nth-child(3)").click();
                page.locator(".J-at1-auR").click();
//                String code = getCodeForward(request.getEmail());
//                System.out.println("code: " + code);
                page.locator("#\\:4e").fill("111111");
                page.locator("#\\:3y > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(4) > td:nth-child(2) > input:nth-child(2)").click();
                page.waitForTimeout(500);
                page.locator("#\\:3r").click();
                page.locator("#\\:3e").click();
                page.waitForTimeout(1000);
            }
        }
    }
    private String getCodeForward(String email) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        GetCodeForwardRequest request = new GetCodeForwardRequest();
        request.setEmail(email);
        request.setService("forward_mail");
        HttpEntity<GetCodeForwardRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(apiGetCodeUrl, entity, String.class);
        System.out.println("Get otp forward: " + response.getBody());
        return JsonUtils.get(response.getBody(), "otp");
    }
}
