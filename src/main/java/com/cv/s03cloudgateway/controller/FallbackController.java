package com.cv.s03cloudgateway.controller;

import com.cv.s03cloudgateway.dto.FallBackResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/fallback/")
public class FallbackController {

    @RequestMapping("org-service")
    public ResponseEntity<Object> fallbackForOrgService() {
        return getFallbackResponse("Org service currently unavailable");
    }

    private ResponseEntity<Object> getFallbackResponse(Object o) {
        try {
            return new ResponseEntity<>(new FallBackResponseDto(false,
                    "Fallback",
                    0, o),
                    HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception ex) {
            log.error("StaticUtil.getFailureResponse {1}", ex);
            return new ResponseEntity<>(new FallBackResponseDto(false, "Fallback",
                    0, ExceptionUtils.getMessage(ex)),
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
