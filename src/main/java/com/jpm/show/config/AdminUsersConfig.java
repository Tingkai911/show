package com.jpm.show.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Data
@Configuration
@ConfigurationProperties("admin")
public class AdminUsersConfig {
    private Set<String> users;
}
