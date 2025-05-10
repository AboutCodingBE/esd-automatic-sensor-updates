package be.aboutcoding.esd.ex1.process;

import be.aboutcoding.esd.ex1.infrastructure.SensorInformationResponse;
import be.aboutcoding.esd.ex1.model.Sensor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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

@Component
public class SensorInformationClient {

    private static final Logger logger = LoggerFactory.getLogger(SensorInformationClient.class);

    private final RestTemplate restTemplate;
    private final String sensorApiUrl;

    public SensorInformationClient(
            RestTemplate restTemplate,
            @Value("${sensor.api.url}") String sensorApiUrl) {
        this.restTemplate = restTemplate;
        this.sensorApiUrl = sensorApiUrl;
    }

    public Sensor getSensorInformation(Long sensorId) {
        logger.info("Retrieving information for sensor with ID: {}", sensorId);

        try {
            String url = sensorApiUrl + "/sensors/" + sensorId;
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-auth-id", "CL019567d9-6e3b-738b-9b6d-32496110bd35==");
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
                logger.warn("Received empty response body for sensor ID: {}", sensorId);
                return createDefaultSensor(sensorId);
            }
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Sensor with ID {} not found in API", sensorId);
            return createDefaultSensor(sensorId);
        } catch (Exception e) {
            logger.error("Error retrieving sensor information for ID {}: {}", sensorId, e.getMessage());
            return createDefaultSensor(sensorId);
        }
    }

    private Sensor createDefaultSensor(Long sensorId) {
        return new Sensor(sensorId, null, null, null);
    }
}
