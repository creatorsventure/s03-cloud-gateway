package com.cv.s03cloudgateway.config.props;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "cloud-gateway")
public class CloudGatewayProperties {
    private List<String> allowedOrigins;
    private List<String> unauthenticatedPaths;
}
