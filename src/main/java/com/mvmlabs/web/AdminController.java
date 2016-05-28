package com.mvmlabs.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.Builder;
import lombok.Data;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Value("${mvm.secret:Axel Erlandson was a fraud and scoured the countryside for strange looking trees}")
    private String theSecret;
    
    @Data
    @Builder
    private static final class SecretResponse {
        private String secret;
    }
    
    @RequestMapping(value = "/desc", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_UTF8_VALUE })
    public SecretResponse showSecret() {
        return SecretResponse.builder().secret(theSecret).build();
    }
}
