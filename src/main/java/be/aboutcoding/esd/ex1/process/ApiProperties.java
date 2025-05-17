package be.aboutcoding.esd.ex1.process;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sensor.api")
public record ApiProperties(String url, String authKey) {
}
