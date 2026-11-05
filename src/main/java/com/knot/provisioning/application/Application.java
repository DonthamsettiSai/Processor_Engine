package com.knot.provisioning.application;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

import com.knot.jwt.security.JwtRequestFilter;
import com.knot.jwt.security.JwtService;
import com.knot.provisioning.service.ProvisioningService;

@SpringBootApplication(scanBasePackages = { "com.knot" })
@EntityScan(basePackages = { "com.knot.provioning.model", "com.knot.provisioning.bo" })
@EnableCaching
public class Application {

    private static final Logger log = LogManager.getLogger(Application.class);
    private final ProvisioningService provisioningService;

    public Application(ProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    public static void main(String[] args) {
        try {
            ThreadContext.put("HOST_NAME", java.net.InetAddress.getLocalHost().getHostName());
        } catch (Exception e) {
            ThreadContext.put("HOST_NAME", "UNKNOWN");
        }
        ThreadContext.put("TRX_ID", "API-REST-" + UUID.randomUUID());
        SpringApplication.run(Application.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startProvisioningProcessor() {
        try {
            log.info("Application ready; starting provisioning processor");
            provisioningService.processProvisioningRequest();
        } catch (Exception e) {
            log.error("Unable to start provisioning processor", e);
        }
    }

    @Bean
    public JwtService jwtService() {
        return new JwtService();
    }

    @Bean
    public JwtRequestFilter jwtRequestFilter() {
        return new JwtRequestFilter();
    }
}
