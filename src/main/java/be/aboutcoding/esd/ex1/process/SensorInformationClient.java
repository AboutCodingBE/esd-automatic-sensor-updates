package be.aboutcoding.esd.ex1.process;

import be.aboutcoding.esd.ex1.infrastructure.SensorInformationResponse;
import be.aboutcoding.esd.ex1.model.Sensor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@Component
public class SensorInformationClient {
    private final RestTemplate restTemplate;
    private final ApiProperties properties;

    public SensorInformationClient(
            RestTemplate restTemplate,
            ApiProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public Sensor getSensorInformation(Long sensorId) {
        log.info("Retrieving information for sensor with ID: {}", sensorId);

        try {
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
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Sensor with ID {} not found in API", sensorId);
            return createDefaultSensor(sensorId);
        } catch (Exception e) {
            log.error("Error retrieving sensor information for ID {}: {}", sensorId, e.getMessage());
            return createDefaultSensor(sensorId);
        }
    }

    private Sensor createDefaultSensor(Long sensorId) {
        return new Sensor(sensorId, null, null, null);
    }
}
