package com.mvmlabs.web;

import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.Builder;
import lombok.Data;

@RestController
public class HealthcheckController {

    @Data
    @Builder
    private static class HealthcheckResponse {
        private int    status;
        private Date   timestamp;
        private String message;
    }

    @RequestMapping(value = "/health", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_UTF8_VALUE })
    public HealthcheckResponse hc() {
        return HealthcheckResponse.builder().status(HttpStatus.OK.value()).timestamp(new Date()).message("OK").build();
    }
}
