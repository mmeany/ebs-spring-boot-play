package com.mvmlabs.web;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.Builder;
import lombok.Data;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@Slf4j
public class ApiController {

    private static final class NvpComparator implements Comparator<Nvp> {

        @Override
        public int compare(Nvp o1, Nvp o2) {
            return o1.name.compareTo(o2.name);
        }
        
    }
    
    @Value("${mvm.value:I was not set}")
    private String populateMe;
    
    @Data
    @Builder
    private static final class Nvp {
        private String name;
        private String value;
    }
    
    @Data
    @Builder
    private static final class DescribeResponse {
        private List<Nvp> headers;
        private List<Nvp> cookies;
        private List<Nvp> requestParams;
        private List<Nvp> environment;
        private List<Nvp> systemProperties;
        
        private String populatedProperty;
    }
    
    @RequestMapping(value = "/desc", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_UTF8_VALUE })
    public DescribeResponse describe(HttpServletRequest request) {
        return DescribeResponse.builder()
                .headers(getHeaders(request))
                .cookies(getCookies(request))
                .requestParams(getParams(request))
                .environment(getEnvironment())
                .systemProperties(getProperties())
                .populatedProperty(populateMe)
                .build();
    }

    private List<Nvp> getEnvironment() {
        List<Nvp> env = new ArrayList<>();
        for (Entry<String, String> entry : System.getenv().entrySet()) {
            env.add(new Nvp(entry.getKey(), truncate(entry.getValue())));
        }
        env.sort(new NvpComparator());
        return env;
    }

    private List<Nvp> getProperties() {
        List<Nvp> props = new ArrayList<>();
        for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
            props.add(new Nvp(entry.getKey().toString(), truncate(entry.getValue().toString())));
        }
        props.sort(new NvpComparator());
        return props;
    }

    private List<Nvp> getHeaders(HttpServletRequest request) {
        log.debug("---------- Headers ----------");
        List<Nvp> headers = new ArrayList<>();
        val names = request.getHeaderNames();
        if (names != null) {
            while (names.hasMoreElements()) {
                val name = names.nextElement();
                val sb = new StringBuilder();
                val values = request.getHeaders(name);
                String sep = "";
                while (values.hasMoreElements()) {
                    val value = values.nextElement();
                    sb.append(sep).append(value);
                    sep = ", ";
                }
                headers.add(new Nvp(name, sb.toString()));
            }
        }
        headers.sort(new NvpComparator());
        return headers;
    }
    
    private List<Nvp> getCookies(HttpServletRequest request) {
        log.debug("---------- Cookies ----------");
        List<Nvp> cookies = new ArrayList<>();
        val cookieArray = request.getCookies();
        if (cookieArray != null) {
            for (val cookie : cookieArray) {
                cookies.add(new Nvp(cookie.getName(), truncate(cookie.getValue())));
            }
        }
        cookies.sort(new NvpComparator());
        return cookies;
    }
    
    private List<Nvp> getParams(HttpServletRequest request) {
        log.debug("---------- Parameters ----------");
        List<Nvp> params = new ArrayList<>();
        for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            val sb = new StringBuilder();
            String[] values = entry.getValue();
            String sep = "";
            for (val value : values) {
                sb.append(sep).append(value);
                sep = ", ";
            }
            params.add(new Nvp(entry.getKey(), truncate(sb.toString())));
        }
        params.sort(new NvpComparator());
        return params;
    }

    private String truncate(String in) {
        return truncate(in, 100);
    }
    
    private String truncate(String in, int maxlen) {
        if (in == null || in.length() <= maxlen) {
            return in;
        }
        return in.substring(0, maxlen);
    }
}
