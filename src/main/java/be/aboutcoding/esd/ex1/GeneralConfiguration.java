package be.aboutcoding.esd.ex1;

import be.aboutcoding.esd.ex1.process.ApiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for creating RestTemplate bean.
 */
@Configuration
@EnableConfigurationProperties(ApiProperties.class)
public class GeneralConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
