package com.tuantoquq.forwardmail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.playwright.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ForwardMailService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mail.server}")
    private String mailServer;

    @Value("${api.base-url}")
    private String apiGmailMMO;
    public void forwardMail(ForwardRequest request) throws JsonProcessingException {
        try (Playwright playwright = Playwright.create()) {
            BrowserType browserType = playwright.firefox();
            BrowserType.LaunchOptions options = new BrowserType.LaunchOptions();
            options.setHeadless(false);
            try (Browser browser = browserType.launch(options)) {
                BrowserContext context = browser.newContext();
                Page page = context.newPage();
                page.navigate("http://mail.google.com/");
                page.locator("#identifierId").fill(request.getEmail());
                page.locator("#identifierNext").click();
                page.locator("#password input").fill(request.getPassword());
                page.locator("#passwordNext").click();
                page.waitForTimeout(2000);
                page.navigate("https://mail.google.com/mail/u/0/#settings/fwdandpop");
                page.locator("input[act=\"add\"]").click();
                page.locator(".PN input").fill(mailServer);
                Page popup = page.waitForPopup(() -> {
                    page.locator("button[name=\"next\"]").click();
                });
                popup.waitForLoadState();
                popup.locator("input[value=\"Proceed\"]").click();
                page.locator("button[name=\"ok\"]").click();
                String code = getCodeForward(request.getEmail());
                page.locator("input[act=\"verifyText\"]").fill(code);
                page.locator("input[name=\"verify\"]").click();
                page.waitForTimeout(500);
                page.locator("input[value=\"1\"]").first().click();
                page.locator("button[guidedhelpid=\"save_changes_button\"]").click();
                page.waitForTimeout(1000);
            }
        }
    }
    private String getCodeForward(String email) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        String requestUrl = apiGmailMMO + "rent-mails/internal/otp-forward?email="+email;
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.GET, entity, String.class);
        System.out.println("Get otp forward: " + response.getBody());
        return JsonUtils.get(response.getBody(), "otp");
    }
    public String updateMailForwardStatus(String email) {
        HttpHeaders headers = new HttpHeaders();
        String requestUrl = apiGmailMMO + "mails/internal/update-forward-status";
        headers.setContentType(MediaType.APPLICATION_JSON);
        UpdateForwardStatusRequest request = new UpdateForwardStatusRequest(email);
        HttpEntity<UpdateForwardStatusRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, entity, String.class);
        return response.getBody();
    }
}
