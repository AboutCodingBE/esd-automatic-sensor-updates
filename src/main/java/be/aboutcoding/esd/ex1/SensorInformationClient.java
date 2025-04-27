package be.aboutcoding.esd.ex1;

import org.springframework.beans.factory.annotation.Value;
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
    public Sensor getSensorInformation(String sensorId) {
        logger.info("Retrieving information for sensor with ID: {}", sensorId);

        try {
            String url = sensorApiUrl + "/sensors/" + sensorId;
            ResponseEntity<SensorApiResponse> response = restTemplate.getForEntity(url, SensorApiResponse.class);

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
    private Sensor mapToSensor(String sensorId, SensorApiResponse apiResponse) {
        return new Sensor(
                sensorId,
                apiResponse.getFirmwareVersion(),
                apiResponse.getConfigurationFile(),
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
    private Sensor createDefaultSensor(String sensorId) {
        return new Sensor(sensorId, null, null, null);
    }

    /**
     * Inner class representing the structure of the sensor API response.
     */
    private static class SensorApiResponse {
        private String firmwareVersion;
        private String configurationFile;

        public String getFirmwareVersion() {
            return firmwareVersion;
        }

        public void setFirmwareVersion(String firmwareVersion) {
            this.firmwareVersion = firmwareVersion;
        }

        public String getConfigurationFile() {
            return configurationFile;
        }

        public void setConfigurationFile(String configurationFile) {
            this.configurationFile = configurationFile;
        }
    }
}
