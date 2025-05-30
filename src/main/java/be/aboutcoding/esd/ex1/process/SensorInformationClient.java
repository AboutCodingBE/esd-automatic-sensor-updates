package be.aboutcoding.esd.ex1.process;

import be.aboutcoding.esd.ex1.infrastructure.SensorInformationResponse;
import be.aboutcoding.esd.ex1.model.TS50X;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class SensorInformationClient {
    private final RestTemplate restTemplate;
    private final ApiProperties properties;

    public SensorInformationClient(RestTemplate restTemplate, ApiProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public TS50X getSensorInformation(Long sensorId) {
        log.info("Retrieving information for sensor with ID: {}", sensorId);

        String url = properties.url() + "/sensors/" + sensorId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-auth-id", properties.authKey());
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<SensorInformationResponse> apiResponse = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                SensorInformationResponse.class
        );
        if (apiResponse.getBody() != null) {
            SensorInformationResponse response = apiResponse.getBody();
            return response.toSensor();
        } else {
            log.warn("Received empty response body for sensor ID: {}", sensorId);
            return createDefaultSensor(sensorId);
        }
    }

    private TS50X createDefaultSensor(Long sensorId) {
        return new TS50X(sensorId, null, null);
    }
}
