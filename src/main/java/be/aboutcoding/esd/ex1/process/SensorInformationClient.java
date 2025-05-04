package be.aboutcoding.esd.ex1.process;

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

/**
 * Client for retrieving sensor information from a third-party API.
 */
@Component
public class SensorInformationClient {

    private static final Logger logger = LoggerFactory.getLogger(SensorInformationClient.class);

    private final RestTemplate restTemplate;
    private final String sensorApiUrl;

    /**
     * Constructor for SensorInformationClient.
     *
     * @param restTemplate The RestTemplate for making HTTP requests
     * @param sensorApiUrl The base URL of the sensor information API
     */
    public SensorInformationClient(
            RestTemplate restTemplate,
            @Value("${sensor.api.url}") String sensorApiUrl) {
        this.restTemplate = restTemplate;
        this.sensorApiUrl = sensorApiUrl;
    }

    /**
     * Retrieves detailed information about a sensor from the third-party API.
     *
     * @param sensorId The ID of the sensor to retrieve information for
     * @return A Sensor object containing the sensor's details
     */
    public Sensor getSensorInformation(Long sensorId) {
        logger.info("Retrieving information for sensor with ID: {}", sensorId);


        try {
            String url = sensorApiUrl + "/sensors/" + sensorId;
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-auth-id", "CL019567d9-6e3b-738b-9b6d-32496110bd35==");
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<SensorApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    SensorApiResponse.class
            );
            if (response.getBody() != null) {
                SensorApiResponse apiResponse = response.getBody();
                return mapToSensor(sensorId, apiResponse);
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

    /**
     * Maps the API response to a Sensor domain object.
     *
     * @param sensorId The ID of the sensor
     * @param apiResponse The response from the sensor API
     * @return A mapped Sensor object
     */
    private Sensor mapToSensor(Long sensorId, SensorApiResponse apiResponse) {
        return new Sensor(
                sensorId,
                apiResponse.currentFirmware(),
                apiResponse.currentConfiguration(),
                null // Status will be determined by the validation process
        );
    }

    /**
     * Creates a default sensor with unknown values.
     * Used when the API call fails or returns no data.
     *
     * @param sensorId The ID of the sensor
     * @return A default Sensor object
     */
    private Sensor createDefaultSensor(Long sensorId) {
        return new Sensor(sensorId, null, null, null);
    }

    /**
     * Inner class representing the structure of the sensor API response.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static record SensorApiResponse(@JsonProperty("current_firmware") String currentFirmware,
                                            @JsonProperty("current_configuration") String currentConfiguration) {
    }
}
